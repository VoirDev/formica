package dev.voir.formica.schema

class ValidationException(
    val validationErrors: List<FieldError>
) : IllegalArgumentException(
    validationErrors.joinToString(separator = "\n") { "${it.field}: ${it.message}" }
)
