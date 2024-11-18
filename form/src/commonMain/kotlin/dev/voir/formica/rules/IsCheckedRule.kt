package dev.voir.formica.rules

import dev.voir.formica.FormFieldResult

class IsCheckedRule(private val message: String? = null) : ValidationRule<Boolean> {
    override fun validate(value: Boolean): FormFieldResult {
        return if (value) {
            FormFieldResult.Success
        } else {
            FormFieldResult.Error(message ?: "Must be checked")
        }
    }
}
