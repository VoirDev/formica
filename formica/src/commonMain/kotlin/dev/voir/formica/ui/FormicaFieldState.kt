package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.voir.formica.Formica
import dev.voir.formica.FormicaFieldId

/**
 * A stable snapshot of a registered field's full reactive state and actions.
 *
 * This is very similar to [FieldAdapter], but intended for cases where you want
 * to **access a field's state outside of its own Composable input**, for example:
 *
 * - To conditionally render another UI element based on the field's value or error
 * - To build composite components that depend on multiple fields
 * - To trigger validation from somewhere else in the UI
 *
 * @param value Current value of the field (nullable to represent "empty").
 * @param error Current error message if invalid, or `null` if valid.
 * @param touched Whether the field has been interacted with at least once.
 * @param dirty Whether the value has changed from its initial value.
 * @param onChange Callback to update the field's value in both field state and form data.
 * @param validate Triggers validation immediately; returns `true` if valid.
 *
 * Marked @Stable so Compose can optimize recomposition.
 */
@Stable
data class FormicaFieldState<V>(
    val value: V?,
    val error: String?,
    val touched: Boolean,
    val dirty: Boolean,
    val onChange: (V?) -> Unit,
    val validate: () -> Boolean
)

/**
 * Retrieve a [FormicaFieldState] for a previously registered field in [form],
 * observing all of its reactive properties (value, error, touched, dirty).
 *
 * @param form The [Formica] instance that holds the registered field.
 * @param id The [FormicaFieldId] lens identifying the field.
 *
 * @return A [FormicaFieldState] bound to this field, or `null` if the field
 *         has not been registered in the form.
 *
 * ### Example
 * ```
 * val fieldState = rememberFormicaFieldState(form, Email)
 * if (fieldState?.error != null) {
 *     Text("Invalid email", color = Color.Red)
 * }
 * ```
 *
 * **Important:** This does *not* register the field. You must register it
 * beforehand with `FormicaField(...)` or `form.registerField(...)`.
 */
@Composable
fun <D, V> rememberFormicaFieldState(
    form: Formica<D>,
    id: FormicaFieldId<D, V>
): FormicaFieldState<V>? {
    // Remember the registered field instance so we don't look it up every recomposition
    val field = remember(form, id) { form.getRegisteredField(id) } ?: return null

    // Observe reactive properties from the field's StateFlows
    val value by field.value.collectAsState()
    val error by field.error.collectAsState()
    val touched by field.touched.collectAsState()
    val dirty by field.dirty.collectAsState()

    // Package everything into a stable snapshot object
    return remember(value, error, touched, dirty) {
        FormicaFieldState(
            value = value,
            error = error,
            touched = touched,
            dirty = dirty,
            onChange = { form.onChange(id, it) },
            validate = { field.isValid() }
        )
    }
}

/**
 * Overload of [rememberFormicaFieldState] that retrieves the current [Formica]
 * instance from [LocalFormica], so you don't need to pass `form` manually.
 *
 * Use inside a [FormicaProvider] scope:
 * ```
 * val fieldState = rememberFormicaFieldState(Email)
 * if (fieldState?.dirty == true) {
 *     SaveButton()
 * }
 * ```
 */
@Composable
fun <D, V> rememberFormicaFieldState(
    id: FormicaFieldId<D, V>
): FormicaFieldState<V>? {
    val form = formicaOf<D>()
    return rememberFormicaFieldState(form, id)
}
