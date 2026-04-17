package dev.voir.formica.schema

import kotlin.reflect.KProperty1

/**
 * Builder for one field inside a schema.
 */
class SchemaFieldBuilder<Data : Any, Value>(
    private val key: String,
    private val property: KProperty1<Data, Value>,
    private val set: (Data, Value?) -> Data,
    private val clear: ((Data) -> Data)?
) {
    private val rules = mutableListOf<ValidationRule<Value?>>()
    private var validateOnChange: Boolean = true
    private var visible: (Data) -> Boolean = { true }
    private var enabled: (Data) -> Boolean = { true }

    /**
     * Add a raw rule.
     */
    fun rule(rule: ValidationRule<Value?>) {
        rules += rule
    }

    /**
     * Control if Formica validates this field immediately on change.
     */
    fun validateOnChange(value: Boolean) {
        validateOnChange = value
    }

    /**
     * Control if the field is currently visible.
     * Hidden fields are filtered by formica-core.
     */
    fun visibleWhen(predicate: (Data) -> Boolean) {
        visible = predicate
    }

    /**
     * Control if the field is currently enabled.
     * Disabled fields are filtered by formica-core.
     */
    fun enabledWhen(predicate: (Data) -> Boolean) {
        enabled = predicate
    }

    internal fun build(): SchemaField<Data, Value> =
        SchemaField(
            key = key,
            property = property,
            set = set,
            clear = clear,
            validateOnChange = validateOnChange,
            isVisible = visible,
            isEnabled = enabled,
            rules = rules.toList()
        )
}
