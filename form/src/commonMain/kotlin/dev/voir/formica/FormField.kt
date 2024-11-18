package dev.voir.formica

import dev.voir.formica.rules.ValidationRule
import kotlinx.coroutines.flow.MutableStateFlow

class FormicaField<Value : Any?>(
    initialValue: Value,
    private val required: Boolean,
    private val validators: Set<ValidationRule<Value>> = emptySet(),
    private val customValidation: ((Value) -> String?)? = null,
    private val validateOnInput: Boolean = true,
    private val requiredError: String? = null,
) {
    val value: MutableStateFlow<Value?> = MutableStateFlow(initialValue)
    val error: MutableStateFlow<String?> = MutableStateFlow(null)
    private val result: MutableStateFlow<FormFieldResult> =
        MutableStateFlow(FormFieldResult.NoInput)

    fun onInput(input: Value?) {
        value.value = input

        if (validateOnInput) {
            validate(input)
        }
    }

    fun isValid(): Boolean = validate(value.value) is FormFieldResult.Success

    private fun validate(input: Value?): FormFieldResult {
        // If field is optional and value is null
        if (!required && input == null) {
            return FormFieldResult.Success
        } else if (required && input == null) {
            val message = requiredError ?: "Field is required"
            val result = FormFieldResult.Error(message = message)

            this.result.value = result
            this.error.value = result.message

            return result
        }

        val validationResult = validators.map { it.validate(input!!) }
        var result =
            validationResult.find { it is FormFieldResult.Error } ?: FormFieldResult.Success

        val customValidation = customValidation?.let { it(input!!) }
        if (customValidation !== null) {
            result = FormFieldResult.Error(message = customValidation)
        }

        this.result.value = result
        if (result is FormFieldResult.Error) {
            this.error.value = result.message
        } else {
            this.error.value = null
        }

        return result
    }
}
