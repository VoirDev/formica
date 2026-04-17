package dev.voir.formica.compose

import dev.voir.formica.core.FormicaSubmitResult
import dev.voir.formica.core.FormicaValidationResult

/**
 * Read-only Compose-friendly controller for the whole form.
 *
 * Values here are already collected from StateFlow and are safe to use directly in UI.
 */
data class FormicaController<Data : Any>(
    val data: Data,
    val isDirty: Boolean,
    val isTouched: Boolean,
    val hasErrors: Boolean,
    val canSubmit: Boolean,
    val fieldErrors: Map<String, String>,
    val formErrors: List<String>,
    val submitResult: FormicaSubmitResult,
    val validate: () -> FormicaValidationResult<Data>,
    val submit: () -> FormicaValidationResult<Data>,
    val reset: () -> Unit,
    val replaceData: (Data) -> Unit
)
