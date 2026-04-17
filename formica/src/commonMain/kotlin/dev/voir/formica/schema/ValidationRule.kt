package dev.voir.formica.schema

/**
 * A reusable validation unit for one value.
 *
 * Rules should be side-effect free.
 */
fun interface ValidationRule<T> {
    fun validate(value: T): FieldValidationResult
}

/**
 * Combine rules and stop at the first error.
 * Skip does not stop the chain.
 */
fun <T> ValidationRule<T>.and(other: ValidationRule<T>): ValidationRule<T> =
    ValidationRule { value ->
        when (val first = validate(value)) {
            is FieldValidationResult.Error -> first
            else -> other.validate(value)
        }
    }

/**
 * Wrap a rule so it only runs when [predicate] is true.
 */
fun <T> ValidationRule<T>.onlyIf(predicate: () -> Boolean): ValidationRule<T> =
    ValidationRule { value ->
        if (predicate()) validate(value) else FieldValidationResult.Success
    }
