package dev.voir.radianced.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import dev.voir.radianced.form.FormField
import dev.voir.radianced.form.rules.ValidationRule
import kotlin.reflect.KMutableProperty1

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
