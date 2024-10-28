package dev.voir.radianced.form.rules

import dev.voir.radianced.form.FormFieldResult

class MaxLengthRule(private val option: Int, private val message: String? = null) :
    ValidationRule<String> {
    override fun validate(value: String): FormFieldResult {
        return if (value.count() <= option) {
            FormFieldResult.Success
        } else {
            FormFieldResult.Error(message ?: "Must not exceed $option characters.")
        }
    }
}
