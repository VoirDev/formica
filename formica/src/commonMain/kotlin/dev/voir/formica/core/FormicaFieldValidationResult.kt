package dev.voir.formica.core

/**
 * Result of validating a single field.
 *
 * Success = valid
 * Skip    = intentionally not validated (e.g. hidden/disabled/not applicable)
 * Error   = invalid with message
 */
sealed interface FormicaFieldValidationResult {
    data object Success : FormicaFieldValidationResult
    data object Skip : FormicaFieldValidationResult
    data class Error(val message: String) : FormicaFieldValidationResult
}
