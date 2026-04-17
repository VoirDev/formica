package dev.voir.formica.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import dev.voir.formica.core.Formica
import kotlin.reflect.KProperty1

@Composable
fun <Data : Any> Formica(
    formica: Formica<Data>,
    content: @Composable FormicaScope<Data>.(FormicaController<Data>) -> Unit
) {
    val data = formica.data.collectAsState().value
    val isDirty = formica.isDirty.collectAsState().value
    val isTouched = formica.isTouched.collectAsState().value
    val hasErrors = formica.hasErrors.collectAsState().value
    val canSubmit = formica.canSubmit.collectAsState().value
    val fieldErrors = formica.fieldErrors.collectAsState().value
    val formErrors = formica.formErrors.collectAsState().value
    val submitResult = formica.submitResult.collectAsState().value

    val formController = remember(
        data,
        isDirty,
        isTouched,
        hasErrors,
        canSubmit,
        fieldErrors,
        formErrors,
        submitResult,
        formica
    ) {
        FormicaController(
            data = data,
            isDirty = isDirty,
            isTouched = isTouched,
            hasErrors = hasErrors,
            canSubmit = canSubmit,
            fieldErrors = fieldErrors,
            formErrors = formErrors,
            submitResult = submitResult,
            validate = { formica.validate() },
            submit = { formica.submit() },
            reset = { formica.reset() },
            replaceData = { formica.replaceData(it) }
        )
    }

    val scope = remember(formica) {
        FormicaScopeImpl(formica)
    }

    @Suppress("UNCHECKED_CAST")
    CompositionLocalProvider(LocalFormica provides (formica as Formica<Any>)) {
        scope.content(formController)
    }
}

private class FormicaScopeImpl<Data : Any>(
    private val formica: Formica<Data>
) : FormicaScope<Data> {

    @Composable
    override fun <Value> Field(
        property: KProperty1<Data, Value>,
        content: @Composable (FormicaFieldController<Value>) -> Unit
    ) {
        val controller = field(property)
        if (controller.visible) {
            content(controller)
        }
    }

    @Composable
    override fun <Value> field(
        property: KProperty1<Data, Value>
    ): FormicaFieldController<Value> {
        val snapshot = formica.fieldSnapshot(property)
        val state = formica.fieldState(property)

        val value = state.value.collectAsState().value
        val error = state.error.collectAsState().value
        val validationResult = state.validationResult.collectAsState().value
        val touched = state.touched.collectAsState().value
        val dirty = state.dirty.collectAsState().value
        val visible = state.visible.collectAsState().value
        val enabled = state.enabled.collectAsState().value

        return remember(
            snapshot.key,
            value,
            error,
            validationResult,
            touched,
            dirty,
            visible,
            enabled,
            formica,
            property
        ) {
            FormicaFieldController(
                key = snapshot.key,
                value = value,
                error = error,
                validationResult = validationResult,
                touched = touched,
                dirty = dirty,
                visible = visible,
                enabled = enabled,
                showError = touched && error != null,
                onChange = { formica.onChange(property, it) },
                validate = { formica.validateField(property) },
                clear = { formica.clear(property) }
            )
        }
    }
}
