package dev.voir.radianced.form.rules

import dev.voir.radianced.form.FormFieldResult

class IsCheckedRule(private val message: String? = null) : ValidationRule<Boolean> {
    override fun validate(value: Boolean): FormFieldResult {
        return if (value) {
            FormFieldResult.Success
        } else {
            FormFieldResult.Error(message ?: "Must be checked")
        }
    }
}
