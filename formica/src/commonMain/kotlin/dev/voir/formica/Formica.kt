package dev.voir.formica

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


/**
 * A form state container that:
 *  - Holds a reactive [data] snapshot (immutable [Data])
 *  - Manages a registry of fields (each field keeps its own reactive state & validation)
 *  - Provides typed updates via [onChange], and form-level [validate]/[submit] workflows
 *  - Supports clearing & syncing from external data sources
 *
 * ### Key ideas
 * - **No reflection:** fields are registered with [FormicaFieldId] (lens-like accessors).
 * - **Immutable data:** every write returns a new [Data], updating [_data] so UI can react.
 * - **Field state is separate:** each field maintains `value/error/result/touched/dirty`, but the
 *   canonical committed model is always [data].
 * - **Validation pipeline:** `FormicaField` handles required/validators/custom. Here we just call `validate()`
 *   across all registered fields and aggregate errors.
 */
class Formica<Data>(val initialData: Data, private val onSubmit: ((Data) -> Unit)? = null) {
    /**
     * Registry of fields by their [FormicaFieldId.id].
     * Pair = (lens, field-state).
     *
     * NOTE: We use `Any?` erasure behind the scenes; public APIs remain strongly typed.
     */
    private val fields =
        mutableMapOf<String, Pair<FormicaFieldId<Data, Any?>, FormicaField<Any?>>>()

    /**
     * Reactive, immutable snapshot of the entire form data model.
     * Every successful [onChange]/[clear] produces a new instance.
     */
    private val _data = MutableStateFlow(initialData)
    val data: StateFlow<Data> get() = _data

    /**
     * Reactive result of the last form-level validation/submit attempt.
     * - Starts as [FormicaResult.NoInput]
     * - Changes to [FormicaResult.Valid] or [FormicaResult.Error] after [validate]/[submit]
     */
    private val _result = MutableStateFlow<FormicaResult>(FormicaResult.NoInput)
    val result: StateFlow<FormicaResult> get() = _result

    /**
     * Register a field for this form. Must be called once per field you plan to use.
     *
     * @param id Lens describing how to read/write the field on [Data].
     * @param validators ORDERED set of validators for the field (first failure short-circuits).
     *                   (Kotlin's default `setOf(...)` is insertion-ordered / LinkedHashSet.)
     * @param customValidation Optional rule run *after* all validators.
     * @param validateOnChange If true, field validates automatically on value changes.
     *
     * @return The created [FormicaField] (reactive value/error/etc.).
     *
     * You can conditionally *render* inputs, but you should *register* fields once to keep
     * state stable across composition toggles.
     */
    fun <Value> registerField(
        id: FormicaFieldId<Data, Value>,
        validators: Set<ValidationRule<Value?>>,
        customValidation: ((Value?) -> FormicaFieldResult)? = null,
        validateOnChange: Boolean = true,
    ): FormicaField<Value> {
        val field = FormicaField(
            initialValue = id.get(_data.value), // seed from current data snapshot
            validators = validators,
            customValidation = customValidation,
            validateOnChange = validateOnChange
        )
        // Store in registry with erased generics
        fields[id.id] = (id as FormicaFieldId<Data, Any?>) to (field as FormicaField<Any?>)

        return field
    }

    /**
     * Retrieve a previously registered field (for reading value/error/touched/dirty externally).
     * Returns null if the field hasn't been registered in this form.
     */
    fun <V> getRegisteredField(id: FormicaFieldId<Data, V>): FormicaField<V>? {
        val pair = fields[id.id] ?: return null
        @Suppress("UNCHECKED_CAST")
        return pair.second as? FormicaField<V>
    }

    /**
     * Apply a single-field update and propagate it to both:
     *  1) The field's reactive state (value/touched/dirty and optional per-change validation)
     *  2) The immutable [data] snapshot via lens set/clear
     *
     * @param id Lens for the field
     * @param value New value, or `null` to indicate "clear". If `null` and [FormicaFieldId.clear] is not set,
     *              the data snapshot remains unchanged (field state still updates).
     */
    fun <V> onChange(id: FormicaFieldId<Data, V>, value: V?) {
        val pair = fields[id.id] ?: return
        val (lens, field) = pair

        // 1) Update field reactive state (value/touched/dirty + maybe validate)
        @Suppress("UNCHECKED_CAST")
        (field as FormicaField<V>).onChange(value)

        // 2) Update immutable data snapshot via lens
        @Suppress("UNCHECKED_CAST")
        val l = lens as FormicaFieldId<Data, V>
        _data.value = if (value == null) {
            l.clear?.invoke(_data.value) ?: _data.value
        } else {
            l.set(_data.value, value)
        }
    }

    /**
     * Validate all registered fields and update [result] accordingly.
     *
     * @return [FormicaResult.Valid] if all fields are valid, otherwise [FormicaResult.Error]
     *         with a map of `fieldId -> message`.
     *
     * Field-level validation order is handled inside each [FormicaField].
     */
    fun validate(): FormicaResult {
        val errors = mutableMapOf<String, String>()

        for ((key, pair) in fields) {
            val (_, field) = pair
            val res = field.validate()
            if (res is FormicaFieldResult.Error) {
                errors[key] = res.message
            }
        }

        val newState = if (errors.isEmpty()) {
            FormicaResult.Valid
        } else {
            FormicaResult.Error(
                message = "Some fields are not valid", // TODO This message can be changed
                fieldErrors = errors.toMap()
            )
        }

        _result.value = newState
        return newState
    }

    /**
     * Bring all registered fields' initial/value state back in sync with the current [data] snapshot.
     *
     * Useful after you changed [_data] externally (e.g., loaded a draft, applied a server patch).
     * Resets each field to "pristine" (NoInput, not dirty/touched).
     */
    fun syncFromData() {
        for ((_, pair) in fields) {
            val (lens, field) = pair
            val v = lens.get(_data.value)
            field.reset(v)
        }
        _result.value = FormicaResult.NoInput
    }

    /**
     * Validate and, if successful, invoke [onSubmit] with the latest [data] snapshot.
     *
     * @return the validation result so callers can branch on it.
     */
    fun submit(): FormicaResult {
        val r = validate()
        if (r is FormicaResult.Valid) {
            onSubmit?.invoke(_data.value)
        }
        return r
    }

    /**
     * Explicitly clear a field on the immutable [data] snapshot using its [FormicaFieldId.clear]
     * (if provided). Does *not* touch the field's reactive value; combine with
     * `getRegisteredField(...).reset(...)` or `onChange(id, null)` if you also want to
     * update the field state.
     */
    fun <V> clear(id: FormicaFieldId<Data, V>) {
        val pair = fields[id.id] ?: return
        val lens = pair.first
        (lens.clear ?: return)(_data.value).also { _data.value = it }
    }
}
