package dev.voir.formica.schema

/**
 * Result returned by schema rules.
 *
 * This maps naturally to formica-core field validation results.
 */
sealed interface FieldValidationResult {
    data object Success : FieldValidationResult
    data object Skip : FieldValidationResult
    data class Error(val message: String) : FieldValidationResult
}
