package dev.voir.formica.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Reactive runtime state of a single field.
 *
 * This class contains UI/runtime state only.
 * It does not know validation rules or schema internals.
 */
class FormicaFieldState<Value>(
    initialValue: Value?,
    initialVisible: Boolean,
    initialEnabled: Boolean
) {
    private var initial: Value? = initialValue

    private val _value = MutableStateFlow(initialValue)
    val value: StateFlow<Value?> get() = _value

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _validationResult =
        MutableStateFlow<FormicaFieldValidationResult>(FormicaFieldValidationResult.Skip)
    val validationResult: StateFlow<FormicaFieldValidationResult> get() = _validationResult

    private val _touched = MutableStateFlow(false)
    val touched: StateFlow<Boolean> get() = _touched

    private val _dirty = MutableStateFlow(false)
    val dirty: StateFlow<Boolean> get() = _dirty

    private val _visible = MutableStateFlow(initialVisible)
    val visible: StateFlow<Boolean> get() = _visible

    private val _enabled = MutableStateFlow(initialEnabled)
    val enabled: StateFlow<Boolean> get() = _enabled

    /**
     * Called when the user changes the field value.
     */
    fun onChange(newValue: Value?) {
        if (!_touched.value) _touched.value = true
        _value.value = newValue
        _dirty.value = newValue != initial
    }

    /**
     * Reset field to a new initial value and clear all transient UI state.
     */
    fun reset(newInitial: Value?, visible: Boolean, enabled: Boolean) {
        initial = newInitial
        _value.value = newInitial
        _error.value = null
        _validationResult.value = FormicaFieldValidationResult.Skip
        _touched.value = false
        _dirty.value = false
        _visible.value = visible
        _enabled.value = enabled
    }

    /**
     * Used when only visibility/enabled state changes.
     */
    fun updateAvailability(visible: Boolean, enabled: Boolean) {
        _visible.value = visible
        _enabled.value = enabled
    }

    /**
     * Store last validation result and synced error text.
     */
    fun setValidationResult(result: FormicaFieldValidationResult) {
        _validationResult.value = result
        _error.value = (result as? FormicaFieldValidationResult.Error)?.message
    }

    /**
     * Clear validation state without touching value/touched/dirty.
     */
    fun clearValidation() {
        _validationResult.value = FormicaFieldValidationResult.Skip
        _error.value = null
    }
}
