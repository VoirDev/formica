package dev.voir.formica.schema

import  dev.voir.formica.core.FormicaFieldDefinition
import  dev.voir.formica.core.FormicaFieldValidationResult
import  dev.voir.formica.core.FormicaValidationAdapter
import dev.voir.formica.core.FormicaValidationResult

/**
 * Default schema implementation for Formica.
 *
 * Validation strategy:
 * - field rules run in declared order
 * - first field error wins for that field
 * - object rules run after field rules
 */
class Schema<Data : Any>(
    private val schemaFields: List<SchemaField<Data, *>>,
    private val objectRules: List<ObjectValidationRule<Data>> = emptyList()
) : FormicaValidationAdapter<Data> {

    override val fields: List<FormicaFieldDefinition<Data, *>>
        get() = schemaFields

    override fun validate(data: Data): FormicaValidationResult<Data> {
        val fieldErrors = linkedMapOf<String, String>()

        for (field in schemaFields) {
            @Suppress("UNCHECKED_CAST")
            val typed = field as SchemaField<Data, Any?>

            val result = validateRules(
                value = typed.property.get(data),
                rules = typed.rules
            )

            if (result is FieldValidationResult.Error) {
                fieldErrors[typed.key] = result.message
            }
        }

        val formErrors = mutableListOf<String>()

        for (rule in objectRules) {
            val result = rule.validate(data)
            for ((key, message) in result.fieldErrors) {
                if (!fieldErrors.containsKey(key)) {
                    fieldErrors[key] = message
                }
            }
            formErrors += result.formErrors
        }

        return FormicaValidationResult(
            data = data,
            fieldErrors = fieldErrors,
            formErrors = formErrors
        )
    }

    override fun validateField(
        data: Data,
        fieldKey: String
    ): FormicaFieldValidationResult {
        val field = schemaFields.firstOrNull { it.key == fieldKey }
            ?: return FormicaFieldValidationResult.Skip

        @Suppress("UNCHECKED_CAST")
        val typed = field as SchemaField<Data, Any?>

        return when (
            val result = validateRules(
                value = typed.property.get(data),
                rules = typed.rules
            )
        ) {
            FieldValidationResult.Success -> FormicaFieldValidationResult.Success
            FieldValidationResult.Skip -> FormicaFieldValidationResult.Skip
            is FieldValidationResult.Error -> FormicaFieldValidationResult.Error(result.message)
        }
    }

    private fun <T> validateRules(
        value: T,
        rules: List<ValidationRule<T>>
    ): FieldValidationResult {
        for (rule in rules) {
            when (val result = rule.validate(value)) {
                is FieldValidationResult.Error -> return result
                else -> Unit
            }
        }
        return FieldValidationResult.Success
    }
}
