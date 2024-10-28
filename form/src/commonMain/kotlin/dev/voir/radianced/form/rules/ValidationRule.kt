package dev.voir.radianced.form.rules

import dev.voir.radianced.form.FormFieldResult

interface ValidationRule<V : Any?> {
    fun validate(value: V): FormFieldResult
}
