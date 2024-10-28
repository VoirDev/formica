package dev.voir.radianced.form.rules

import dev.voir.radianced.form.FormFieldResult

class StrongPasswordRule(
    private val minLength: Int = 8,
    private val lengthMessage: String? = null,
    private val uppercaseMessage: String? = null,
    private val lowercaseMessage: String? = null,
    private val digitMessage: String? = null,
    private val specialCharacterMessage: String? = null,
) :
    ValidationRule<String> {
    override fun validate(value: String): FormFieldResult {
        return when {
            value.length < minLength -> FormFieldResult.Error(
                lengthMessage ?: "Password must be at least $minLength characters long."
            )

            !value.any { it.isUpperCase() } -> FormFieldResult.Error(
                uppercaseMessage ?: "Password must contain at least one uppercase letter."
            )

            !value.any { it.isLowerCase() } -> FormFieldResult.Error(
                lowercaseMessage ?: "Password must contain at least one lowercase letter."
            )

            !value.any { it.isDigit() } -> FormFieldResult.Error(
                digitMessage ?: "Password must contain at least one digit."
            )

            !value.any { it in "!@#$%^&*()-_=+[]{};:'\",.<>?/|\\`~" } -> FormFieldResult.Error(
                specialCharacterMessage ?: "Password must contain at least one special character."
            )

            else -> FormFieldResult.Success
        }
    }
}
