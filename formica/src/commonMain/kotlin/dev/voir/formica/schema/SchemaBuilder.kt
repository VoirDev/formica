package dev.voir.formica.schema

import kotlin.reflect.KProperty1

class SchemaBuilder<T : Any> {
    private val fields = mutableListOf<FieldSchema<T, *>>()

    fun <V> field(
        property: KProperty1<T, V>,
        block: FieldBuilder<T, V>.() -> Unit
    ) {
        val builder = FieldBuilder<T, V>()
        builder.block()
        fields += FieldSchema(property, builder.build())
    }

    internal fun build(): Schema<T> = Schema(fields)
}

fun <T : Any> schema(
    block: SchemaBuilder<T>.() -> Unit
): Schema<T> {
    val builder = SchemaBuilder<T>()
    builder.block()
    return builder.build()
}
