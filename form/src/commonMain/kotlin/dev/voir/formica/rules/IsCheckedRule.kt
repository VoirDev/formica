package dev.voir.formica.rules

import dev.voir.formica.FormicaFieldResult

class IsCheckedRule(private val message: String? = null) : ValidationRule<Boolean> {
    override fun validate(value: Boolean): FormicaFieldResult {
        return if (value) {
            FormicaFieldResult.Success
        } else {
            FormicaFieldResult.Error(message ?: "Must be checked")
        }
    }
}
