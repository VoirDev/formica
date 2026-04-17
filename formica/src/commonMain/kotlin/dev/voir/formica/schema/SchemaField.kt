package dev.voir.formica.schema


import dev.voir.formica.core.FormicaFieldDefinition
import kotlin.reflect.KProperty1

/**
 * One schema field definition.
 *
 * Stores:
 * - runtime field metadata needed by formica-core
 * - rules used by this schema implementation
 */
data class SchemaField<Data : Any, Value>(
    override val key: String,
    override val property: KProperty1<Data, Value>,
    override val set: (Data, Value?) -> Data,
    override val clear: ((Data) -> Data)? = null,
    override val validateOnChange: Boolean = true,
    override val isVisible: (Data) -> Boolean = { true },
    override val isEnabled: (Data) -> Boolean = { true },
    val rules: List<ValidationRule<Value?>> = emptyList()
) : FormicaFieldDefinition<Data, Value>
