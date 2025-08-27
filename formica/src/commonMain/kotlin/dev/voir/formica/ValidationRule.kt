package dev.voir.formica

fun interface ValidationRule<V> {
    fun validate(value: V): FormicaFieldResult
}

object ValidationRules {
    fun <V> validateOnlyIf(active: () -> Boolean, rule: ValidationRule<V?>) =
        ValidationRule<V?> { v -> if (active()) rule.validate(v) else FormicaFieldResult.Success }

    fun <V> required(
        message: String = "Field is required",
        isEmpty: (V?) -> Boolean = { v ->
            when (v) {
                null -> true
                is String -> v.isBlank()
                is Collection<*> -> v.isEmpty()
                else -> false
            }
        }
    ): ValidationRule<V?> = ValidationRule { v ->
        if (isEmpty(v)) {
            FormicaFieldResult.Error(message)
        } else {
            FormicaFieldResult.Success
        }
    }

    /* TODO Maybe useful
       fun <D, V> requiredIf(
            form: Formica<D>,
            predicate: (D) -> Boolean,
            message: String
        ): ValidationRule<V?> = ValidationRule { v ->
            val active = predicate(form.data.value)        // read live snapshot
            if (!active) return@ValidationRule FormicaFieldResult.Success

            val empty = when (v) {
                null -> true
                is String -> v.isBlank()
                is Collection<*> -> v.isEmpty()
                else -> false
            }
            if (empty) FormicaFieldResult.Error(message) else FormicaFieldResult.Success
        }*/


    fun notEmpty(
        message: String = "This field cannot be empty."
    ): ValidationRule<String?> =
        ValidationRule { v ->
            when {
                v == null -> FormicaFieldResult.NoInput
                v.isNotEmpty() -> FormicaFieldResult.Success
                else -> FormicaFieldResult.Error(message)
            }
        }

    fun notBlank(
        message: String = "This field cannot be blank."
    ): ValidationRule<String?> =
        ValidationRule { v ->
            when {
                v == null -> FormicaFieldResult.NoInput
                v.isNotBlank() -> FormicaFieldResult.Success
                else -> FormicaFieldResult.Error(message)
            }
        }

