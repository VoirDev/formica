package dev.voir.formica.compose

import dev.voir.formica.core.FormicaFieldValidationResult

/**
 * Read-only Compose-friendly controller for one field.
 *
 * All values are plain Compose state values, not flows.
 */
data class FormicaFieldController<Value>(
    val key: String,
    val value: Value?,
    val error: String?,
    val validationResult: FormicaFieldValidationResult,
    val touched: Boolean,
    val dirty: Boolean,
    val visible: Boolean,
    val enabled: Boolean,

    /**
     * Convenience UI flag.
     *
     * Default behavior:
     * show error only when the field has been touched and there is an error.
     */
    val showError: Boolean,

    /**
     * Typed field update callback.
     */
    val onChange: (Value?) -> Unit,

    /**
     * Validate this field now.
     */
    val validate: () -> FormicaFieldValidationResult,

    /**
     * Clear this field through Formica.
     */
    val clear: () -> Unit
)
