package dev.voir.formica.core

import kotlin.reflect.KProperty1

/**
 * Pluggable validation contract.
 *
 * Your schema DSL can implement this.
 * Another library can implement this too.
 * Formica core only depends on this interface.
 */
interface FormicaValidationAdapter<Data : Any> {
    val fields: List<FormicaFieldDefinition<Data, *>>

    /**
     * Validate the whole data object.
     */
    fun validate(data: Data): FormicaValidationResult<Data>

    /**
     * Validate a single field by field key.
     */
    fun validateField(
        data: Data,
        fieldKey: String
    ): FormicaFieldValidationResult
}

/**
 * Find a field definition by property reference.
 */
fun <Data : Any, Value> FormicaValidationAdapter<Data>.findField(
    property: KProperty1<Data, Value>
): FormicaFieldDefinition<Data, Value>? {
    @Suppress("UNCHECKED_CAST")
    return fields.firstOrNull { it.property == property } as? FormicaFieldDefinition<Data, Value>
}
