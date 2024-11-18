package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import dev.voir.formica.Formica
import dev.voir.formica.scopes.FormicaScope

@Composable
fun <Data : Any> Formica(form: Formica<Data>, content: @Composable FormicaScope<Data>.() -> Unit) {
    val scope = remember { derivedStateOf { FormicaScope(f = form) } }
    scope.value.content()
}
