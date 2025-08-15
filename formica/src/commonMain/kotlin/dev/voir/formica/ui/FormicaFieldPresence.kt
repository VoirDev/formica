package dev.voir.formica.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import dev.voir.formica.Formica
import dev.voir.formica.FormicaFieldId

/**
 * Conditional wrapper for a form field that can be shown/hidden without losing registration.
 *
 * This is useful for *conditionally rendered fields* where:
 *  - You still want the field to be part of the form's registry (state preserved across shows/hides)
 *  - You want to enable/disable validation automatically based on visibility
 *  - You optionally want to clear its value when it is hidden
 *
 * ### How it works
 * - Looks up the registered [FormicaField] for [id] (does **not** register it — you must have
 *   registered it beforehand via `FormicaField` or `registerField`).
 * - Whenever [present] changes:
 *   - Calls `field.setEnabled(present)` so validation short-circuits when hidden.
 *   - If `present == false` and [clearOnHide] is `true`, also calls `form.onChange(id, null)`
 *     to clear the field value in both field state and the form data snapshot.
 * - Renders [content] only if [present] is `true`.
 *
 * ### Example:
 * ```
 * // Always register the field
 * FormicaField(form, AdditionalText) { adapter ->
 *     TextField(
 *         value = adapter.value.orEmpty(),
 *         onValueChange = adapter.onChange
 *     )
 * }
 *
 * // Conditionally render it
 * FormFieldPresence(
 *     form = form,
 *     id = AdditionalText,
 *     present = isExtraSectionEnabled,
 *     clearOnHide = true
 * ) {
 *     // UI for AdditionalText here
 * }
 * ```
 *
 * @param form The form instance holding the registered field.
 * @param id The lens identifying the field to control.
 * @param present Whether the field should be visible and validated.
 * @param clearOnHide If true, clears the field value when it becomes hidden.
 * @param content UI content to render when the field is present.
 */
@Composable
fun <D, V> FormFieldPresence(
    form: Formica<D>,
    id: FormicaFieldId<D, V>,
    present: Boolean,
    clearOnHide: Boolean = false,
    content: @Composable () -> Unit
) {
    // Cache the registered field instance for the lifetime of this form+id combination.
    // This avoids re-fetching the field every recomposition.
    val field = remember(form, id) { form.getRegisteredField(id) }

    // React to changes in the `present` flag.
    LaunchedEffect(present) {
        field?.setEnabled(present)             // Enable/disable validation
        if (!present && clearOnHide) {
            // Clear value in both field state and form data snapshot
            form.onChange(id, null)
        }
    }

    // Render UI only when field is "present".
    if (present) content()
}
