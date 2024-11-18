package dev.voir.formica

import dev.voir.formica.rules.ValidationRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KMutableProperty1

class Formica<Data>(val initialData: Data, private val onSubmit: ((Data) -> Unit)? = null) {
    private val fields: MutableMap<KMutableProperty1<Data, *>, FormicaField<Any>> = mutableMapOf()

    val data: MutableStateFlow<Data> = MutableStateFlow(initialData)

    fun submit() {
        // Validate all fields
        val errors = fields.map { field ->
            field.value.isValid()
        }

        // If all fields valid -> do submit
        if (errors.all { it }) {
            onSubmit?.let { it(data.value) }
        }
    }

    fun <Value : Any?> onFormChange(property: KMutableProperty1<Data, Value>, value: Value) {
        val newData = data.value
        property.setValue(newData, property, value)
        data.value = newData
        fields[property]?.onInput(value)
    }

    fun <Value : Any?> registerField(
        name: KMutableProperty1<Data, Value>,
        required: Boolean,
        validators: Set<ValidationRule<Value>>,
        customValidation: ((Data, Value) -> String?)? = null,
        validateOnInput: Boolean = true,
        requiredError: String? = null,
    ): FormicaField<Value> {
        val field = FormicaField(
            initialValue = name.get(data.value),
            required = required,
            requiredError = requiredError,
            validators = validators,
            customValidation = {
                if (customValidation === null) return@FormicaField null
                return@FormicaField customValidation(data.value, it)
            },
            validateOnInput = validateOnInput
        )
        fields[name] = field as FormicaField<Any>

        return field
    }
}
