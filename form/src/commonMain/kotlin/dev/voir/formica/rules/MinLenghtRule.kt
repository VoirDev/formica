package dev.voir.formica.rules

import dev.voir.formica.FormicaFieldResult

class MinLengthRule(
    private val option: Int,
    private val message: String? = null
) : ValidationRule<String?> {
    override fun validate(value: String?): FormicaFieldResult {
        if (value == null) return FormicaFieldResult.NoInput

        return if (value.count() >= option) {
            FormicaFieldResult.Success
        } else {
            FormicaFieldResult.Error(message ?: "Must be at least ${this.option} characters long.")
        }
    }
}
