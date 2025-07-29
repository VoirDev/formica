package dev.voir.formica

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

@Composable
fun <Data : Any> rememberFormica(initialData: Data, onSubmit: ((Data) -> Unit)? = null) = remember {
    Formica(
        initialData = initialData,
        onSubmit = onSubmit
    )
}

@Composable
fun <Data : Any> Formica<Data>.collectDataAsState() = this.data.collectAsState()

@Composable
fun <Data : Any> Formica<Data>.collectResultAsState() = this.result.collectAsState()
