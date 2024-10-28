package dev.voir.radianced.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
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
