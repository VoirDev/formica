package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import dev.voir.formica.Formica

/**
 * A CompositionLocal that holds the current [Formica] instance for this UI tree.
 *
 * - Typed as `Formica<Any>?` internally so it can store any generic form type.
 * - Accessed via [formicaOf] to get a strongly-typed instance.
 * - Provided by [FormicaProvider].
 *
 * Defaults to `null` when not inside a [FormicaProvider] scope.
 */
val LocalFormica = staticCompositionLocalOf<Formica<Any>?> { null }

/**
 * Create and remember a [Formica] instance tied to the current composition.
 *
 * This is typically called at the top of a screen or form scope.
 * The instance will survive recompositions, and will only be recreated when
 * either [initialData] or [onSubmit] changes.
 *
 * @param initialData The initial immutable data model for the form.
 * @param onSubmit Optional callback invoked with the form's current data when [Formica.submit] is called.
 *
 * @return A remembered [Formica] instance you can use directly or pass to [FormicaProvider].
 *
 * ### Example:
 * ```
 * val form = rememberFormica(Profile("", null)) { data ->
 *     saveProfile(data)
 * }
 * ```
 */
@Composable
fun <Data> rememberFormica(
    initialData: Data,
    onSubmit: ((Data) -> Unit)? = null
): Formica<Data> {
    // Will only recreate the Formica instance when initialData or onSubmit changes
    return remember(initialData, onSubmit) {
        Formica(initialData, onSubmit)
    }
}

/**
 * Provide a [Formica] instance to all composables in [content] via [LocalFormica].
 *
 * This allows child composables to use [formicaOf] to retrieve the form without
 * having to pass it down explicitly.
 *
 * @param form The form instance to provide in this composition scope.
 * @param content UI content that should have access to [form] via [LocalFormica].
 *
 * ### Example:
 * ```
 * val form = rememberFormica(Profile("", null))
 * FormicaProvider(form) {
 *     // Inside here, you can call formicaOf<Profile>() to get the form
 * }
 * ```
 */
@Composable
fun <D> FormicaProvider(
    form: Formica<D>,
    content: @Composable () -> Unit
) {
    @Suppress("UNCHECKED_CAST") // Safe cast because we only ever read via formicaOf<D>()
    CompositionLocalProvider(LocalFormica provides (form as Formica<Any>)) {
        content()
    }
}

/**
 * Retrieve the current [Formica] instance from [LocalFormica] with strong typing.
 *
 * This must be called inside a [FormicaProvider] scope, otherwise it will throw an error.
 *
 * @return The current [Formica] instance typed as [Formica]<D>.
 *
 * ### Example:
 * ```
 * val form = formicaOf<Profile>()
 * form.onChange(FirstName, "Gary")
 * ```
 *
 * @throws IllegalStateException if no [FormicaProvider] is found in the current composition.
 */
@Suppress("UNCHECKED_CAST")
@Composable
fun <D> formicaOf(): Formica<D> {
    val f = LocalFormica.current
        ?: error("No Formica in scope. Wrap with FormicaProvider or pass form explicitly.")
    return f as Formica<D>
}
