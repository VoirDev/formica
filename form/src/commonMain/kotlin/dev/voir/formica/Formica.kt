package dev.voir.formica

import dev.voir.formica.rules.ValidationRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KMutableProperty1

sealed class FormicaState {
    data object NoInput : FormicaState()
    data object Valid : FormicaState()
    data class Error(val message: String) : FormicaState()
}

class Formica<Data>(
    val initialData: Data,
    private val onValid: ((Data) -> Unit)? = null,
    private val onInvalid: (() -> Unit)? = null
) {
    private val fields: MutableMap<KMutableProperty1<Data, *>, FormicaField<Any>> = mutableMapOf()

    val data: MutableStateFlow<Data> = MutableStateFlow(initialData)

    val state: MutableStateFlow<FormicaState> = MutableStateFlow(FormicaState.NoInput)

    fun validate() {
        // Validate all fields
        val errors = fields.map { field ->
            field.value.isValid()
        }

        // If all fields valid -> do submit
        if (errors.all { it }) {
            state.value = FormicaState.Valid
            onValid?.let { it(data.value) }
        } else {
            state.value = FormicaState.Error(message = "Some fields not valid")
            // TODO Pass invalid fields
            onInvalid?.let { it() }
        }
    }

    fun <Value : Any?> onChange(property: KMutableProperty1<Data, Value>, value: Value) {
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
