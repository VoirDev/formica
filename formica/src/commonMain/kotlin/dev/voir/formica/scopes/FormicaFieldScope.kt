package dev.voir.formica.scopes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import dev.voir.formica.FormicaField

class FormicaFieldScope<V : Any?>(
    private val formField: FormicaField<V>,
) {
    val field: State<V?>
        @Composable
        get() = this.formField.value.collectAsState(null)

    val error: State<String?>
        @Composable
        get() = this.formField.error.collectAsState(null)
}
