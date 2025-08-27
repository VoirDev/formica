package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.voir.formica.Formica
import dev.voir.formica.FormicaFieldId
import dev.voir.formica.FormicaFieldResult
import dev.voir.formica.ValidationRule
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * A stable snapshot of a form field's UI-facing state and operations.
 *
 * This is what you pass into your composable input components so they have:
 *  - [value] → the current field value (nullable to represent "empty")
 *  - [error] → the last validation error message, or null if valid
 *  - [touched] → whether the user has interacted with the field yet
 *  - [dirty] → whether the value has changed from its initial value
 *  - [onChange] → callback to update the value (also updates form data snapshot)
 *  - [validate] → function to trigger validation manually; returns true if valid
 *
 * Marked as @Stable so Compose can optimize recompositions when only internal
 * values change.
 */
@Stable
data class FieldAdapter<V>(
    val value: V?,
    val error: String?,
    val touched: Boolean,
    val dirty: Boolean,
    val onChange: (V?) -> Unit,
    val validate: () -> Boolean
)


/**
 * Register a form field with the given [Formica] instance and expose it to UI
 * via a [FieldAdapter] in a type-safe, reactive way.
 *
 * This overload is for when you have an explicit [form] reference.
 *
 * Typical usage in Compose:
 * ```
 * FormicaField(form, FirstName, validators = setOf(...)) { f ->
 *     BasicTextField(
 *         value = f.value ?: "",
 *         onValueChange = { f.onChange(it) }
 *     )
 *     f.error?.let { Text(it, color = Color.Red) }
 * }
 * ```
 *
 * @param form The form instance holding all field state and data snapshot.
 * @param id A [FormicaFieldId] lens for reading/writing this field in the form's data.
 * @param validators Optional set of ordered validation rules for this field.
 * @param customValidation Optional extra validation run after [validators].
 * @param validateOnChange Whether to run validation automatically on every value change.
 * @param content Composable content lambda that receives a [FieldAdapter] for UI binding.
 */
@Composable
fun <D, V> FormicaField(
    form: Formica<D>,
    id: FormicaFieldId<D, V>,
    validators: Set<ValidationRule<V?>> = emptySet(),
    customValidation: ((V?) -> FormicaFieldResult)? = null,
    validateOnChange: Boolean = true,
    content: @Composable (FieldAdapter<V>) -> Unit
) {
    // Register the field once for this form + id combination.
    // Registration seeds its initial value from the current form data snapshot.
    val field = remember(form, id) {
        form.registerField(
            id = id,
            validators = validators,
            customValidation = customValidation,
            validateOnChange = validateOnChange
        )
    }

    // Collect reactive field state for UI binding.
    // These flows update whenever field state changes (value, error, touched, dirty).
    val value by field.value.collectAsState(initial = id.get(form.data.value))
    val error by field.error.collectAsState(initial = null)
    val touched by field.touched.collectAsState(initial = false)
    val dirty by field.dirty.collectAsState(initial = false)

    // Package the reactive state and callbacks into a stable adapter for the UI.
    val adapter = remember(value, error, touched, dirty) {
        FieldAdapter(
            value = value,
            error = error,
            touched = touched,
            dirty = dirty,
            onChange = { v -> form.onChange(id, v) },
            validate = { field.isValid() }
        )
    }

    // Render UI content with the adapter.
    content(adapter)
}

/**
 * Overload of [FormicaField] that uses the ambient [LocalFormica] form context
 * instead of requiring an explicit [form] parameter.
 *
 * Allows cleaner usage when you've wrapped your UI in a FormicaProvider:
 * ```
 * FormicaProvider(form) {
 *     FormicaField(FirstName) { f ->
 *         BasicTextField(
 *             value = f.value ?: "",
 *             onValueChange = { f.onChange(it) }
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun <D, V> FormicaField(
    id: FormicaFieldId<D, V>,
    validators: Set<ValidationRule<V?>> = emptySet(),
    customValidation: ((V?) -> FormicaFieldResult)? = null,
    validateOnChange: Boolean = true,
    content: @Composable (FieldAdapter<V>) -> Unit
) {
    val form = formicaOf<D>() // Grab the current form from the composition
    FormicaField(
        form = form,
        id = id,
        validators = validators,
        customValidation = customValidation,
        validateOnChange = validateOnChange,
        content = content
    )
}

/**
 * Convenience for reading a field's committed value from a [Formica] instance
 * reactively (without registering a field).
 *
 * Returns the current value of the field from the immutable form data snapshot.
 */
@Composable
fun <D, V> rememberFormicaFieldValue(
    form: Formica<D>,
    id: FormicaFieldId<D, V>,
    // Optional comparator (useful for floats with epsilon)
    areEquivalent: (V?, V?) -> Boolean = { a, b -> a == b }
): V? {
    // If the field is registered, prefer its own StateFlow (cheapest & already scoped)
    val registered = remember(form, id) { form.getRegisteredField(id) }
    val initial = remember(form, id) { id.get(form.data.value) }

    return if (registered != null) {
        registered.value.collectAsState(initial = initial).value
    } else {
        // Project the form snapshot to just this field and suppress identical emissions
        val projected = remember(form, id, areEquivalent) {
            form.data
                .map { id.get(it) }
                .distinctUntilChanged { old, new -> areEquivalent(old, new) }
        }
        projected.collectAsState(initial = initial).value
    }
}

@Composable
fun <D, V> rememberFormicaFieldValue(
    id: FormicaFieldId<D, V>,
    areEquivalent: (V?, V?) -> Boolean = { a, b -> a == b }
): V? {
    val form = formicaOf<D>()
    return rememberFormicaFieldValue(form, id, areEquivalent)
}

/**
 * Overload of [rememberFormicaFieldValue] that uses [LocalFormica] instead of
 * requiring an explicit [form] parameter.
 *
 * Useful inside a [FormicaProvider] scope:
 * ```
 * val firstName = rememberFormicaFieldValue(FirstName) ?: ""
 * ```
 */
@Composable
fun <D, V> rememberFormicaFieldValue(id: FormicaFieldId<D, V>): V? {
    val form = formicaOf<D>()
    val data by form.data.collectAsState()
    return id.get(data)
}
