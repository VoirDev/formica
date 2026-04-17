package dev.voir.formica.schema

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.required(
    message: String = "Field is required",
    isEmpty: (Value?) -> Boolean = { value ->
        when (value) {
            null -> true
            is String -> value.isBlank()
            is Collection<*> -> value.isEmpty()
            else -> false
        }
    }
) {
    rule(ValidationRules.required(message, isEmpty) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.validateOnlyIf(
    active: () -> Boolean,
    rule: ValidationRule<Value?>
) {
    this.rule(ValidationRules.validateOnlyIf(active, rule) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.notEmpty(
    message: String = "This field cannot be empty."
) {
    rule(ValidationRules.notEmpty(message) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.notBlank(
    message: String = "This field cannot be blank."
) {
    rule(ValidationRules.notBlank(message) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.email(
    message: String = "Must be a valid email address.",
    pattern: Regex = Regex(EMAIL_PATTERN)
) {
    rule(ValidationRules.email(message, pattern) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.strongPassword(
    minLength: Int = 8,
    lengthMessage: String = "Password must be at least $minLength characters long.",
    uppercaseMessage: String = "Password must contain at least one uppercase letter.",
    lowercaseMessage: String = "Password must contain at least one lowercase letter.",
    digitMessage: String = "Password must contain at least one digit.",
    specialCharacterMessage: String = "Password must contain at least one special character.",
) {
    rule(
        ValidationRules.strongPassword(
            minLength = minLength,
            lengthMessage = lengthMessage,
            uppercaseMessage = uppercaseMessage,
            lowercaseMessage = lowercaseMessage,
            digitMessage = digitMessage,
            specialCharacterMessage = specialCharacterMessage
        ) as ValidationRule<Value?>
    )
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.url(
    protocolRequired: Boolean = false,
    message: String = "Must be a valid URL."
) {
    rule(ValidationRules.url(protocolRequired, message) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.checked(
    message: String = "Must be checked"
) {
    rule(ValidationRules.checked(message) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.minLength(
    option: Int,
    message: String = "Must be at least $option characters long."
) {
    rule(ValidationRules.minLength(option, message) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.maxLength(
    option: Int,
    message: String = "Must not exceed $option characters."
) {
    rule(ValidationRules.maxLength(option, message) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value, T> SchemaFieldBuilder<Data, Value>.range(
    min: T,
    max: T,
    inclusive: Boolean = true,
    message: (T, T) -> String = { lo, hi -> "Must be a number between $lo and $hi." }
) where T : Number, T : Comparable<T> {
    rule(ValidationRules.range(min, max, inclusive, message) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.range(
    min: Double,
    max: Double,
    inclusive: Boolean = true,
    epsilon: Double = 0.0,
    message: (Double, Double) -> String = { lo, hi -> "Must be a number between $lo and $hi." }
) {
    rule(ValidationRules.range(min, max, inclusive, epsilon, message) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any, Value> SchemaFieldBuilder<Data, Value>.range(
    min: Float,
    max: Float,
    inclusive: Boolean = true,
    epsilon: Float = 0f,
    message: (Float, Float) -> String = { lo, hi -> "Must be a number between $lo and $hi." }
) {
    rule(ValidationRules.range(min, max, inclusive, epsilon, message) as ValidationRule<Value?>)
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any> SchemaFieldBuilder<Data, Int?>.min(
    min: Int,
    inclusive: Boolean = true,
    message: (Int) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
) {
    rule(ValidationRules.min(min, inclusive, message))
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any> SchemaFieldBuilder<Data, Int?>.max(
    max: Int,
    inclusive: Boolean = true,
    message: (Int) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
) {
    rule(ValidationRules.max(max, inclusive, message))
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any> SchemaFieldBuilder<Data, Long?>.min(
    min: Long,
    inclusive: Boolean = true,
    message: (Long) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
) {
    rule(ValidationRules.min(min, inclusive, message))
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any> SchemaFieldBuilder<Data, Long?>.max(
    max: Long,
    inclusive: Boolean = true,
    message: (Long) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
) {
    rule(ValidationRules.max(max, inclusive, message))
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any> SchemaFieldBuilder<Data, Double?>.min(
    min: Double,
    inclusive: Boolean = true,
    epsilon: Double = 0.0,
    message: (Double) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
) {
    rule(ValidationRules.min(min, inclusive, epsilon, message))
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any> SchemaFieldBuilder<Data, Double?>.max(
    max: Double,
    inclusive: Boolean = true,
    epsilon: Double = 0.0,
    message: (Double) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
) {
    rule(ValidationRules.max(max, inclusive, epsilon, message))
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any> SchemaFieldBuilder<Data, Float?>.min(
    min: Float,
    inclusive: Boolean = true,
    epsilon: Float = 0f,
    message: (Float) -> String = { "Must be ${if (inclusive) ">=" else ">"} $it." }
) {
    rule(ValidationRules.min(min, inclusive, epsilon, message))
}

@Suppress("UNCHECKED_CAST")
fun <Data : Any> SchemaFieldBuilder<Data, Float?>.max(
    max: Float,
    inclusive: Boolean = true,
    epsilon: Float = 0f,
    message: (Float) -> String = { "Must be ${if (inclusive) "<=" else "<"} $it." }
) {
    rule(ValidationRules.max(max, inclusive, epsilon, message))
}
