package dev.voir.formica.schema

fun interface ValidationRule<T> {
    fun validate(value: T): FieldValidationResult
}
