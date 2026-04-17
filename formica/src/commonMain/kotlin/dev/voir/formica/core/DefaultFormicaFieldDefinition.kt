package dev.voir.formica.core

import kotlin.reflect.KProperty1

/**
 * Small default implementation of a field definition.
 *
 * Most adapters/schemas can reuse this instead of implementing
 * FormicaFieldDefinition manually every time.
 */
data class DefaultFormicaFieldDefinition<Data : Any, Value>(
    override val key: String,
    override val property: KProperty1<Data, Value>,
    override val set: (Data, Value?) -> Data,
    override val clear: ((Data) -> Data)? = null,
    override val validateOnChange: Boolean = true,
    override val isVisible: (Data) -> Boolean = { true },
    override val isEnabled: (Data) -> Boolean = { true }
) : FormicaFieldDefinition<Data, Value>
