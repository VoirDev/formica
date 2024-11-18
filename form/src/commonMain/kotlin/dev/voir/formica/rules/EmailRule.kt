package dev.voir.formica.rules

import dev.voir.formica.FormFieldResult

class EmailRule(private val message: String? = null) : ValidationRule<String> {
    override fun validate(value: String): FormFieldResult {
        return if (value.matches(EMAIL_PATTERN.toRegex())) {
            FormFieldResult.Success
        } else {
            FormFieldResult.Error(message ?: "Must be a valid email address.")
        }
    }

    companion object {
        private const val EMAIL_PATTERN =
            "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    }
}