    fun email(
        message: String = "Must be a valid email address.",
        pattern: Regex = EMAIL_PATTERN.toRegex()
    ): ValidationRule<String?> =
        ValidationRule { v ->
            when {
                v == null -> FormicaFieldResult.NoInput
                v.matches(pattern) -> FormicaFieldResult.Success
                else -> FormicaFieldResult.Error(message)
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
        ValidationRule { v ->
            when {
                v == null -> FormicaFieldResult.NoInput
                v.length < minLength -> FormicaFieldResult.Error(lengthMessage)
                !v.any { it.isUpperCase() } -> FormicaFieldResult.Error(uppercaseMessage)
                !v.any { it.isLowerCase() } -> FormicaFieldResult.Error(lowercaseMessage)
                !v.any { it.isDigit() } -> FormicaFieldResult.Error(digitMessage)
                !v.any { it in "!@#$%^&*()-_=+[]{};:'\",.<>?/|\\`~" } -> FormicaFieldResult.Error(
                    specialCharacterMessage
                )

                else -> FormicaFieldResult.Success
            }
        }

    fun url(
        protocolRequired: Boolean = false,
        message: String = "Must be a valid URL."
    ): ValidationRule<String?> = ValidationRule { v ->
        if (v == null) {
            return@ValidationRule FormicaFieldResult.NoInput
        }

        val result = if (protocolRequired) {
            v.matches(HTTP_URL_PATTERN.toRegex())
        } else {
            v.matches(HTTP_URL_PATTERN.toRegex()) || v.matches(DOMAIN_URL_PATTERN.toRegex())
        }

        if (result) {
            FormicaFieldResult.Success
        } else {
            FormicaFieldResult.Error(message)
        }
    }

    fun checked(message: String = "Must be checked"): ValidationRule<Boolean?> =
        ValidationRule { v ->
            if (v == null) {
                return@ValidationRule FormicaFieldResult.NoInput
            }

            if (v) {
                FormicaFieldResult.Success
            } else {
                FormicaFieldResult.Error(message)
            }
        }

    fun minLength(
        option: Int,
        message: String = "Must be at least $option characters long.",
    ): ValidationRule<String?> = ValidationRule { v ->
        if (v == null) {
            return@ValidationRule FormicaFieldResult.NoInput
        }
        if (v.count() >= option) {
            FormicaFieldResult.Success
        } else {
            FormicaFieldResult.Error(message)
        }
    }

    fun maxLength(
        option: Int,
        message: String = "Must not exceed $option characters.",
    ): ValidationRule<String?> = ValidationRule { v ->
        if (v == null) {
            return@ValidationRule FormicaFieldResult.NoInput
        }
        if (v.count() <= option) {
            FormicaFieldResult.Success
        } else {
            FormicaFieldResult.Error(message)
        }
    }

    fun <T> range(
        min: T,
        max: T,
        inclusive: Boolean = true,
        message: (min: T, max: T) -> String = { lo, hi -> "Must be a number between $lo and $hi." }
    ): ValidationRule<T?> where T : Number, T : Comparable<T> = ValidationRule { v ->
        if (v == null) return@ValidationRule FormicaFieldResult.NoInput
        val ok = if (inclusive) v >= min && v <= max else v > min && v < max
        if (ok) FormicaFieldResult.Success else FormicaFieldResult.Error(message(min, max))
    }

    fun range(
        min: Double,
        max: Double,
        inclusive: Boolean = true,
        epsilon: Double = 0.0,
        message: (Double, Double) -> String = { lo, hi -> "Must be a number between $lo and $hi." }
    ): ValidationRule<Double?> = ValidationRule { v ->
        if (v == null || v.isNaN()) return@ValidationRule FormicaFieldResult.NoInput
        val lower = if (inclusive) v >= min - epsilon else v > min + epsilon
        val upper = if (inclusive) v <= max + epsilon else v < max - epsilon
        if (lower && upper) FormicaFieldResult.Success else FormicaFieldResult.Error(
            message(
                min,
                max
            )
        )
    }

    fun range(
        min: Float,
        max: Float,
        inclusive: Boolean = true,
        epsilon: Float = 0f,
        message: (Float, Float) -> String = { lo, hi -> "Must be a number between $lo and $hi." }
    ): ValidationRule<Float?> = ValidationRule { v ->
        if (v == null || v.isNaN()) return@ValidationRule FormicaFieldResult.NoInput
        val lower = if (inclusive) v >= min - epsilon else v > min + epsilon
        val upper = if (inclusive) v <= max + epsilon else v < max - epsilon
        if (lower && upper) FormicaFieldResult.Success else FormicaFieldResult.Error(
            message(
                min,
                max
            )
        )
    }

    fun min(
        min: Int,
        inclusive: Boolean = true,
        message: (Int) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
    ): ValidationRule<Int?> = ValidationRule { v ->
        if (v == null) return@ValidationRule FormicaFieldResult.NoInput
        val ok = if (inclusive) v >= min else v > min
        if (ok) FormicaFieldResult.Success else FormicaFieldResult.Error(message(min))
    }

    fun max(
        max: Int,
        inclusive: Boolean = true,
        message: (Int) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
    ): ValidationRule<Int?> = ValidationRule { v ->
        if (v == null) return@ValidationRule FormicaFieldResult.NoInput
        val ok = if (inclusive) v <= max else v < max
        if (ok) FormicaFieldResult.Success else FormicaFieldResult.Error(message(max))
    }
    
    fun min(
        min: Long,
        inclusive: Boolean = true,
        message: (Long) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
    ): ValidationRule<Long?> = ValidationRule { v ->
        if (v == null) return@ValidationRule FormicaFieldResult.NoInput
        val ok = if (inclusive) v >= min else v > min
        if (ok) FormicaFieldResult.Success else FormicaFieldResult.Error(message(min))
    }

    fun max(
        max: Long,
        inclusive: Boolean = true,
        message: (Long) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
    ): ValidationRule<Long?> = ValidationRule { v ->
        if (v == null) return@ValidationRule FormicaFieldResult.NoInput
        val ok = if (inclusive) v <= max else v < max
        if (ok) FormicaFieldResult.Success else FormicaFieldResult.Error(message(max))
    }

    fun min(
        min: Double,
        inclusive: Boolean = true,
        epsilon: Double = 0.0,
        message: (Double) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
    ): ValidationRule<Double?> = ValidationRule { v ->
        if (v == null || v.isNaN()) return@ValidationRule FormicaFieldResult.NoInput
        val ok = if (inclusive) v >= min - epsilon else v > min + epsilon
        if (ok) FormicaFieldResult.Success else FormicaFieldResult.Error(message(min))
    }

    fun max(
        max: Double,
        inclusive: Boolean = true,
        epsilon: Double = 0.0,
        message: (Double) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
    ): ValidationRule<Double?> = ValidationRule { v ->
        if (v == null || v.isNaN()) return@ValidationRule FormicaFieldResult.NoInput
        val ok = if (inclusive) v <= max + epsilon else v < max - epsilon
        if (ok) FormicaFieldResult.Success else FormicaFieldResult.Error(message(max))
    }

    fun min(
        min: Float,
        inclusive: Boolean = true,
        epsilon: Float = 0f,
        message: (Float) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
    ): ValidationRule<Float?> = ValidationRule { v ->
        if (v == null || v.isNaN()) return@ValidationRule FormicaFieldResult.NoInput
        val ok = if (inclusive) v >= min - epsilon else v > min + epsilon
        if (ok) FormicaFieldResult.Success else FormicaFieldResult.Error(message(min))
    }

    fun max(
        max: Float,
        inclusive: Boolean = true,
        epsilon: Float = 0f,
        message: (Float) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
    ): ValidationRule<Float?> = ValidationRule { v ->
        if (v == null || v.isNaN()) return@ValidationRule FormicaFieldResult.NoInput
        val ok = if (inclusive) v <= max + epsilon else v < max - epsilon
        if (ok) FormicaFieldResult.Success else FormicaFieldResult.Error(message(max))
    }
}

private const val EMAIL_PATTERN = "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"

private const val DOMAIN_URL_PATTERN =
    "^[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]*\$"
private const val HTTP_URL_PATTERN =
    "^https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]*\$"
