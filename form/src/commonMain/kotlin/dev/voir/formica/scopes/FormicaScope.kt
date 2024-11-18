package dev.voir.formica.scopes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import dev.voir.formica.Formica
import dev.voir.formica.FormicaField
import dev.voir.formica.rules.ValidationRule
import kotlin.reflect.KMutableProperty1

class FormicaScope<Data>(private val f: Formica<Data>) {
    val data: Data
        get() = f.data.value

    val form: State<Data>
        @Composable
        get() = f.data.collectAsState(f.initialData)

    fun validate() {
        f.validate()
    }

    fun <Value : Any?> onChange(fieldName: KMutableProperty1<Data, Value>, value: Value) {
        f.onChange(fieldName, value)
    }

    fun <Value : Any?> registerField(
        name: KMutableProperty1<Data, Value>,
        validators: Set<ValidationRule<Value>>,
        required: Boolean,
        requiredError: String? = null,
        customValidation: ((Data, Value) -> String?)? = null,
        validateOnInput: Boolean = true,
    ): FormicaField<Value> =
        f.registerField(
            name = name,
            required = required,
            requiredError = requiredError,
            validators = validators,
            customValidation = customValidation,
            validateOnInput = validateOnInput
        )
}
