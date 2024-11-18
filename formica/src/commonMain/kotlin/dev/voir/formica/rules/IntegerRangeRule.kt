package dev.voir.formica.rules

import dev.voir.formica.FormicaFieldResult

class IntegerRangeRule(
    private val min: Int,
    private val max: Int,
    private val message: String? = null
) : ValidationRule<Int?> {
    override fun validate(value: Int?): FormicaFieldResult {
        if (value == null) return FormicaFieldResult.NoInput

        return if ((value >= min) && (value <= max)) {
            FormicaFieldResult.Success
        } else {
            FormicaFieldResult.Error(message ?: "Must be a number between $min and $max.")
        }
    }
}
