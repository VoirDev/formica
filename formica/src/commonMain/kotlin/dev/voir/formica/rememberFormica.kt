package dev.voir.formica

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

@Composable
fun <Data : Any> rememberFormica(initialData: Data) = remember {
    Formica(initialData = initialData)
}

@Composable
fun <Data : Any> Formica<Data>.collectDataAsState() = this.data.collectAsState()

@Composable
fun <Data : Any> Formica<Data>.collectResultAsState() = this.result.collectAsState()
