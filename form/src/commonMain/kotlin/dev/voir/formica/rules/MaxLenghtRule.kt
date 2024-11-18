package dev.voir.formica.rules

import dev.voir.formica.FormicaFieldResult

class MaxLengthRule(private val option: Int, private val message: String? = null) :
    ValidationRule<String> {
    override fun validate(value: String): FormicaFieldResult {
        return if (value.count() <= option) {
            FormicaFieldResult.Success
        } else {
            FormicaFieldResult.Error(message ?: "Must not exceed $option characters.")
        }
    }
}
