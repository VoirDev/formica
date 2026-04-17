package dev.voir.formica.schema

import kotlin.reflect.KProperty1

/**
 * Root schema builder.
 */
class SchemaBuilder<Data : Any> {
    private val fields = mutableListOf<SchemaField<Data, *>>()
    private val objectRules = mutableListOf<ObjectValidationRule<Data>>()

    /**
     * Register one field.
     *
     * key defaults to property.name, but you can override it if needed.
     */
    fun <Value> field(
        property: KProperty1<Data, Value>,
        set: (Data, Value?) -> Data,
        clear: ((Data) -> Data)? = null,
        key: String = property.name,
        block: SchemaFieldBuilder<Data, Value>.() -> Unit = {}
    ): SchemaField<Data, Value> {
        val builder = SchemaFieldBuilder(
            key = key,
            property = property,
            set = set,
            clear = clear
        )
        builder.block()
        return builder.build().also(fields::add)
    }

    /**
     * Add a cross-field validation rule.
     */
    fun objectRule(rule: ObjectValidationRule<Data>) {
        objectRules += rule
    }

    /**
     * Convenience overload.
     */
    fun objectRule(block: (Data) -> ObjectRuleResult) {
        objectRules += ObjectValidationRule(block)
    }

    internal fun build(): Schema<Data> =
        Schema(
            schemaFields = fields.toList(),
            objectRules = objectRules.toList()
        )
}

/**
 * Entry point for building a schema.
 */
fun <Data : Any> schema(
    block: SchemaBuilder<Data>.() -> Unit
): Schema<Data> =
    SchemaBuilder<Data>().apply(block).build()
