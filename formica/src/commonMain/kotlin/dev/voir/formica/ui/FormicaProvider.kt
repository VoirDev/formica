package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import dev.voir.formica.Formica

val LocalFormica = staticCompositionLocalOf<Formica<Any>?> { null }

@Composable
fun <Data> rememberFormica(
    initialData: Data,
    onSubmit: ((Data) -> Unit)? = null
): Formica<Data> {
    // Recreate the form only when initialData/onSubmit changes
    return remember(initialData, onSubmit) {
        Formica(initialData, onSubmit)
    }
}

@Composable
fun <D> FormicaProvider(
    form: Formica<D>,
    content: @Composable () -> Unit
) {
    @Suppress("UNCHECKED_CAST")
    (CompositionLocalProvider(LocalFormica provides (form as Formica<Any>)) {
        content()
    })
}

@Suppress("UNCHECKED_CAST")
@Composable
fun <D> formicaOf(): Formica<D> {
    val f = LocalFormica.current
        ?: error("No Formica in scope. Wrap with FormicaProvider or pass form explicitly.")
    return f as Formica<D>
}
