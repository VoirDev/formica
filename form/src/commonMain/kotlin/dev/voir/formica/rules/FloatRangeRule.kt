package dev.voir.formica.rules

import dev.voir.formica.FormicaFieldResult

class FloatRangeRule(
    private val min: Float,
    private val max: Float,
    private val message: String? = null
) : ValidationRule<Float> {
    override fun validate(value: Float): FormicaFieldResult {
        return if ((value >= min) && (value <= max)) {
            FormicaFieldResult.Success
        } else {
            FormicaFieldResult.Error(message ?: "Must be a number between $min and $max.")
        }
    }
}
