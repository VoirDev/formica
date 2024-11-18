package dev.voir.formica

import dev.voir.formica.rules.ValidationRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KMutableProperty1

sealed class FormicaResult {
    data object NoInput : FormicaResult()
    data object Valid : FormicaResult()
    data class Error(val message: String) : FormicaResult()
}

class Formica<Data>(val initialData: Data, private val onSubmit: ((Data) -> Unit)? = null) {
    private val fields: MutableMap<KMutableProperty1<Data, *>, FormicaField<Any>> = mutableMapOf()

    private val _data: MutableStateFlow<Data> = MutableStateFlow(initialData)
    val data: StateFlow<Data>
        get() = _data

    private val _result: MutableStateFlow<FormicaResult> = MutableStateFlow(FormicaResult.NoInput)
    val result: StateFlow<FormicaResult>
        get() = _result

    fun validate(): FormicaResult {
        val errors = fields.map { field ->
            field.value.isValid()
        }
        val newState = if (errors.all { it }) {
            FormicaResult.Valid
        } else {
            // TODO Pass information about invalid fields
            FormicaResult.Error(message = "Some fields not valid")
        }

        _result.value = newState
        return newState
    }

    fun submit() {
        val result = validate()
        if (result is FormicaResult.Valid) {
            onSubmit?.let { it(data.value) }
        }
    }

    fun <Value : Any?> onChange(property: KMutableProperty1<Data, Value>, value: Value) {
        val newData = data.value
        property.setValue(newData, property, value)
        _data.value = newData
        fields[property]?.onChange(value)
    }

    fun <Value : Any?> registerField(
        name: KMutableProperty1<Data, Value>,
        required: Boolean,
        validators: Set<ValidationRule<Value?>>,
        customValidation: ((Value?) -> FormicaFieldResult)? = null,
        validateOnChange: Boolean = true,
        requiredError: String? = null,
    ): FormicaField<Value?> {
        val field = FormicaField(
            initialValue = name.get(data.value),
            required = required,
            requiredError = requiredError,
            validators = validators,
            customValidation = customValidation,
            validateOnChange = validateOnChange
        )
        fields[name] = field as FormicaField<Any>

        return field
    }
}
