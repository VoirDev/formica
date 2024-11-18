package dev.voir.formica.scopes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import dev.voir.formica.Formica
import dev.voir.formica.FormicaField
import dev.voir.formica.FormicaFieldResult
import dev.voir.formica.rules.ValidationRule
import kotlin.reflect.KMutableProperty1

class FormicaScope<Data>(private val formica: Formica<Data>) {
    val data: Data
        get() = formica.data.value

    val state: State<Data>
        @Composable
        get() = formica.data.collectAsState(formica.initialData)

    fun validate() {
        formica.validate()
    }

    fun <Value : Any?> onChange(fieldName: KMutableProperty1<Data, Value>, value: Value) {
        formica.onChange(fieldName, value)
    }

    fun <Value : Any?> registerField(
        name: KMutableProperty1<Data, Value>,
        validators: Set<ValidationRule<Value?>>,
        required: Boolean,
        requiredError: String? = null,
        customValidation: ((Value?) -> FormicaFieldResult)? = null,
        validateOnChange: Boolean = true,
    ): FormicaField<Value?> =
        formica.registerField(
            name = name,
            required = required,
            requiredError = requiredError,
            validators = validators,
            customValidation = customValidation,
            validateOnChange = validateOnChange
        )
}
