package dev.voir.formica.rules

import dev.voir.formica.FormicaFieldResult

class NotEmptyRule(private val message: String? = null) :
    ValidationRule<String> {
    override fun validate(value: String): FormicaFieldResult {
        return if (value.isNotEmpty()) {
            FormicaFieldResult.Success
        } else {
            FormicaFieldResult.Error(message ?: "This field cannot be empty.")
        }
    }
}
