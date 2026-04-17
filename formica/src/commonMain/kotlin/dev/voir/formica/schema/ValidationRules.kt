package dev.voir.formica.schema


private const val SPECIAL_PASSWORD_CHARS = "!@#$%^&*()-_=+[]{};:'\",.<>?/|\\`~"

internal const val EMAIL_PATTERN = "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"

internal const val DOMAIN_URL_PATTERN =
    "^[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]*$"

internal const val HTTP_URL_PATTERN =
    "^https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]*$"

private val EMAIL_REGEX = Regex(EMAIL_PATTERN)
private val DOMAIN_URL_REGEX = Regex(DOMAIN_URL_PATTERN)
private val HTTP_URL_REGEX = Regex(HTTP_URL_PATTERN)

/**
 * Built-in reusable rules.
 *
 * These rules intentionally keep your semantics:
 * - optional/null values mostly return Skip
 * - required is generic and treats null/blank/empty collection as empty by default
 */
object ValidationRules {

    fun <V> validateOnlyIf(
        active: () -> Boolean,
        rule: ValidationRule<V?>
    ): ValidationRule<V?> =
        ValidationRule { value ->
            if (active()) rule.validate(value) else FieldValidationResult.Success
        }

    fun <V> required(
        message: String = "Field is required",
        isEmpty: (V?) -> Boolean = { value ->
            when (value) {
                null -> true
                is String -> value.isBlank()
                is Collection<*> -> value.isEmpty()
                else -> false
            }
        }
    ): ValidationRule<V?> =
        ValidationRule { value ->
            if (isEmpty(value)) {
                FieldValidationResult.Error(message)
            } else {
                FieldValidationResult.Success
            }
        }

    fun notEmpty(
        message: String = "This field cannot be empty."
    ): ValidationRule<String?> =
        ValidationRule { value ->
            when {
                value == null -> FieldValidationResult.Skip
                value.isNotEmpty() -> FieldValidationResult.Success
                else -> FieldValidationResult.Error(message)
            }
        }

    fun notBlank(
        message: String = "This field cannot be blank."
    ): ValidationRule<String?> =
        ValidationRule { value ->
            when {
                value == null -> FieldValidationResult.Skip
                value.isNotBlank() -> FieldValidationResult.Success
                else -> FieldValidationResult.Error(message)
            }
        }

    fun email(
        message: String = "Must be a valid email address.",
        pattern: Regex = EMAIL_REGEX
    ): ValidationRule<String?> =
        ValidationRule { value ->
            when {
                value == null -> FieldValidationResult.Skip
                pattern.matches(value) -> FieldValidationResult.Success
                else -> FieldValidationResult.Error(message)
            }
        }

    fun strongPassword(
        minLength: Int = 8,
        lengthMessage: String = "Password must be at least $minLength characters long.",
        uppercaseMessage: String = "Password must contain at least one uppercase letter.",
        lowercaseMessage: String = "Password must contain at least one lowercase letter.",
        digitMessage: String = "Password must contain at least one digit.",
        specialCharacterMessage: String = "Password must contain at least one special character.",
    ): ValidationRule<String?> =
        ValidationRule { value ->
            when {
                value == null -> FieldValidationResult.Skip
                value.length < minLength -> FieldValidationResult.Error(lengthMessage)
                !value.any(Char::isUpperCase) -> FieldValidationResult.Error(uppercaseMessage)
                !value.any(Char::isLowerCase) -> FieldValidationResult.Error(lowercaseMessage)
                !value.any(Char::isDigit) -> FieldValidationResult.Error(digitMessage)
                !value.any { it in SPECIAL_PASSWORD_CHARS } ->
                    FieldValidationResult.Error(specialCharacterMessage)

                else -> FieldValidationResult.Success
            }
        }

    fun url(
        protocolRequired: Boolean = false,
        message: String = "Must be a valid URL."
    ): ValidationRule<String?> =
        ValidationRule { value ->
            if (value == null) return@ValidationRule FieldValidationResult.Skip

            val valid = if (protocolRequired) {
                HTTP_URL_REGEX.matches(value)
            } else {
                HTTP_URL_REGEX.matches(value) || DOMAIN_URL_REGEX.matches(value)
            }

            if (valid) {
                FieldValidationResult.Success
            } else {
                FieldValidationResult.Error(message)
            }
        }

    fun checked(
        message: String = "Must be checked"
    ): ValidationRule<Boolean?> =
        ValidationRule { value ->
            if (value == null) return@ValidationRule FieldValidationResult.Skip
            if (value) FieldValidationResult.Success else FieldValidationResult.Error(message)
        }

    fun minLength(
        option: Int,
        message: String = "Must be at least $option characters long.",
    ): ValidationRule<String?> =
        ValidationRule { value ->
            if (value == null) return@ValidationRule FieldValidationResult.Skip
            if (value.length >= option) {
                FieldValidationResult.Success
            } else {
                FieldValidationResult.Error(message)
            }
        }

    fun maxLength(
        option: Int,
        message: String = "Must not exceed $option characters.",
    ): ValidationRule<String?> =
        ValidationRule { value ->
            if (value == null) return@ValidationRule FieldValidationResult.Skip
            if (value.length <= option) {
                FieldValidationResult.Success
            } else {
                FieldValidationResult.Error(message)
            }
        }

