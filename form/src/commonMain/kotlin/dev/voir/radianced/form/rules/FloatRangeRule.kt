package dev.voir.radianced.form.rules

import dev.voir.radianced.form.FormFieldResult

class FloatRangeRule(
    private val min: Float,
    private val max: Float,
    private val message: String? = null
) : ValidationRule<Float> {
    override fun validate(value: Float): FormFieldResult {
        return if ((value >= min) && (value <= max)) {
            FormFieldResult.Success
        } else {
            FormFieldResult.Error(message ?: "Must be a number between $min and $max.")
        }
    }
}
