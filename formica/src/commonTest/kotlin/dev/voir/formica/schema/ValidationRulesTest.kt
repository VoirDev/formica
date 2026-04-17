package dev.voir.formica.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ValidationRulesTest {

    @Test
    fun `required fails for null`() {
        val result = ValidationRules.required<String>().validate(null)

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Field is required", result.message)
    }

    @Test
    fun `required fails for blank string`() {
        val result = ValidationRules.required<String>().validate("   ")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Field is required", result.message)
    }

    @Test
    fun `required fails for empty collection`() {
        val result = ValidationRules.required<List<Int>>().validate(emptyList())

        assertIs<FieldValidationResult.Error>(result)
    }

    @Test
    fun `required succeeds for non-empty value`() {
        val result = ValidationRules.required<String>().validate("John")

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `validateOnlyIf skips when inactive`() {
        val rule = ValidationRules.validateOnlyIf(
            active = { false },
            rule = ValidationRules.required<String>()
        )

        val result = rule.validate(null)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `validateOnlyIf delegates when active`() {
        val rule = ValidationRules.validateOnlyIf(
            active = { true },
            rule = ValidationRules.required<String>()
        )

        val result = rule.validate(null)

        assertIs<FieldValidationResult.Error>(result)
    }

    @Test
    fun `notEmpty skips null`() {
        val result = ValidationRules.notEmpty().validate(null)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `notEmpty fails for empty string`() {
        val result = ValidationRules.notEmpty().validate("")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("This field cannot be empty.", result.message)
    }

    @Test
    fun `notEmpty succeeds for blank but non-empty string`() {
        val result = ValidationRules.notEmpty().validate(" ")

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `notBlank skips null`() {
        val result = ValidationRules.notBlank().validate(null)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `notBlank fails for blank string`() {
        val result = ValidationRules.notBlank().validate("   ")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("This field cannot be blank.", result.message)
    }

    @Test
    fun `notBlank succeeds for non-blank string`() {
        val result = ValidationRules.notBlank().validate("abc")

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `email skips null`() {
        val result = ValidationRules.email().validate(null)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `email fails for invalid address`() {
        val result = ValidationRules.email().validate("wrong")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Must be a valid email address.", result.message)
    }

    @Test
    fun `email succeeds for valid address`() {
        val result = ValidationRules.email().validate("john@example.com")

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `strongPassword skips null`() {
        val result = ValidationRules.strongPassword().validate(null)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `strongPassword fails for short password`() {
        val result = ValidationRules.strongPassword().validate("Aa1!")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Password must be at least 8 characters long.", result.message)
    }

    @Test
    fun `strongPassword fails without uppercase`() {
        val result = ValidationRules.strongPassword().validate("lowercase1!")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Password must contain at least one uppercase letter.", result.message)
    }

    @Test
    fun `strongPassword fails without lowercase`() {
        val result = ValidationRules.strongPassword().validate("UPPERCASE1!")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Password must contain at least one lowercase letter.", result.message)
    }

    @Test
    fun `strongPassword fails without digit`() {
        val result = ValidationRules.strongPassword().validate("Password!")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Password must contain at least one digit.", result.message)
    }

    @Test
    fun `strongPassword fails without special character`() {
        val result = ValidationRules.strongPassword().validate("Password1")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Password must contain at least one special character.", result.message)
    }

    @Test
    fun `strongPassword succeeds for valid password`() {
        val result = ValidationRules.strongPassword().validate("Password1!")

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `url skips null`() {
        val result = ValidationRules.url().validate(null)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `url accepts domain when protocol not required`() {
        val result = ValidationRules.url(protocolRequired = false).validate("example.com")

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `url rejects domain when protocol required`() {
        val result = ValidationRules.url(protocolRequired = true).validate("example.com")

        assertIs<FieldValidationResult.Error>(result)
    }

    @Test
    fun `url accepts full http url when protocol required`() {
        val result = ValidationRules.url(protocolRequired = true).validate("https://example.com")

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `checked skips null`() {
        val result = ValidationRules.checked().validate(null)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `checked fails for false`() {
        val result = ValidationRules.checked().validate(false)

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Must be checked", result.message)
    }

    @Test
    fun `checked succeeds for true`() {
        val result = ValidationRules.checked().validate(true)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `minLength skips null`() {
        val result = ValidationRules.minLength(3).validate(null)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `minLength fails when too short`() {
        val result = ValidationRules.minLength(3).validate("ab")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Must be at least 3 characters long.", result.message)
    }

    @Test
    fun `minLength succeeds when long enough`() {
        val result = ValidationRules.minLength(3).validate("abc")

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `maxLength skips null`() {
        val result = ValidationRules.maxLength(3).validate(null)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `maxLength fails when too long`() {
        val result = ValidationRules.maxLength(3).validate("abcd")

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Must not exceed 3 characters.", result.message)
    }

    @Test
    fun `maxLength succeeds when short enough`() {
        val result = ValidationRules.maxLength(3).validate("abc")

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `generic range skips null`() {
        val result = ValidationRules.range(1, 10).validate(null)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `generic range succeeds inside inclusive bounds`() {
        val result = ValidationRules.range(1, 10).validate(5)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `generic range fails outside inclusive bounds`() {
        val result = ValidationRules.range(1, 10).validate(11)

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Must be a number between 1 and 10.", result.message)
    }

    @Test
    fun `generic range fails at edge when exclusive`() {
        val result = ValidationRules.range(1, 10, inclusive = false).validate(1)

        assertIs<FieldValidationResult.Error>(result)
    }

    @Test
    fun `double range skips NaN`() {
        val result = ValidationRules.range(1.0, 2.0).validate(Double.NaN)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `double range respects epsilon`() {
        val result = ValidationRules.range(
            min = 1.0,
            max = 2.0,
            inclusive = true,
            epsilon = 0.01
        ).validate(2.005)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `float range skips NaN`() {
        val result = ValidationRules.range(1f, 2f).validate(Float.NaN)

        assertEquals(FieldValidationResult.Skip, result)
    }

    @Test
    fun `int min fails below bound`() {
        val result = ValidationRules.min(5).validate(4)

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Must be >= 5.", result.message)
    }

    @Test
    fun `int min succeeds at inclusive bound`() {
        val result = ValidationRules.min(5).validate(5)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `int max fails above bound`() {
        val result = ValidationRules.max(5).validate(6)

        assertIs<FieldValidationResult.Error>(result)
        assertEquals("Must be <= 5.", result.message)
    }

    @Test
    fun `int max succeeds at inclusive bound`() {
        val result = ValidationRules.max(5).validate(5)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `long min succeeds`() {
        val result = ValidationRules.min(5L).validate(6L)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `long max succeeds`() {
        val result = ValidationRules.max(5L).validate(4L)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `double min respects epsilon`() {
        val result = ValidationRules.min(
            min = 10.0,
            inclusive = true,
            epsilon = 0.1
        ).validate(9.95)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `double max respects epsilon`() {
        val result = ValidationRules.max(
            max = 10.0,
            inclusive = true,
            epsilon = 0.1
        ).validate(10.05)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `float min succeeds`() {
        val result = ValidationRules.min(1f).validate(2f)

        assertEquals(FieldValidationResult.Success, result)
    }

    @Test
    fun `float max succeeds`() {
        val result = ValidationRules.max(2f).validate(1f)

        assertEquals(FieldValidationResult.Success, result)
    }
}
