package dev.voir.formica

import kotlinx.coroutines.flow.MutableStateFlow

class FormicaField<Value>(
    initialValue: Value,
    private val validators: Set<ValidationRule<Value?>> = emptySet(),
    private val customValidation: ((Value?) -> FormicaFieldResult)? = null,
    private val validateOnChange: Boolean = true,
) {
    val value = MutableStateFlow<Value?>(initialValue)
    val error = MutableStateFlow<String?>(null)
    val result = MutableStateFlow<FormicaFieldResult>(FormicaFieldResult.NoInput)
    val touched = MutableStateFlow(false)
    val dirty = MutableStateFlow(false)

    private var initial = initialValue

    private val enabled = MutableStateFlow(true)

    fun setEnabled(v: Boolean) {
        enabled.value = v
    }

    fun onChange(input: Value?) {
        if (!touched.value) touched.value = true
        dirty.value = (input != initial)
        value.value = input
        if (validateOnChange) validate()
    }


    fun reset(newInitial: Value) {
        initial = newInitial
        value.value = newInitial
        error.value = null
        result.value = FormicaFieldResult.NoInput
        touched.value = false
        dirty.value = false
    }

    fun isValid(): Boolean = validate() is FormicaFieldResult.Success

    fun validate(): FormicaFieldResult {
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

    private fun setError(r: FormicaFieldResult): FormicaFieldResult {
        result.value = r
        error.value = (r as? FormicaFieldResult.Error)?.message
        return r
    }
}
