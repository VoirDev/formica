package dev.voir.formica

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Represents a single field inside a form.
 *
 * This class encapsulates:
 *  - The field's current value
 *  - Validation rules and results
 *  - UI-friendly state flags (touched, dirty)
 *  - Optional enable/disable (presence) flag
 *
 * Generic type [Value] can be nullable or non-null, but internally we always store it
 * as `Value?` in [value] to allow temporarily "empty" states.
 */
class FormicaField<Value>(
    initialValue: Value,
    /**
     * A set of built-in or attached validation rules to run in order.
     * Each rule is a [ValidationRule] applied before [customValidation].
     * Order matters: the first failing rule will short-circuit validation.
     */
    private val validators: Set<ValidationRule<Value?>> = emptySet(),

    /**
     * A custom validation function run *after* all [validators].
     * Allows for cross-field or complex validation logic.
     */
    private val customValidation: ((Value?) -> FormicaFieldResult)? = null,

    /**
     * Whether the field should validate itself automatically every time its value changes.
     * If false, you must call [validate] manually (e.g., on form submit).
     */
    private val validateOnChange: Boolean = true,
) {
    /**
     * Current value of the field (nullable so that "empty" can be represented).
     * Observed by UI to display current input.
     */
    val value = MutableStateFlow<Value?>(initialValue)

    /**
     * The last error message for this field, or null if valid.
     * Updated during validation. Observed by UI to show error messages.
     */
    val error = MutableStateFlow<String?>(null)

    /**
     * The last validation result: Success, Error, or NoInput.
     */
    val result = MutableStateFlow<FormicaFieldResult>(FormicaFieldResult.NoInput)

    /**
     * Whether the field has been interacted with (first change made).
     * Useful for deciding when to show validation errors (e.g., on blur).
     */
    val touched = MutableStateFlow(false)

    /**
     * Whether the value has changed from its initial value.
     * Often used to enable/disable a "Save" button.
     */
    val dirty = MutableStateFlow(false)

    /**
     * Snapshot of the initial value for dirty checking and reset logic.
     */
    private var initial = initialValue

    /**
     * Whether the field is "enabled" (present in form) for validation purposes.
     * If disabled, validation will always return Success and skip validators.
     */
    private val enabled = MutableStateFlow(true)

    /**
     * Enable or disable the field for validation.
     * When disabled, validators and customValidation will be skipped.
     */
    fun setEnabled(v: Boolean) {
        enabled.value = v
    }


    /**
     * Called when the user changes the value.
     *
     * Updates:
     *  - [touched]: true after the first change
     *  - [dirty]: true if value != initial
     *  - [value]: set to new input
     *
     * Optionally triggers validation immediately if [validateOnChange] is true.
     */
    fun onChange(input: Value?) {
        if (!touched.value) touched.value = true
        dirty.value = (input != initial)
        value.value = input
        if (validateOnChange) validate()
    }


    /**
     * Reset the field to a new initial value.
     * Clears errors and flags, resets result to NoInput.
     */
    fun reset(newInitial: Value) {
        initial = newInitial
        value.value = newInitial
        error.value = null
        result.value = FormicaFieldResult.NoInput
        touched.value = false
        dirty.value = false
    }

    /**
     * Convenience: return true if [validate] passes.
     */
    fun isValid(): Boolean = validate() is FormicaFieldResult.Success


    /**
     * Run validation on the current value:
     *
     *  1. If disabled ([enabled] == false), skip and mark Success.
     *  2. Run [validators] in order, short-circuit on first Error.
     *  3. Run [customValidation] last if present.
     *  4. Store final result in [result] and update [error] if applicable.
     */
    fun validate(): FormicaFieldResult {
        // Disabled? skip everything and mark as valid
        if (!enabled.value) {
            error.value = null
            result.value = FormicaFieldResult.Success
            return FormicaFieldResult.Success
        }

        val v = value.value

        // Run built-in/attached validators first (ordered)
        for (rule in validators) {
            when (val r = rule.validate(v)) {
                is FormicaFieldResult.Error -> return setError(r)
                else -> {}
            }
        }

        // Custom validation last
        val r = customValidation?.invoke(v) ?: FormicaFieldResult.Success
        return setError(r)
    }


    /**
     * Internal helper to update [result] and [error] flows together.
     */
    private fun setError(r: FormicaFieldResult): FormicaFieldResult {
        result.value = r
        error.value = (r as? FormicaFieldResult.Error)?.message
        return r
    }
}
