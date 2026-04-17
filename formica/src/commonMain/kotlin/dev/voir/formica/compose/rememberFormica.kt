package dev.voir.formica.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.voir.formica.core.Formica
import dev.voir.formica.core.FormicaValidationAdapter

/**
 * Remember one Formica instance for the given adapter + initial data.
 *
 * This keeps the form runtime stable across recompositions.
 */
@Composable
fun <Data : Any> rememberFormica(
    adapter: FormicaValidationAdapter<Data>,
    initialData: Data,
    onSubmit: ((Data) -> Unit)? = null
): Formica<Data> {
    return remember(adapter, initialData, onSubmit) {
        Formica(
            adapter = adapter,
            initialData = initialData,
            onSubmit = onSubmit
        )
    }
}
