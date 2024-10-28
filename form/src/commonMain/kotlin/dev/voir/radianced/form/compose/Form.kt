package dev.voir.radianced.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import dev.voir.radianced.form.Form
import dev.voir.radianced.form.FormField
import dev.voir.radianced.form.rules.ValidationRule
import kotlin.reflect.KMutableProperty1

class FormScope<Data>(private val f: Form<Data>) {
    val data: Data
        get() = f.data.value

    val form: State<Data>
        @Composable
        get() = f.data.collectAsState(f.initialData)

    fun submit() {
        f.submit()
    }

    fun <Value : Any?> onFormChange(fieldName: KMutableProperty1<Data, Value>, value: Value) {
        f.onFormChange(fieldName, value)
    }

    fun <Value : Any?> registerField(
        name: KMutableProperty1<Data, Value>,
        validators: Set<ValidationRule<Value>>,
        required: Boolean,
        requiredError: String? = null,
        customValidation: ((Data, Value) -> String?)? = null,
        validateOnInput: Boolean = true,
    ): FormField<Value> =
        f.registerField(
            name = name,
            required = required,
            requiredError = requiredError,
            validators = validators,
            customValidation = customValidation,
            validateOnInput = validateOnInput
        )
}

@Composable
fun <Data : Any> form(
    initialData: Data,
    onSubmit: ((Data) -> Unit)? = null,
    content: @Composable FormScope<Data>.() -> Unit
): Form<Data> {
    val form = remember { Form(initialData = initialData, onSubmit = onSubmit) }
    val scope = remember { derivedStateOf { FormScope(f = form) } }
    scope.value.content()
    return form
}
