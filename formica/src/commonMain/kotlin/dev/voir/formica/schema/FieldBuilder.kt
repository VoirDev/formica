package dev.voir.formica.schema

class FieldBuilder<T : Any, V> {
    private val rules = mutableListOf<ValidationRule<V>>()

    fun rule(rule: ValidationRule<V>) {
        rules += rule
    }

    internal fun build(): List<ValidationRule<V>> = rules
}

fun <T : Any, V> FieldBuilder<T, V?>.required(
    message: String = "Field is required",
    isEmpty: (V?) -> Boolean = { v ->
        when (v) {
            null -> true
            is String -> v.isBlank()
            is Collection<*> -> v.isEmpty()
            else -> false
        }
    }
) {
    rule(ValidationRules.required(message, isEmpty))
}

fun <T : Any, V> FieldBuilder<T, V?>.validateOnlyIf(
    active: () -> Boolean,
    rule: ValidationRule<V?>
) {
    rule(ValidationRules.validateOnlyIf(active, rule))
}

fun <T : Any> FieldBuilder<T, String?>.notEmpty(
    message: String = "This field cannot be empty."
) {
    rule(ValidationRules.notEmpty(message))
}

fun <T : Any> FieldBuilder<T, String?>.notBlank(
    message: String = "This field cannot be blank."
) {
    rule(ValidationRules.notBlank(message))
}

fun <T : Any> FieldBuilder<T, String?>.email(
    message: String = "Must be a valid email address.",
    pattern: Regex = EMAIL_PATTERN.toRegex()
) {
    rule(ValidationRules.email(message, pattern))
}

fun <T : Any> FieldBuilder<T, String?>.strongPassword(
    minLength: Int = 8,
    lengthMessage: String = "Password must be at least $minLength characters long.",
    uppercaseMessage: String = "Password must contain at least one uppercase letter.",
    lowercaseMessage: String = "Password must contain at least one lowercase letter.",
    digitMessage: String = "Password must contain at least one digit.",
    specialCharacterMessage: String = "Password must contain at least one special character.",
) {
    rule(
        ValidationRules.strongPassword(
            minLength,
            lengthMessage,
            uppercaseMessage,
            lowercaseMessage,
            digitMessage,
            specialCharacterMessage
        )
    )
}

fun <T : Any> FieldBuilder<T, String?>.url(
    protocolRequired: Boolean = false,
    message: String = "Must be a valid URL."
) {
    rule(ValidationRules.url(protocolRequired, message))
}

fun <T : Any> FieldBuilder<T, String?>.minLength(
    option: Int,
    message: String = "Must be at least $option characters long."
) {
    rule(ValidationRules.minLength(option, message))
}

fun <T : Any> FieldBuilder<T, String?>.maxLength(
    option: Int,
    message: String = "Must not exceed $option characters."
) {
    rule(ValidationRules.maxLength(option, message))
}

fun <T : Any> FieldBuilder<T, String>.notEmpty(
    message: String = "This field cannot be empty."
) {
    rule(
        ValidationRule { v ->
            if (v.isNotEmpty()) FieldValidationResult.Success
            else FieldValidationResult.Error(message)
        }
    )
}

fun <T : Any> FieldBuilder<T, String>.notBlank(
    message: String = "This field cannot be blank."
) {
    rule(
        ValidationRule { v ->
            if (v.isNotBlank()) FieldValidationResult.Success
            else FieldValidationResult.Error(message)
        }
    )
}

fun <T : Any> FieldBuilder<T, String>.email(
    message: String = "Must be a valid email address.",
    pattern: Regex = EMAIL_PATTERN.toRegex()
) {
    rule(
        ValidationRule { v ->
            if (pattern.matches(v)) FieldValidationResult.Success
            else FieldValidationResult.Error(message)
        }
    )
}

fun <T : Any, N> FieldBuilder<T, N?>.range(
    min: N,
    max: N,
    inclusive: Boolean = true,
    message: (N, N) -> String = { lo, hi -> "Must be a number between $lo and $hi." }
) where N : Number, N : Comparable<N> {
    rule(ValidationRules.range(min, max, inclusive, message))
}

fun <T : Any> FieldBuilder<T, Double?>.range(
    min: Double,
    max: Double,
    inclusive: Boolean = true,
    epsilon: Double = 0.0,
    message: (Double, Double) -> String = { lo, hi -> "Must be a number between $lo and $hi." }
) {
    rule(ValidationRules.range(min, max, inclusive, epsilon, message))
}

fun <T : Any> FieldBuilder<T, Float?>.range(
    min: Float,
    max: Float,
    inclusive: Boolean = true,
    epsilon: Float = 0f,
    message: (Float, Float) -> String = { lo, hi -> "Must be a number between $lo and $hi." }
) {
    rule(ValidationRules.range(min, max, inclusive, epsilon, message))
}

fun <T : Any> FieldBuilder<T, Int?>.min(
    min: Int,
    inclusive: Boolean = true,
    message: (Int) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
) {
    rule(ValidationRules.min(min, inclusive, message))
}

fun <T : Any> FieldBuilder<T, Int?>.max(
    max: Int,
    inclusive: Boolean = true,
    message: (Int) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
) {
    rule(ValidationRules.max(max, inclusive, message))
}

fun <T : Any> FieldBuilder<T, Long?>.min(
    min: Long,
    inclusive: Boolean = true,
    message: (Long) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
) {
    rule(ValidationRules.min(min, inclusive, message))
}

fun <T : Any> FieldBuilder<T, Long?>.max(
    max: Long,
    inclusive: Boolean = true,
    message: (Long) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
) {
    rule(ValidationRules.max(max, inclusive, message))
}

fun <T : Any> FieldBuilder<T, Double?>.min(
    min: Double,
    inclusive: Boolean = true,
    epsilon: Double = 0.0,
    message: (Double) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
) {
    rule(ValidationRules.min(min, inclusive, epsilon, message))
}

fun <T : Any> FieldBuilder<T, Double?>.max(
    max: Double,
    inclusive: Boolean = true,
    epsilon: Double = 0.0,
    message: (Double) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
) {
    rule(ValidationRules.max(max, inclusive, epsilon, message))
}

fun <T : Any> FieldBuilder<T, Float?>.min(
    min: Float,
    inclusive: Boolean = true,
    epsilon: Float = 0f,
    message: (Float) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
) {
    rule(ValidationRules.min(min, inclusive, epsilon, message))
}

fun <T : Any> FieldBuilder<T, Float?>.max(
    max: Float,
    inclusive: Boolean = true,
    epsilon: Float = 0f,
    message: (Float) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
) {
    rule(ValidationRules.max(max, inclusive, epsilon, message))
}
