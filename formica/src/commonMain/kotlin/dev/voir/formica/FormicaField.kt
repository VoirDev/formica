package dev.voir.formica

import dev.voir.formica.rules.ValidationRule
import kotlinx.coroutines.flow.MutableStateFlow

sealed class FormicaFieldResult {
    data object Success : FormicaFieldResult()

    data class Error(val message: String) : FormicaFieldResult()

    data object NoInput : FormicaFieldResult()
}

class FormicaField<Value : Any?>(
    initialValue: Value,
    private val required: Boolean,
    private val validators: Set<ValidationRule<Value>> = emptySet(),
    private val customValidation: ((Value?) -> FormicaFieldResult)? = null,
    private val validateOnChange: Boolean = true,
    private val requiredError: String? = null,
) {
    val value: MutableStateFlow<Value?> = MutableStateFlow(initialValue)
    val error: MutableStateFlow<String?> = MutableStateFlow(null)
    private val result: MutableStateFlow<FormicaFieldResult> =
        MutableStateFlow(FormicaFieldResult.NoInput)

    /**
     * Update field value with new one
     *
     * @param input New field value
     */
    fun onChange(input: Value?) {
        value.value = input

        if (validateOnChange) {
            validate(input)
        }
    }

    /**
     * Validate field
     *
     * @return true if field is valid, otherwise false
     */
    fun isValid(): Boolean = validate(value.value) is FormicaFieldResult.Success

    private fun validate(input: Value?): FormicaFieldResult {
        if (customValidation != null) {
            val result = customValidation.invoke(input)
            this.result.value = result
            if (result is FormicaFieldResult.Error) {
                this.error.value = result.message
            } else {
                this.error.value = null
            }
            return result
        }

        // If field is optional and value is null, than everything fine
        // If field is required and value is null, than show required error
        if (!required && input == null) {
            this.result.value = FormicaFieldResult.Success
            this.error.value = null
            return FormicaFieldResult.Success
        } else if (required && input == null) {
            val message = requiredError ?: "Field is required"
            val result = FormicaFieldResult.Error(message = message)

            this.result.value = result
            this.error.value = result.message

            return result
        }

        // Run Validators
        val validationResult = validators.map { it.validate(input!!) }
        val result =
            validationResult.find { it is FormicaFieldResult.Error } ?: FormicaFieldResult.Success

        this.result.value = result
        if (result is FormicaFieldResult.Error) {
            this.error.value = result.message
        } else {
            this.error.value = null
        }

        return result
    }
}
