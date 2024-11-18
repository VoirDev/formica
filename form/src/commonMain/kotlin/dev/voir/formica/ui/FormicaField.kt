package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import dev.voir.formica.FormicaField
import dev.voir.formica.rules.ValidationRule
import dev.voir.formica.scopes.FormicaFieldScope
import dev.voir.formica.scopes.FormicaScope
import kotlin.reflect.KMutableProperty1


@Composable
fun <Data, Value : Any?> FormicaScope<Data>.FormicaField(
    name: KMutableProperty1<Data, Value>,
    required: Boolean = true,
    requiredError: String? = null,
    validators: Set<ValidationRule<Value>> = emptySet(),
    customValidation: ((Data, Value) -> String?)? = null,
    validateOnInput: Boolean = true,
    content: @Composable FormicaFieldScope<Value>.() -> Unit
): FormicaField<Value> {
    val field =
        registerField(
            name = name,
            required = required,
            requiredError = requiredError,
            validators = validators,
            customValidation = customValidation,
            validateOnInput = validateOnInput
        )
    val scope = FormicaFieldScope(formField = field)
    scope.content()

    return field
}
