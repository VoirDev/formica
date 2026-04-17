package dev.voir.formica.schema

data class ValidationResult(
    val errors: List<FieldError>
) {
    val isValid: Boolean get() = errors.isEmpty()

    fun throwIfInvalid() {
        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }
    }
}
