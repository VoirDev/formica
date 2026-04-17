package dev.voir.formica.schema

import kotlin.reflect.KProperty1

class Schema<T : Any>(
    private val fields: List<FieldSchema<T, *>>
) {
    fun validate(instance: T): ValidationResult {
        val errors = buildList {
            for (field in fields) {
                @Suppress("UNCHECKED_CAST")
                addAll((field as FieldSchema<T, Any?>).validate(instance))
            }
        }
        return ValidationResult(errors)
    }

    fun <V> validateField(
        instance: T,
        property: KProperty1<T, V>
    ): List<FieldError> {
        val field = fields.firstOrNull { it.property == property }
            ?: error("Property '${property.name}' is not registered in schema")

        @Suppress("UNCHECKED_CAST")
        return (field as FieldSchema<T, V>).validate(instance)
    }
}
