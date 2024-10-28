package dev.voir.radianced.form

import dev.voir.radianced.form.rules.ValidationRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KMutableProperty1

class Form<Data>(val initialData: Data, private val onSubmit: ((Data) -> Unit)? = null) {
    private val fields: MutableMap<KMutableProperty1<Data, *>, FormField<Any>> = mutableMapOf()

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
    ): FormField<Value> {
        val field = FormField(
            initialValue = name.get(data.value),
            required = required,
            requiredError = requiredError,
            validators = validators,
            customValidation = {
                if (customValidation === null) return@FormField null
                return@FormField customValidation(data.value, it)
            },
            validateOnInput = validateOnInput
        )
        fields[name] = field as FormField<Any>

        return field
    }
}
