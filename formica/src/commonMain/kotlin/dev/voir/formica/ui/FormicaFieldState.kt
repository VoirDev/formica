package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.voir.formica.Formica
import dev.voir.formica.FormicaFieldId

@Stable
data class FormicaFieldState<V>(
    val value: V?,
    val error: String?,
    val touched: Boolean,
    val dirty: Boolean,
    val onChange: (V?) -> Unit,
    val validate: () -> Boolean
)

@Composable
fun <D, V> rememberFormicaFieldState(
    form: Formica<D>,
    id: FormicaFieldId<D, V>
): FormicaFieldState<V>? {
    val field = remember(form, id) { form.getRegisteredField(id) } ?: return null

    val value by field.value.collectAsState()
    val error by field.error.collectAsState()
    val touched by field.touched.collectAsState()
    val dirty by field.dirty.collectAsState()

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

@Composable
fun <D, V> rememberFormicaFieldState(id: FormicaFieldId<D, V>): FormicaFieldState<V>? {
    val form = formicaOf<D>()
    return rememberFormicaFieldState(form, id)
}