    fun <T> range(
        min: T,
        max: T,
        inclusive: Boolean = true,
        message: (min: T, max: T) -> String = { lo, hi ->
            "Must be a number between $lo and $hi."
        }
    ): ValidationRule<T?> where T : Number, T : Comparable<T> =
        ValidationRule { value ->
            if (value == null) return@ValidationRule FieldValidationResult.Skip

            val ok = if (inclusive) {
                value >= min && value <= max
            } else {
                value > min && value < max
            }

            if (ok) {
                FieldValidationResult.Success
            } else {
                FieldValidationResult.Error(message(min, max))
            }
        }

    fun range(
        min: Double,
        max: Double,
        inclusive: Boolean = true,
        epsilon: Double = 0.0,
        message: (Double, Double) -> String = { lo, hi ->
            "Must be a number between $lo and $hi."
        }
    ): ValidationRule<Double?> =
        ValidationRule { value ->
            if (value == null || value.isNaN()) return@ValidationRule FieldValidationResult.Skip

            val lower = if (inclusive) value >= min - epsilon else value > min + epsilon
            val upper = if (inclusive) value <= max + epsilon else value < max - epsilon

            if (lower && upper) {
                FieldValidationResult.Success
            } else {
                FieldValidationResult.Error(message(min, max))
            }
        }

    fun range(
        min: Float,
        max: Float,
        inclusive: Boolean = true,
        epsilon: Float = 0f,
        message: (Float, Float) -> String = { lo, hi ->
            "Must be a number between $lo and $hi."
        }
    ): ValidationRule<Float?> =
        ValidationRule { value ->
            if (value == null || value.isNaN()) return@ValidationRule FieldValidationResult.Skip

            val lower = if (inclusive) value >= min - epsilon else value > min + epsilon
            val upper = if (inclusive) value <= max + epsilon else value < max - epsilon

            if (lower && upper) {
                FieldValidationResult.Success
            } else {
                FieldValidationResult.Error(message(min, max))
            }
        }

    fun min(
        min: Int,
        inclusive: Boolean = true,
        message: (Int) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
    ): ValidationRule<Int?> =
        ValidationRule { value ->
            if (value == null) return@ValidationRule FieldValidationResult.Skip
            val ok = if (inclusive) value >= min else value > min
            if (ok) FieldValidationResult.Success else FieldValidationResult.Error(message(min))
        }

    fun max(
        max: Int,
        inclusive: Boolean = true,
        message: (Int) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
    ): ValidationRule<Int?> =
        ValidationRule { value ->
            if (value == null) return@ValidationRule FieldValidationResult.Skip
            val ok = if (inclusive) value <= max else value < max
            if (ok) FieldValidationResult.Success else FieldValidationResult.Error(message(max))
        }

    fun min(
        min: Long,
        inclusive: Boolean = true,
        message: (Long) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
    ): ValidationRule<Long?> =
        ValidationRule { value ->
            if (value == null) return@ValidationRule FieldValidationResult.Skip
            val ok = if (inclusive) value >= min else value > min
            if (ok) FieldValidationResult.Success else FieldValidationResult.Error(message(min))
        }

    fun max(
        max: Long,
        inclusive: Boolean = true,
        message: (Long) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
    ): ValidationRule<Long?> =
        ValidationRule { value ->
            if (value == null) return@ValidationRule FieldValidationResult.Skip
            val ok = if (inclusive) value <= max else value < max
            if (ok) FieldValidationResult.Success else FieldValidationResult.Error(message(max))
        }

    fun min(
        min: Double,
        inclusive: Boolean = true,
        epsilon: Double = 0.0,
        message: (Double) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
    ): ValidationRule<Double?> =
        ValidationRule { value ->
            if (value == null || value.isNaN()) return@ValidationRule FieldValidationResult.Skip
            val ok = if (inclusive) value >= min - epsilon else value > min + epsilon
            if (ok) FieldValidationResult.Success else FieldValidationResult.Error(message(min))
        }

    fun max(
        max: Double,
        inclusive: Boolean = true,
        epsilon: Double = 0.0,
        message: (Double) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
    ): ValidationRule<Double?> =
        ValidationRule { value ->
            if (value == null || value.isNaN()) return@ValidationRule FieldValidationResult.Skip
            val ok = if (inclusive) value <= max + epsilon else value < max - epsilon
            if (ok) FieldValidationResult.Success else FieldValidationResult.Error(message(max))
        }

    fun min(
        min: Float,
        inclusive: Boolean = true,
        epsilon: Float = 0f,
        message: (Float) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
    ): ValidationRule<Float?> =
        ValidationRule { value ->
            if (value == null || value.isNaN()) return@ValidationRule FieldValidationResult.Skip
            val ok = if (inclusive) value >= min - epsilon else value > min + epsilon
            if (ok) FieldValidationResult.Success else FieldValidationResult.Error(message(min))
        }

    fun max(
        max: Float,
        inclusive: Boolean = true,
        epsilon: Float = 0f,
        message: (Float) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
    ): ValidationRule<Float?> =
        ValidationRule { value ->
            if (value == null || value.isNaN()) return@ValidationRule FieldValidationResult.Skip
            val ok = if (inclusive) value <= max + epsilon else value < max - epsilon
            if (ok) FieldValidationResult.Success else FieldValidationResult.Error(message(max))
        }
}
