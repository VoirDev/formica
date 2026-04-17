package dev.voir.formica.schema

import kotlin.reflect.KProperty1

class FieldSchema<T : Any, V>(
    val property: KProperty1<T, V>,
    private val rules: List<ValidationRule<V>>
) {
    val name: String get() = property.name

    fun validate(instance: T): List<FieldError> {
        val value = property.get(instance)

        return buildList {
            for (rule in rules) {
                when (val result = rule.validate(value)) {
                    FieldValidationResult.Success,
                    FieldValidationResult.Skip -> Unit

                    is FieldValidationResult.Error ->
                        add(FieldError(name, result.message))
                }
            }
        }
    }
}
