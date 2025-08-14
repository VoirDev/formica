package dev.voir.formica

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FormicaFieldId<Data, V>(
    val id: String,                       // stable key (e.g., "firstName")
    val get: (Data) -> V,                 // read from Data
    val set: (Data, V) -> Data,            // return a *new* Data with V set
    val clear: ((Data) -> Data)? = null
)

class Formica<Data>(val initialData: Data, private val onSubmit: ((Data) -> Unit)? = null) {
    private val fields =
        mutableMapOf<String, Pair<FormicaFieldId<Data, Any?>, FormicaField<Any?>>>()

    private val _data = MutableStateFlow(initialData)
    val data: StateFlow<Data> get() = _data

    private val _result = MutableStateFlow<FormicaResult>(FormicaResult.NoInput)
    val result: StateFlow<FormicaResult> get() = _result

    /**
     * Register a field.
     * - You provide a FieldId (lens-like get/set) so we avoid reflection and mutation.
     * - validators are ORDERED.
     */
    fun <Value> registerField(
        id: FormicaFieldId<Data, Value>,
        validators: Set<ValidationRule<Value?>>,
        customValidation: ((Value?) -> FormicaFieldResult)? = null,
        validateOnChange: Boolean = true,
    ): FormicaField<Value> {
        val field = FormicaField(
            initialValue = id.get(_data.value),
            validators = validators,
            customValidation = customValidation,
            validateOnChange = validateOnChange
        )
        fields[id.id] = (id as FormicaFieldId<Data, Any?>) to (field as FormicaField<Any?>)

        return field
    }

    fun <V> getRegisteredField(id: FormicaFieldId<Data, V>): FormicaField<V>? {
        val pair = fields[id.id] ?: return null
        return pair.second as? FormicaField<V>
    }

    /**
     * Update a single field and data immutably.
     */
    fun <V> onChange(id: FormicaFieldId<Data, V>, value: V?) {
        val pair = fields[id.id] ?: return
        val (lens, field) = pair

        // Update field reactive state
        @Suppress("UNCHECKED_CAST")
        (field as FormicaField<V>).onChange(value)

        // Update data via lens (if value == null, keep old value as-is)
        @Suppress("UNCHECKED_CAST")
        val l = lens as FormicaFieldId<Data, V>
        _data.value = if (value == null) {
            l.clear?.invoke(_data.value) ?: _data.value
        } else {
            l.set(_data.value, value)
        }
    }

    /**
     * Validate all fields and set form result.
     * Returns detailed errors map.
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
                message = "Some fields are not valid",
                fieldErrors = errors.toMap()
            )
        }

        _result.value = newState
        return newState
    }

    /**
     * Reset all fields to reflect the current data snapshot (useful after external data changes).
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
     * Try to submit; returns the result so callers can branch.
     */
    fun submit(): FormicaResult {
        val r = validate()
        if (r is FormicaResult.Valid) {
            onSubmit?.invoke(_data.value)
        }
        return r
    }

    fun <V> clear(id: FormicaFieldId<Data, V>) {
        val pair = fields[id.id] ?: return
        val lens = pair.first
        (lens.clear ?: return)(_data.value).also { _data.value = it }
    }
}
