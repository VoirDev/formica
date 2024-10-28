package dev.voir.radianced.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import dev.voir.radianced.form.rules.ValidationRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KMutableProperty1

class FormField<Value : Any?>(
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

class FormFieldScope<V : Any?>(
    private val formField: FormField<V>,
) {
    val field: State<V?>
        @Composable
        get() = this.formField.value.collectAsState(null)

    val error: State<String?>
        @Composable
        get() = this.formField.error.collectAsState(null)
}

@Composable
fun <Data, Value : Any?> FormScope<Data>.formField(
    name: KMutableProperty1<Data, Value>,
    required: Boolean = true,
    requiredError: String? = null,
    validators: Set<ValidationRule<Value>> = emptySet(),
    customValidation: ((Data, Value) -> String?)? = null,
    validateOnInput: Boolean = true,
    content: @Composable FormFieldScope<Value>.() -> Unit
): FormField<Value> {
    val field =
        registerField(
            name = name,
            required = required,
            requiredError = requiredError,
            validators = validators,
            customValidation = customValidation,
            validateOnInput = validateOnInput
        )
    val scope = FormFieldScope(formField = field)
    scope.content()

    return field
}
