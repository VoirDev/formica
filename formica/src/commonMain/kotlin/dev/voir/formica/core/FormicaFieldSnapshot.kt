package dev.voir.formica.core

/**
 * Immutable snapshot of one field.
 *
 * Useful for consumers that want a read-only summary
 * without directly observing all individual flows.
 */
data class FormicaFieldSnapshot<Value>(
    val key: String,
    val value: Value?,
    val error: String?,
    val validationResult: FormicaFieldValidationResult,
    val touched: Boolean,
    val dirty: Boolean,
    val visible: Boolean,
    val enabled: Boolean
)
