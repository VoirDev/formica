package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.voir.formica.Formica
import dev.voir.formica.FormicaFieldId
import dev.voir.formica.FormicaFieldResult
import dev.voir.formica.ValidationRule

@Stable
data class FieldAdapter<V>(
    val value: V?,
    val error: String?,
    val touched: Boolean,
    val dirty: Boolean,
    val onChange: (V?) -> Unit,
    val validate: () -> Boolean
)


// Registers a field once and exposes reactive state + callbacks.
// Usage:
// FormField(form, FirstName, validators = setOf(...)) { f ->
//     BasicTextField(value = f.value ?: "", onValueChange = { f.onChange(it) })
//     f.error?.let { Text(it) }
// }
@Composable
fun <D, V> FormicaField(
    form: Formica<D>,
    id: FormicaFieldId<D, V>,
    validators: Set<ValidationRule<V?>> = emptySet(),
    customValidation: ((V?) -> FormicaFieldResult)? = null,
    validateOnChange: Boolean = true,
    // Provide content with a convenient adapter
    content: @Composable (FieldAdapter<V>) -> Unit
) {
    // Register once per (form, id)
    val field = remember(form, id) {
        form.registerField(
            id = id,
            validators = validators,
            customValidation = customValidation,
            validateOnChange = validateOnChange
        )
    }

    // Collect reactive bits
    val value by field.value.collectAsState(initial = id.get(form.data.value))
    val error by field.error.collectAsState(initial = null)
    val touched by field.touched.collectAsState(initial = false)
    val dirty by field.dirty.collectAsState(initial = false)

    // Keep field reset in sync if external data snapshot changes (optional)
    // This ensures that if you replace form.data from the outside (e.g., load draft),
    // the field UI updates its initial/dirty/touched state accordingly.
    val dataSnapshot by form.data.collectAsState()

    LaunchedEffect(dataSnapshot) {
        field.reset(id.get(dataSnapshot))
    }

    // Adapter that UI can use
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

    content(adapter)
}

@Composable
fun <D, V> FormicaField(
    id: FormicaFieldId<D, V>,
    validators: Set<ValidationRule<V?>> = emptySet(),
    customValidation: ((V?) -> FormicaFieldResult)? = null,
    validateOnChange: Boolean = true,
    content: @Composable (FieldAdapter<V>) -> Unit
) {
    val form = formicaOf<D>()
    FormicaField(
        form = form,
        id = id,
        validators = validators,
        customValidation = customValidation,
        validateOnChange = validateOnChange,
        content = content
    )
}

@Composable
fun <D, V> rememberFormicaFieldValue(form: Formica<D>, id: FormicaFieldId<D, V>): V? {
    val data by form.data.collectAsState()
    return id.get(data)
}

// Overload that uses LocalFormica
@Composable
fun <D, V> rememberFormicaFieldValue(id: FormicaFieldId<D, V>): V? {
    val form = formicaOf<D>()
    val data by form.data.collectAsState()
    return id.get(data)
}
