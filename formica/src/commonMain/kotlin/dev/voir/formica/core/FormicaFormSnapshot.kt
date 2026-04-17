package dev.voir.formica.core

/**
 * Immutable snapshot of the whole form.
 *
 * Helpful for debugging, tests, or UI layers that want
 * a single aggregate view.
 */
data class FormicaFormSnapshot<Data : Any>(
    val data: Data,
    val isDirty: Boolean,
    val isTouched: Boolean,
    val hasErrors: Boolean,
    val canSubmit: Boolean,
    val submitResult: FormicaSubmitResult,
    val fieldErrors: Map<String, String>,
    val formErrors: List<String>
)
