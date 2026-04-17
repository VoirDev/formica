package dev.voir.formica.core

/**
 * Form-level validation output returned by adapters and by Formica.validate().
 *
 * fieldErrors -> keyed by schema field key
 * formErrors  -> non-field/global errors
 */
data class FormicaValidationResult<Data : Any>(
    val data: Data,
    val fieldErrors: Map<String, String> = emptyMap(),
    val formErrors: List<String> = emptyList()
) {
    val isValid: Boolean get() = fieldErrors.isEmpty() && formErrors.isEmpty()
}
