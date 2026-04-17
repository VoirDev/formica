package dev.voir.formica.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KProperty1

/**
 * Core form state container.
 *
 * Responsibilities:
 * - owns current immutable data snapshot
 * - owns reactive runtime state for each field
 * - delegates validation to the provided adapter
 * - exposes typed field access and immutable updates
 *
 * Non-responsibilities:
 * - does not define validation rules
 * - does not depend on Compose
 */
class Formica<Data : Any>(
    private val adapter: FormicaValidationAdapter<Data>,
    initialData: Data,
    private val onSubmit: ((Data) -> Unit)? = null
) {
    private val initialDataSnapshot: Data = initialData

    private val definitionsByKey: Map<String, FormicaFieldDefinition<Data, *>> =
        adapter.fields.associateBy { it.key }

    private val definitionsByProperty: Map<KProperty1<Data, *>, FormicaFieldDefinition<Data, *>> =
        adapter.fields.associateBy { it.property }

    private val fieldStates: Map<String, FormicaFieldState<Any?>> =
        adapter.fields.associate { field ->
            val visible = field.isVisible(initialData)
            val enabled = field.isEnabled(initialData)

            @Suppress("UNCHECKED_CAST")
            val typedField = field as FormicaFieldDefinition<Data, Any?>

            field.key to FormicaFieldState(
                initialValue = typedField.property.get(initialData),
                initialVisible = visible,
                initialEnabled = enabled
            )
        }

    private val _data = MutableStateFlow(initialData)
    val data: StateFlow<Data> get() = _data

    private val _submitResult = MutableStateFlow<FormicaSubmitResult>(FormicaSubmitResult.NoAttempt)
    val submitResult: StateFlow<FormicaSubmitResult> get() = _submitResult

    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> get() = _isDirty

    private val _isTouched = MutableStateFlow(false)
    val isTouched: StateFlow<Boolean> get() = _isTouched

    private val _hasErrors = MutableStateFlow(false)
    val hasErrors: StateFlow<Boolean> get() = _hasErrors

    private val _canSubmit = MutableStateFlow(false)
    val canSubmit: StateFlow<Boolean> get() = _canSubmit

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> get() = _fieldErrors

    private val _formErrors = MutableStateFlow<List<String>>(emptyList())
    val formErrors: StateFlow<List<String>> get() = _formErrors

    init {
        refreshAvailability()
        refreshDerivedState()
    }

    /**
     * Typed access to a field state by property reference.
     */
    fun <Value> fieldState(property: KProperty1<Data, Value>): FormicaFieldState<Value> {
        val definition = findDefinition(property)
        val state = fieldStates[definition.key]
            ?: error("Missing field state for key '${definition.key}'")

        @Suppress("UNCHECKED_CAST")
        return state as FormicaFieldState<Value>
    }

    /**
     * Access a field snapshot by property reference.
     */
    fun <Value> fieldSnapshot(property: KProperty1<Data, Value>): FormicaFieldSnapshot<Value> {
        val definition = findDefinition(property)
        val state = fieldState(property)
        return FormicaFieldSnapshot(
            key = definition.key,
            value = state.value.value,
            error = state.error.value,
            validationResult = state.validationResult.value,
            touched = state.touched.value,
            dirty = state.dirty.value,
            visible = state.visible.value,
            enabled = state.enabled.value
        )
    }

    /**
     * Apply immutable field update + runtime field state update.
     *
     * If validateOnChange is enabled for the field, field validation runs immediately.
     */
    fun <Value> onChange(property: KProperty1<Data, Value>, value: Value?) {
        val definition = findDefinition(property)
        val state = fieldState(property)

        state.onChange(value)

        _data.value = definition.set(_data.value, value)

        refreshAvailability()

        if (definition.validateOnChange) {
            validateField(property)
        } else {
            state.clearValidation()
        }

        refreshDerivedState()
    }

    /**
     * Clear a field using the field definition's clear function, if present.
     */
    fun <Value> clear(property: KProperty1<Data, Value>) {
        val definition = findDefinition(property)
        val clearFn = definition.clear ?: return

        _data.value = clearFn(_data.value)

        @Suppress("UNCHECKED_CAST")
        val typedDefinition = definition as FormicaFieldDefinition<Data, Value>

        val newValue = typedDefinition.property.get(_data.value)
        val state = fieldState(property)
        state.onChange(newValue)

        refreshAvailability()

        if (definition.validateOnChange) {
            validateField(property)
        } else {
            state.clearValidation()
        }

        refreshDerivedState()
    }

    /**
     * Validate a single field through the adapter.
     *
     * Hidden/disabled fields are treated as skipped.
     */
    fun <Value> validateField(property: KProperty1<Data, Value>): FormicaFieldValidationResult {
        val definition = findDefinition(property)
        val state = fieldState(property)

        val updatedFieldErrors = _fieldErrors.value.toMutableMap()

        if (!state.visible.value || !state.enabled.value) {
            state.setValidationResult(FormicaFieldValidationResult.Skip)
            updatedFieldErrors.remove(definition.key)
            _fieldErrors.value = updatedFieldErrors
            refreshDerivedState()
            return FormicaFieldValidationResult.Skip
        }

        val result = adapter.validateField(_data.value, definition.key)
        state.setValidationResult(result)

        when (result) {
            is FormicaFieldValidationResult.Error -> {
                updatedFieldErrors[definition.key] = result.message
            }

            else -> {
                updatedFieldErrors.remove(definition.key)
            }
        }

        _fieldErrors.value = updatedFieldErrors
        refreshDerivedState()
        return result
    }

    /**
     * Validate the whole form through the adapter.
     *
     * Also synchronizes field-level error state with adapter results.
     */
    fun validate(): FormicaValidationResult<Data> {
        val rawValidation = adapter.validate(_data.value)

        val filteredFieldErrors = buildMap<String, String> {
            for (field in adapter.fields) {
                val state = fieldStates[field.key] ?: continue
                val visible = state.visible.value
                val enabled = state.enabled.value

                if (!visible || !enabled) {
                    state.setValidationResult(FormicaFieldValidationResult.Skip)
                    continue
                }

                val error = rawValidation.fieldErrors[field.key]
                if (error == null) {
                    state.setValidationResult(FormicaFieldValidationResult.Success)
                } else {
                    state.setValidationResult(FormicaFieldValidationResult.Error(error))
                    put(field.key, error)
                }
            }
        }

        val validation = FormicaValidationResult(
            data = rawValidation.data,
            fieldErrors = filteredFieldErrors,
            formErrors = rawValidation.formErrors
        )

        _fieldErrors.value = validation.fieldErrors
        _formErrors.value = validation.formErrors
        _submitResult.value = if (validation.isValid) {
            FormicaSubmitResult.Success
        } else {
            FormicaSubmitResult.Error(
                fieldErrors = validation.fieldErrors,
                formErrors = validation.formErrors
            )
        }

        refreshDerivedState()
        return validation
    }

    /**
     * Validate and call submit callback if valid.
     */
    fun submit(): FormicaValidationResult<Data> {
        val validation = validate()
        if (validation.isValid) {
            onSubmit?.invoke(validation.data)
        }
        return validation
    }

    /**
     * Reset the form to a provided snapshot or back to the initial one.
     */
    fun reset(to: Data = initialDataSnapshot) {
        _data.value = to

        for (field in adapter.fields) {
            @Suppress("UNCHECKED_CAST")
            val typedField = field as FormicaFieldDefinition<Data, Any?>
            val state = fieldStates[field.key] ?: continue

            state.reset(
                newInitial = typedField.property.get(to),
                visible = field.isVisible(to),
                enabled = field.isEnabled(to)
            )
        }

        _fieldErrors.value = emptyMap()
        _formErrors.value = emptyList()
        _submitResult.value = FormicaSubmitResult.NoAttempt
        refreshDerivedState()
    }

    /**
     * Replace current data snapshot and mark the current values as pristine.
     *
     * Useful when loading a draft or replacing form data from an external source.
     */
    fun replaceData(data: Data) {
        reset(data)
    }

    /**
     * Create an immutable snapshot of current form state.
     */
    fun snapshot(): FormicaFormSnapshot<Data> =
        FormicaFormSnapshot(
            data = _data.value,
            isDirty = _isDirty.value,
            isTouched = _isTouched.value,
            hasErrors = _hasErrors.value,
            canSubmit = _canSubmit.value,
            submitResult = _submitResult.value,
            fieldErrors = _fieldErrors.value,
            formErrors = _formErrors.value
        )

    private fun refreshAvailability() {
        val current = _data.value

        for (field in adapter.fields) {
            val state = fieldStates[field.key] ?: continue
            state.updateAvailability(
                visible = field.isVisible(current),
                enabled = field.isEnabled(current)
            )
        }
    }

    private fun refreshDerivedState() {
        val states = fieldStates.values

        val isDirtyNow = states.any { it.dirty.value }
        val isTouchedNow = states.any { it.touched.value }
        val hasErrorsNow = states.any { state ->
            state.visible.value &&
                    state.enabled.value &&
                    state.error.value != null
        }

        _isDirty.value = isDirtyNow
        _isTouched.value = isTouchedNow
        _hasErrors.value = hasErrorsNow
        _canSubmit.value = isDirtyNow && !hasErrorsNow
    }

    private fun <Value> findDefinition(
        property: KProperty1<Data, Value>
    ): FormicaFieldDefinition<Data, Value> {
        @Suppress("UNCHECKED_CAST")
        return definitionsByProperty[property] as? FormicaFieldDefinition<Data, Value>
            ?: error("Field '${property.name}' is not registered in adapter")
    }
}
