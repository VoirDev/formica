package dev.voir.radianced.form.rules

import dev.voir.radianced.form.FormFieldResult

class IntegerRangeRule(
    private val min: Int,
    private val max: Int,
    private val message: String? = null
) : ValidationRule<Int> {
    override fun validate(value: Int): FormFieldResult {
        return if ((value >= min) && (value <= max)) {
            FormFieldResult.Success
        } else {
            FormFieldResult.Error(message ?: "Must be a number between $min and $max.")
        }
    }
}
