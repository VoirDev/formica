package dev.voir.formica

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormicaFieldTest {

    // --- helpers -------------------------------------------------------------

    private fun success() = FormicaFieldResult.Success
    private fun err(msg: String) = FormicaFieldResult.Error(msg)

    private fun <V> rule(block: (V?) -> FormicaFieldResult): ValidationRule<V?> =
        ValidationRule { v -> block(v) }

    // Kotlin's setOf(...) is insertion-ordered (LinkedHashSet), so we can verify short-circuit.
    private fun <V> orderedRules(vararg rules: ValidationRule<V?>): Set<ValidationRule<V?>> =
        linkedSetOf(*rules)

    // --- initial state -------------------------------------------------------

    @Test
    fun initial_state_is_pristine() {
        val f = FormicaField(
            initialValue = "foo"
        )

        assertEquals("foo", f.value.value)
        assertNull(f.error.value)
        assertTrue(f.result.value is FormicaFieldResult.NoInput)
        assertFalse(f.touched.value)
        assertFalse(f.dirty.value)
    }

    // --- onChange / touched / dirty -----------------------------------------

    @Test
    fun onChange_sets_value_and_flags() {
        val f = FormicaField(initialValue = "a", validateOnChange = false)

        f.onChange("a") // same as initial
        assertEquals("a", f.value.value)
        assertTrue(f.touched.value)
        assertFalse(f.dirty.value)

        f.onChange("b") // different
        assertEquals("b", f.value.value)
        assertTrue(f.touched.value)
        assertTrue(f.dirty.value)
    }

    // --- validateOnChange behaviour -----------------------------------------

    @Test
    fun validateOnChange_false_does_not_validate_automatically() {
        val f = FormicaField(
            initialValue = "",
            validators = orderedRules(rule<String> { if (it.isNullOrBlank()) err("x") else success() }),
            validateOnChange = false
        )

        // After change, still NoInput because we didn't call validate()
        f.onChange("")
        assertTrue(f.result.value is FormicaFieldResult.NoInput)
        assertNull(f.error.value)

        // Now validate explicitly
        val r = f.validate()
        assertTrue(r is FormicaFieldResult.Error)
        assertEquals("x", f.error.value)
    }

    @Test
    fun validateOnChange_true_validates_automatically() {
        val f = FormicaField(
            initialValue = "",
            validators = orderedRules(rule<String> { if (it.isNullOrBlank()) err("empty") else success() }),
            validateOnChange = true
        )

        f.onChange("") // triggers validate
        assertEquals("empty", f.error.value)
        assertTrue(f.result.value is FormicaFieldResult.Error)

        f.onChange("ok") // triggers validate
        assertNull(f.error.value)
        assertTrue(f.result.value is FormicaFieldResult.Success)
    }

    // --- validators before custom & short-circuit ----------------------------

    @Test
    fun validators_run_before_custom_and_shortCircuit_on_error() {
        var customCalled = false

        val v1 = rule<String> { err("fail-1") }               // first fails
        val v2 = rule<String> { error("should not run") }     // must never run
        val custom = { _: String? ->
            customCalled = true
            success()
        }

        val f = FormicaField(
            initialValue = "x",
            validators = orderedRules(v1, v2),
            customValidation = custom,
            validateOnChange = false
        )

        val r = f.validate()
        assertTrue(r is FormicaFieldResult.Error)
        assertEquals("fail-1", f.error.value)
        assertFalse(customCalled) // custom not called due to short-circuit
    }

    @Test
    fun custom_runs_when_validators_pass_and_can_fail() {
        var vCalled = false
        val vOk = rule<String> { vCalled = true; success() }
        val custom = { _: String? -> err("custom-fail") }

        val f = FormicaField(
            initialValue = "x",
            validators = orderedRules(vOk),
            customValidation = custom,
            validateOnChange = false
        )

        val r = f.validate()
        assertTrue(vCalled)
        assertTrue(r is FormicaFieldResult.Error)
        assertEquals("custom-fail", f.error.value)
    }

    @Test
    fun success_sets_success_and_clears_error() {
        val f = FormicaField(
            initialValue = "x",
            validators = orderedRules(rule<String> { success() }),
            customValidation = { success() },
            validateOnChange = false
        )

        val r = f.validate()
        assertTrue(r is FormicaFieldResult.Success)
        assertNull(f.error.value)
        assertTrue(f.result.value is FormicaFieldResult.Success)
    }

    // --- enabled / presence --------------------------------------------------

    @Test
    fun disabled_field_skips_validation_and_is_always_success() {
        var vCalls = 0
        val v = rule<String> { vCalls++; err("nope") }

        val f = FormicaField(
            initialValue = "",
            validators = orderedRules(v),
            validateOnChange = true
        )

        // disable first
        f.setEnabled(false)

        // onChange would normally validate, but since disabled, it should stay Success
        f.onChange("")  // validateOnChange triggers validate()
        assertNull(f.error.value)
        assertTrue(f.result.value is FormicaFieldResult.Success)
        assertEquals(0, vCalls, "validator must not be called when disabled")

        // validate() should also short-circuit
        val r = f.validate()
        assertTrue(r is FormicaFieldResult.Success)
        assertEquals(0, vCalls)
    }

    // --- reset ---------------------------------------------------------------

    @Test
    fun reset_restores_pristine_state_and_updates_initial() {
        val f = FormicaField(
            initialValue = "init",
            validators = orderedRules(rule<String> { if (it.isNullOrBlank()) err("empty") else success() }),
            validateOnChange = true
        )

        f.onChange("") // set error, touched, dirty
        assertTrue(f.touched.value)
        assertTrue(f.dirty.value)
        assertEquals("empty", f.error.value)

        f.reset("fresh")
        assertEquals("fresh", f.value.value)
        assertTrue(f.result.value is FormicaFieldResult.NoInput)
        assertNull(f.error.value)
        assertFalse(f.touched.value)
        assertFalse(f.dirty.value)

        // dirty should reflect new initial
        f.onChange("fresh")
        assertFalse(f.dirty.value)
        f.onChange("changed")
        assertTrue(f.dirty.value)
    }

    // --- isValid() -----------------------------------------------------------

    @Test
    fun isValid_calls_validate_and_returns_boolean() {
        val f = FormicaField(
            initialValue = "",
            validators = orderedRules(rule<String> { if (it.isNullOrBlank()) err("x") else success() }),
            validateOnChange = false
        )

        assertFalse(f.isValid()) // triggers validate -> error
        f.onChange("ok")
        assertTrue(f.isValid())
    }
}
