package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun <Data : Any> rememberFormica(initialData: Data) = remember {
    dev.voir.formica.Formica(initialData = initialData)
}
