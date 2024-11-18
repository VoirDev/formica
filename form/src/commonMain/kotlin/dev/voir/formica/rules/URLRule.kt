package dev.voir.formica.rules

import dev.voir.formica.FormicaFieldResult

class WebUrlRule(
    private val protocolRequired: Boolean = false,
    private val message: String? = null
) : ValidationRule<String> {
    override fun validate(value: String): FormicaFieldResult {
        val result = if (protocolRequired) {
            value.matches(HTTP_URL_PATTERN.toRegex())
        } else {
            value.matches(HTTP_URL_PATTERN.toRegex()) || value.matches(DOMAIN_URL_PATTERN.toRegex())
        }
        return if (result) {
            FormicaFieldResult.Success
        } else {
            FormicaFieldResult.Error(message ?: "Must be a valid URL.")
        }
    }

    companion object {
        private const val DOMAIN_URL_PATTERN =
            "^[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]*\$"
        private const val HTTP_URL_PATTERN =
            "^https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]*\$"
    }
}
