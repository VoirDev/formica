package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun <Data : Any> rememberFormica(
    initialData: Data,
    onSubmit: ((Data) -> Unit)? = null,
) = remember { dev.voir.formica.Formica(initialData = initialData, onSubmit = onSubmit) }
