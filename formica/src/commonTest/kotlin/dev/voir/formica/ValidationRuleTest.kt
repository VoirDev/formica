package dev.voir.formica

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationRulesTest {

    // ---- Helpers ------------------------------------------------------------

    private fun assertSuccess(result: FormicaFieldResult) =
        assertTrue(result is FormicaFieldResult.Success, "Expected Success, got $result")

    private fun assertErrorMessage(result: FormicaFieldResult, expected: String) {
        when (result) {
            is FormicaFieldResult.Error -> assertEquals(expected, result.message)
            else -> throw AssertionError("Expected Error('$expected'), got $result")
        }
    }

    // ---- validateOnlyIf -----------------------------------------------------

    @Test
    fun validateOnlyIf_runsRuleWhenActive() {
        var called = false
        val rule = ValidationRules.validateOnlyIf(active = { true }) { v: String? ->
            called = true
            if (v.isNullOrBlank()) FormicaFieldResult.Error("X") else FormicaFieldResult.Success
        }

        val r1 = rule.validate(null)
        assertTrue(called)
        assertErrorMessage(r1, "X")

        called = false
        val r2 = rule.validate("ok")
        assertTrue(called)
        assertSuccess(r2)
    }

    @Test
    fun validateOnlyIf_skipsWhenInactive() {
        var called = false
        val rule = ValidationRules.validateOnlyIf(active = { false }) { _: String? ->
            called = true
            FormicaFieldResult.Error("should never be called")
        }

        val r = rule.validate(null)
        assertFalse(called)
        assertSuccess(r)
    }

    // ---- required -----------------------------------------------------------

    @Test
    fun required_handlesNullBlankAndEmpty() {
        val ruleStr = ValidationRules.required<String>()
        assertErrorMessage(ruleStr.validate(null), "Field is required")
        assertErrorMessage(ruleStr.validate(""), "Field is required")
        assertErrorMessage(ruleStr.validate("   "), "Field is required")
        assertSuccess(ruleStr.validate("data"))

        val ruleList = ValidationRules.required<List<Int>>()
        assertErrorMessage(ruleList.validate(null), "Field is required")
        assertErrorMessage(ruleList.validate(emptyList()), "Field is required")
        assertSuccess(ruleList.validate(listOf(1)))
    }

    @Test
    fun required_customMessageAndIsEmpty() {
        val rule = ValidationRules.required<Int>(
            message = "Need a positive int",
            isEmpty = { it == null || it!! <= 0 }
        )
        assertErrorMessage(rule.validate(null), "Need a positive int")
        assertErrorMessage(rule.validate(0), "Need a positive int")
        assertSuccess(rule.validate(1))
    }

    // ---- notEmpty / notBlank ------------------------------------------------

    @Test
    fun notEmpty_works() {
        val rule = ValidationRules.notEmpty("NE")
        assertErrorMessage(rule.validate(""), "NE")
        assertSuccess(rule.validate(" "))
        assertSuccess(rule.validate("x"))
    }

    @Test
    fun notBlank_works() {
        val rule = ValidationRules.notBlank("NB")
        assertErrorMessage(rule.validate(""), "NB")
        assertErrorMessage(rule.validate("   "), "NB")
        assertSuccess(rule.validate("x"))
        assertSuccess(rule.validate("  x  "))
    }

    // ---- email --------------------------------------------------------------

    @Test
    fun email_validAndInvalid() {
        val rule = ValidationRules.email("Bad email")

        // Valid
        assertSuccess(rule.validate("a@b.co"))
        assertSuccess(rule.validate("first.last+tag@sub.domain.io"))

        // Invalid
        assertErrorMessage(rule.validate("plainaddress"), "Bad email")
        assertErrorMessage(rule.validate("a@b"), "Bad email")
        assertErrorMessage(rule.validate("@nope.com"), "Bad email")
        assertErrorMessage(rule.validate("a@b..com"), "Bad email")
    }

    // ---- strongPassword -----------------------------------------------------

    @Test
    fun strongPassword_coversEachFailurePath() {
        val rule = ValidationRules.strongPassword(
            minLength = 8,
            lengthMessage = "LEN",
            uppercaseMessage = "UC",
            lowercaseMessage = "LC",
            digitMessage = "DG",
            specialCharacterMessage = "SC"
        )

        assertErrorMessage(rule.validate("A1!a"), "LEN")          // too short
        assertErrorMessage(rule.validate("abcd123!"), "UC")       // no uppercase
        assertErrorMessage(rule.validate("ABCD123!"), "LC")       // no lowercase
        assertErrorMessage(rule.validate("Abcd!!!!"), "DG")       // no digit
        assertErrorMessage(rule.validate("Abcd1234"), "SC")       // no special

        assertSuccess(rule.validate("Abcd123!"))                  // all good
    }

    // ---- url ----------------------------------------------------------------

    @Test
    fun url_withOrWithoutProtocol() {
        val anyUrl = ValidationRules.url(protocolRequired = false, message = "URL")
        val protoOnly = ValidationRules.url(protocolRequired = true, message = "URL")

        // Valid without protocol
        assertSuccess(anyUrl.validate("example.com"))
        assertSuccess(anyUrl.validate("sub.domain.io/path?q=1"))

        // Valid with protocol
        assertSuccess(anyUrl.validate("http://example.com"))
        assertSuccess(anyUrl.validate("https://www.example.org/a/b?x=1#frag"))
        assertSuccess(protoOnly.validate("https://example.com"))

        // Invalids
        assertErrorMessage(protoOnly.validate("example.com"), "URL")
        assertErrorMessage(anyUrl.validate("htp://broken.com"), "URL")
        assertErrorMessage(anyUrl.validate("://nope.com"), "URL")
        assertErrorMessage(anyUrl.validate(" "), "URL")
    }

    // ---- checked ------------------------------------------------------------

    @Test
    fun checked_rule() {
        val rule = ValidationRules.checked("Must be checked")
        assertErrorMessage(rule.validate(false), "Must be checked")
        assertSuccess(rule.validate(true))
    }

    // ---- minLength / maxLength ---------------------------------------------

    @Test
    fun minLength_and_maxLength() {
        val min3 = ValidationRules.minLength(3, message = "MIN3")
        val max5 = ValidationRules.maxLength(5, message = "MAX5")

        assertErrorMessage(min3.validate("ab"), "MIN3")
        assertSuccess(min3.validate("abc"))
        assertSuccess(min3.validate("abcdef"))

        assertSuccess(max5.validate("abc"))
        assertSuccess(max5.validate("abcde"))
        assertErrorMessage(max5.validate("abcdef"), "MAX5")
    }

    // ---- range (Int) & range (Float) ---------------------------------------

    @Test
    fun range_int_inclusive() {
        val r = ValidationRules.range(min = 2, max = 4, message = "RANGE")
        assertErrorMessage(r.validate(1), "RANGE")
        assertSuccess(r.validate(2))  // min boundary
        assertSuccess(r.validate(3))
        assertSuccess(r.validate(4))  // max boundary
        assertErrorMessage(r.validate(5), "RANGE")
    }

    @Test
    fun range_float_inclusive() {
        val r = ValidationRules.range(min = 0.5f, max = 1.5f, message = "RANGEF")
        assertErrorMessage(r.validate(0.49f), "RANGEF")
        assertSuccess(r.validate(0.5f))
        assertSuccess(r.validate(1.0f))
        assertSuccess(r.validate(1.5f))
        assertErrorMessage(r.validate(1.5001f), "RANGEF")
    }
}
