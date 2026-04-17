package dev.voir.formica.core

/**
 * Last submit/validate status of the form itself.
 */
sealed interface FormicaSubmitResult {
    data object NoAttempt : FormicaSubmitResult
    data object Success : FormicaSubmitResult
    data class Error(
        val fieldErrors: Map<String, String>,
        val formErrors: List<String>
    ) : FormicaSubmitResult
}
