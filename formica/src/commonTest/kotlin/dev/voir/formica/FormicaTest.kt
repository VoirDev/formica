package dev.voir.formica

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

data class TestData(var field1: String, var field2: Int)

class FormicaTest {

    @Test
    fun `initial state is correct`() = runTest {
        val testData = TestData(field1 = "initial", field2 = 0)
        val formica = Formica(initialData = testData)

        assertEquals(testData, formica.data.first())
        assertTrue(formica.result.first() is FormicaResult.NoInput)
    }

    @Test
    fun `registerField adds field and allows validation`() = runTest {
        val testData = TestData(field1 = "initial", field2 = 0)
        val formica = Formica(initialData = testData)

        val rule: ValidationRule<String?> = object : ValidationRule<String?> {
            override fun validate(value: String?): FormicaFieldResult {
                return if (value.isNullOrEmpty()) FormicaFieldResult.Error("Field1 cannot be empty")
                else FormicaFieldResult.Success
            }
        }

        val field = formica.registerField(
            name = TestData::field1,
            required = true,
            validators = setOf(rule)
        )

        assertEquals("initial", field.value.first())
        assertTrue(field.isValid())
    }

    @Test
    fun `onChange updates data and validates field`() = runTest {
        val testData = TestData(field1 = "initial", field2 = 0)
        val formica = Formica(initialData = testData)

        val rule: ValidationRule<String?> = object : ValidationRule<String?> {
            override fun validate(value: String?): FormicaFieldResult {
                return if (value.isNullOrEmpty()) FormicaFieldResult.Error("Field1 cannot be empty")
                else FormicaFieldResult.Success
            }
        }

        formica.registerField(
            name = TestData::field1,
            required = true,
            validators = setOf(rule)
        )

        formica.onChange(TestData::field1, "updated")
        assertEquals("updated", formica.data.first().field1)
        assertEquals(FormicaResult.NoInput, formica.result.first())
    }

    @Test
    fun `validate updates state based on fields validity`() = runTest {
        val testData = TestData(field1 = "valid", field2 = 0)
        val formica = Formica(initialData = testData)

        val rule1: ValidationRule<String?> = object : ValidationRule<String?> {
            override fun validate(value: String?): FormicaFieldResult {
                return if (value.isNullOrEmpty()) FormicaFieldResult.Error("Field1 cannot be empty")
                else FormicaFieldResult.Success
            }
        }

        val rule2: ValidationRule<Int?> = object : ValidationRule<Int?> {
            override fun validate(value: Int?): FormicaFieldResult {
                return if (value != null && value >= 0) FormicaFieldResult.Success
                else FormicaFieldResult.Error("Field2 must be non-negative")
            }
        }

        formica.registerField(
            name = TestData::field1,
            required = true,
            validators = setOf(rule1)
        )

        formica.registerField(
            name = TestData::field2,
            required = true,
            validators = setOf(rule2)
        )

        assertEquals(FormicaResult.Valid, formica.validate())
        formica.onChange(TestData::field1, "")
        formica.validate()
        assertTrue(formica.result.first() is FormicaResult.Error)
    }

    @Test
    fun `validate returns error when fields are invalid`() = runTest {
        val testData = TestData(field1 = "", field2 = -1)
        val formica = Formica(initialData = testData)

        val rule1: ValidationRule<String?> = object : ValidationRule<String?> {
            override fun validate(value: String?): FormicaFieldResult {
                return if (value.isNullOrEmpty()) FormicaFieldResult.Error("Field1 cannot be empty")
                else FormicaFieldResult.Success
            }
        }

        val rule2: ValidationRule<Int?> = object : ValidationRule<Int?> {
            override fun validate(value: Int?): FormicaFieldResult {
                return if (value != null && value >= 0) FormicaFieldResult.Success
                else FormicaFieldResult.Error("Field2 must be non-negative")
            }
        }

        formica.registerField(
            name = TestData::field1,
            required = true,
            validators = setOf(rule1)
        )

        formica.registerField(
            name = TestData::field2,
            required = true,
            validators = setOf(rule2)
        )

        val validationResult = formica.validate()
        assertTrue(validationResult is FormicaResult.Error)
        assertEquals("Some fields not valid", validationResult.message)
    }
}
