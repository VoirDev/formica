package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import dev.voir.formica.FormicaFieldResult
import dev.voir.formica.rules.ValidationRule
import dev.voir.formica.scopes.FormicaFieldScope
import dev.voir.formica.scopes.FormicaScope
import kotlin.reflect.KMutableProperty1

@Composable
fun <Data, Value : Any?> FormicaScope<Data>.FormicaField(
    name: KMutableProperty1<Data, Value>,
    required: Boolean = true,
    requiredError: String? = null,
    validators: Set<ValidationRule<Value?>> = emptySet(),
    customValidation: ((Value?) -> FormicaFieldResult)? = null,
    validateOnChange: Boolean = true,
    content: @Composable FormicaFieldScope<Value?>.() -> Unit
) {
    val scope = remember {
        derivedStateOf {
            FormicaFieldScope(
                formField = registerField(
                    name = name,
                    required = required,
                    requiredError = requiredError,
                    validators = validators,
                    customValidation = customValidation,
                    validateOnChange = validateOnChange
                )
            )
        }
    }

    scope.value.content()
}
