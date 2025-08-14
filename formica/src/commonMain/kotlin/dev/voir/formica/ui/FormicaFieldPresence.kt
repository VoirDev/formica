package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import dev.voir.formica.Formica
import dev.voir.formica.FormicaFieldId

@Composable
fun <D, V> FormFieldPresence(
    form: Formica<D>,
    id: FormicaFieldId<D, V>,
    present: Boolean,
    clearOnHide: Boolean = false,
    content: @Composable () -> Unit
) {
    val field = remember(form, id) { form.getRegisteredField(id) }
    LaunchedEffect(present) {
        field?.setEnabled(present)
        if (!present && clearOnHide) form.onChange(id, null)
    }
    if (present) content()
}