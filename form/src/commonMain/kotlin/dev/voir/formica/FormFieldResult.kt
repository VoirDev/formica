package dev.voir.formica

sealed class FormFieldResult {
    data object Success : FormFieldResult()

    data class Error(val message: String) : FormFieldResult()

    data object NoInput : FormFieldResult()
}
