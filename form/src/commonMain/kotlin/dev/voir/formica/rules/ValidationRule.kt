package dev.voir.formica.rules

import dev.voir.formica.FormFieldResult

interface ValidationRule<V : Any?> {
    fun validate(value: V): FormFieldResult
}
