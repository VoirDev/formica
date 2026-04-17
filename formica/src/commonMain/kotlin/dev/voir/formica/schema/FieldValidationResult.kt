package dev.voir.formica.schema

sealed interface FieldValidationResult {
    data object Success : FieldValidationResult
    data object Skip : FieldValidationResult
    data class Error(val message: String) : FieldValidationResult
}
