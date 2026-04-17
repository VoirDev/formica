package dev.voir.formica.compose

import androidx.compose.runtime.Composable
import kotlin.reflect.KProperty1

/**
 * Scope receiver available inside Formica { ... }.
 *
 * Provides typed field access without exposing flows directly.
 */
interface FormicaScope<Data : Any> {

    /**
     * Render one typed field controller.
     */
    @Composable
    fun <Value> Field(
        property: KProperty1<Data, Value>,
        content: @Composable (FormicaFieldController<Value>) -> Unit
    )

    /**
     * Convenience helper to get current field controller as a value.
     * Useful when composing custom wrappers.
     */
    @Composable
    fun <Value> field(
        property: KProperty1<Data, Value>
    ): FormicaFieldController<Value>
}
