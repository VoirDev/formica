package dev.voir.formica.rules

import dev.voir.formica.FormicaFieldResult

interface ValidationRule<V : Any?> {
    fun validate(value: V): FormicaFieldResult
}
