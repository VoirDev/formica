package dev.voir.formica.rules

import dev.voir.formica.FormicaFieldResult

class StrongPasswordRule(
    private val minLength: Int = 8,
    private val lengthMessage: String? = null,
    private val uppercaseMessage: String? = null,
    private val lowercaseMessage: String? = null,
    private val digitMessage: String? = null,
    private val specialCharacterMessage: String? = null,
) : ValidationRule<String?> {
    override fun validate(value: String?): FormicaFieldResult {
        if (value == null) return FormicaFieldResult.NoInput

        return when {
            value.length < minLength -> FormicaFieldResult.Error(
                lengthMessage ?: "Password must be at least $minLength characters long."
            )

            !value.any { it.isUpperCase() } -> FormicaFieldResult.Error(
                uppercaseMessage ?: "Password must contain at least one uppercase letter."
            )

            !value.any { it.isLowerCase() } -> FormicaFieldResult.Error(
                lowercaseMessage ?: "Password must contain at least one lowercase letter."
            )

            !value.any { it.isDigit() } -> FormicaFieldResult.Error(
                digitMessage ?: "Password must contain at least one digit."
            )

            !value.any { it in "!@#$%^&*()-_=+[]{};:'\",.<>?/|\\`~" } -> FormicaFieldResult.Error(
                specialCharacterMessage ?: "Password must contain at least one special character."
            )

            else -> FormicaFieldResult.Success
        }
    }
}
