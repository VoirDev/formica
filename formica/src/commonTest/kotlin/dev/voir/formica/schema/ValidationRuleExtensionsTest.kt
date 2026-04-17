package dev.voir.formica.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ValidationRuleExtensionsTest {

    @Test
    fun `and returns first error if first rule fails`() {
        val rule = ValidationRules.notBlank("blank")
            .and(ValidationRules.minLength(5, "short"))

        val result = rule.validate("")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("blank", result.message)
    }

    @Test
    fun `and returns second error if first succeeds and second fails`() {
        val rule = ValidationRules.notBlank("blank")
            .and(ValidationRules.minLength(5, "short"))

        val result = rule.validate("abc")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("short", result.message)
    }

    @Test
    fun `and succeeds when both succeed`() {
        val rule = ValidationRules.notBlank("blank")
            .and(ValidationRules.minLength(3, "short"))

        val result = rule.validate("abcd")

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `onlyIf skips when predicate is false`() {
        val rule = ValidationRules.required<String>().onlyIf { false }

        val result = rule.validate(null)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `onlyIf evaluates when predicate is true`() {
        val rule = ValidationRules.required<String>().onlyIf { true }

        val result = rule.validate(null)

        assertIs<FieldValidationResult.Error>(result)
    }
}
