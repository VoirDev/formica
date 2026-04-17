package dev.voir.formica.core

import kotlin.reflect.KProperty1

/**
 * Describes one field known to the form engine.
 *
 * The core runtime uses:
 * - key      -> stable identity inside the form
 * - property -> typed getter
 * - set      -> immutable update function
 * - clear    -> optional "remove/reset" update function
 *
 * Validation is NOT defined here directly. That belongs to the adapter.
 */
interface FormicaFieldDefinition<Data : Any, Value> {
    val key: String
    val property: KProperty1<Data, Value>

    /**
     * Immutable update of a field value into the parent data object.
     */
    val set: (Data, Value?) -> Data

    /**
     * Optional immutable clear/reset operation for this field.
     */
    val clear: ((Data) -> Data)?

    /**
     * Whether Formica should validate this field automatically when it changes.
     */
    val validateOnChange: Boolean

    /**
     * Whether this field should be visible for the current data snapshot.
     */
    val isVisible: (Data) -> Boolean

    /**
     * Whether this field should be enabled for the current data snapshot.
     * Disabled fields are ignored for interaction/validation purposes.
     */
    val isEnabled: (Data) -> Boolean
}
