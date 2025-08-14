package dev.voir.formica

import dev.voir.formica.ui.FormicaField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormicaFieldTest {

    @Test
    fun `initial state is correct`() = runTest {
        val field = FormicaField(initialValue = null, required = false)
        assertNull(field.value.first())
        assertNull(field.error.first())
    }

    @Test
    fun `onChange updates value and validates when validateOnChange is true`() = runTest {
        val field = FormicaField<String?>(
            initialValue = null,
            required = true,
            requiredError = "Required field"
        )

        field.onChange("New Value")

        assertEquals("New Value", field.value.first())
        assertNull(field.error.first())
    }

    @Test
    fun `onChange skips validation when validateOnChange is false`() = runTest {
        val field = FormicaField(
            initialValue = null,
            required = true,
            validateOnChange = false,
            requiredError = "Required field"
        )

        field.onChange(null)

        assertNull(field.error.first()) // No validation should have occurred
    }

    @Test
    fun `validate returns error for required field with null value`() = runTest {
        val field = FormicaField(
            initialValue = null,
            required = true,
            requiredError = "Field is required"
        )

        assertFalse(field.isValid())
        assertEquals("Field is required", field.error.first())
    }

    @Test
    fun `validate passes for non-required field with null value`() = runTest {
        val field = FormicaField(
            initialValue = null,
            required = false
        )

        assertTrue(field.isValid())
        assertNull(field.error.first())
    }

    @Test
    fun `customValidation is applied correctly`() = runTest {
        val customValidation: (String?) -> FormicaFieldResult = { input ->
            if (input == "valid") FormicaFieldResult.Success
            else FormicaFieldResult.Error("Invalid input")
        }

        val field = FormicaField<String?>(
            initialValue = null,
            required = false,
            customValidation = customValidation
        )

        field.onChange("invalid")
        assertEquals("Invalid input", field.error.first())
        assertFalse(field.isValid())

        field.onChange("valid")
        assertNull(field.error.first())
        assertTrue(field.isValid())
    }

    @Test
    fun `validators are applied correctly`() = runTest {
        val rule: ValidationRule<String> = object : ValidationRule<String> {
            override fun validate(value: String): FormicaFieldResult {
                return if (value.length > 5) FormicaFieldResult.Success
                else FormicaFieldResult.Error("Too short")
            }
        }

        val field = FormicaField(
            initialValue = "",
            required = false,
            validators = setOf(rule)
        )

        field.onChange("short")
        assertEquals("Too short", field.error.first())
        assertFalse(field.isValid())

        field.onChange("longer text")
        assertNull(field.error.first())
        assertTrue(field.isValid())
    }
}
