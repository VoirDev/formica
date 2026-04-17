package dev.voir.formica.schema

/**
 * Cross-field validation rule.
 *
 * Return field-keyed errors or form-level errors as needed.
 */
data class ObjectRuleResult(
    val fieldErrors: Map<String, String> = emptyMap(),
    val formErrors: List<String> = emptyList()
)

fun interface ObjectValidationRule<Data : Any> {
    fun validate(data: Data): ObjectRuleResult
}
