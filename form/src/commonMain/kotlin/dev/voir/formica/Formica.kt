package dev.voir.formica

import dev.voir.formica.rules.ValidationRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KMutableProperty1

sealed class FormicaState {
    data object NoInput : FormicaState()
    data object Valid : FormicaState()
    data class Error(val message: String) : FormicaState()
}

class Formica<Data>(val initialData: Data) {
    private val fields: MutableMap<KMutableProperty1<Data, *>, FormicaField<Any>> = mutableMapOf()

    val data: MutableStateFlow<Data> = MutableStateFlow(initialData)

    val state: MutableStateFlow<FormicaState> = MutableStateFlow(FormicaState.NoInput)

    fun validate(): FormicaState {
        val errors = fields.map { field ->
            field.value.isValid()
        }
        val newState = if (errors.all { it }) {
            FormicaState.Valid
        } else {
            // TODO Pass invalid fields
            FormicaState.Error(message = "Some fields not valid")
        }

        state.value = newState
        return newState
    }

    fun <Value : Any?> onChange(property: KMutableProperty1<Data, Value>, value: Value) {
        val newData = data.value
        property.setValue(newData, property, value)
        data.value = newData
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
