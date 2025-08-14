package dev.voir.formica

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

// ---------- Fixtures ---------------------------------------------------------

private data class Profile(
    val firstName: String,
    val note: String?,
    val age: Int
)

private val FirstName = FormicaFieldId<Profile, String>(
    id = "firstName",
    get = { it.firstName },
    set = { d, v -> d.copy(firstName = v) }
)

private val Note = FormicaFieldId<Profile, String?>(
    id = "note",
    get = { it.note },
    set = { d, v -> d.copy(note = v) },
    clear = { d -> d.copy(note = null) }
)

private val Age = FormicaFieldId<Profile, Int>(
    id = "age",
    get = { it.age },
    set = { d, v -> d.copy(age = v) }
    // no clear -> null updates should NOT change data snapshot
)

// Helpers
private fun <V> rule(block: (V?) -> FormicaFieldResult): ValidationRule<V?> =
    ValidationRule { v -> block(v) }

// Preserve validator order explicitly for Set
private fun <V> ordered(vararg r: ValidationRule<V?>): Set<ValidationRule<V?>> = linkedSetOf(*r)

// ---------- Tests ------------------------------------------------------------

class FormicaCoreTest {

    @Test
    fun registerField_seeds_initialValue_from_data() {
        val form = Formica(Profile("Ann", null, 21))
        val f = form.registerField(
            id = FirstName,
            validators = emptySet()
        )
        assertEquals("Ann", f.value.value)
        assertTrue(f.result.value is FormicaFieldResult.NoInput)
    }

    @Test
    fun onChange_updates_field_and_data_immutably() {
        val form = Formica(Profile("Ann", null, 21))
        form.registerField(id = FirstName, validators = emptySet())

        val before = form.data.value
        form.onChange(FirstName, "Bob")

        val after = form.data.value
        assertNotSame(before, after)                 // new snapshot
        assertEquals("Bob", after.firstName)         // changed
        assertEquals("Ann", before.firstName)        // old unchanged
    }

    @Test
    fun onChange_null_uses_clear_when_available() {
        val form = Formica(Profile("Ann", "hello", 21))
        val field = form.registerField(id = Note, validators = emptySet())

        // sanity: both data and field start with "hello"
        assertEquals("hello", form.data.value.note)
        assertEquals("hello", field.value.value)

        form.onChange(Note, null) // should call FormicaFieldId.clear
        assertNull(form.data.value.note)             // data cleared
        assertNull(field.value.value)                // field value updated
    }

    @Test
    fun onChange_null_without_clear_does_not_mutate_data_snapshot() {
        val form = Formica(Profile("Ann", "x", 30))
        val ageField = form.registerField(id = Age, validators = emptySet())

        val before = form.data.value
        assertEquals(30, before.age)

        form.onChange(Age, null) // no clear provided

        val after = form.data.value
        assertSame(before, after)                   // same instance -> no change
        assertEquals(30, after.age)
        assertNull(ageField.value.value)            // field state still updated to null
    }

    @Test
    fun getRegisteredField_returns_same_instance_and_can_toggle_enabled() {
        val form = Formica(Profile("Ann", null, 21))
        form.registerField(
            id = FirstName,
            validators = ordered(rule<String> { FormicaFieldResult.Error("fail") }),
            validateOnChange = true
        )

        val f = form.getRegisteredField(FirstName)
        assertNotNull(f)
        // Disable -> validation should short-circuit to Success
        f.setEnabled(false)
        form.onChange(FirstName, "") // would fail if enabled
        assertTrue(f.result.value is FormicaFieldResult.Success)
        assertNull(f.error.value)
    }

    @Test
    fun validate_aggregates_errors_with_fieldIds() {
        val form = Formica(Profile("Ann", "", 5))
        form.registerField(
            id = FirstName,
            validators = ordered(rule<String> { if (it.isNullOrBlank()) FormicaFieldResult.Error("first required") else FormicaFieldResult.Success })
        )
        form.registerField(
            id = Note,
            validators = ordered(rule<String> { FormicaFieldResult.Success }) // note ok
        )

        // Make firstName empty -> should produce an error map entry
        form.onChange(FirstName, "")
        val res = form.validate()
        assertTrue(res is FormicaResult.Error)
        val err = res as FormicaResult.Error
        assertEquals(mapOf("firstName" to "first required"), err.fieldErrors)
    }

    @Test
    fun validator_order_shortCircuits_and_custom_runs_last() {
        var v1Called = false
        var v2Called = false
        var customCalled = false

        val form = Formica(Profile("Ann", null, 21))
        form.registerField(
            id = FirstName,
            validators = ordered(
                rule<String> { v1Called = true; FormicaFieldResult.Error("v1") },
                rule<String> { v2Called = true; error("should not be called") }
            ),
            customValidation = { customCalled = true; FormicaFieldResult.Success },
            validateOnChange = false
        )

        val res = form.validate()
        assertTrue(res is FormicaResult.Error)
        assertTrue(v1Called)
        assertFalse(v2Called)         // short-circuited
        assertFalse(customCalled)     // skipped because validator failed
    }

    @Test
    fun syncFromData_resets_fields_to_match_current_snapshot() {
        val form = Formica(Profile("Ann", "hello", 21))
        val noteField =
            form.registerField(id = Note, validators = emptySet(), validateOnChange = true)

        // Mutate field (and make it dirty)
        form.onChange(Note, "changed")
        assertEquals("changed", noteField.value.value)
        assertTrue(noteField.dirty.value)

        // Mutate DATA without touching field state via clear()
        form.clear(Note)
        assertNull(form.data.value.note)
        // Field value is still "changed" and dirty at this moment
        assertEquals("changed", noteField.value.value)

        // Now sync field state from DATA snapshot
        form.syncFromData()
        assertNull(noteField.value.value)
        assertTrue(noteField.result.value is FormicaFieldResult.NoInput)
        assertFalse(noteField.dirty.value)
        assertFalse(noteField.touched.value)
    }

    @Test
    fun submit_invokes_onSubmit_only_when_valid() {
        var submitted: Profile? = null
        val form = Formica(
            initialData = Profile("Ann", null, 21),
            onSubmit = { submitted = it }
        )

        // FirstName required
        form.registerField(
            id = FirstName,
            validators = ordered(rule<String> { if (it.isNullOrBlank()) FormicaFieldResult.Error("required") else FormicaFieldResult.Success })
        )

        // Make invalid
        form.onChange(FirstName, "")
        val r1 = form.submit()
        assertTrue(r1 is FormicaResult.Error)
        assertNull(submitted)

        // Fix and submit again
        form.onChange(FirstName, "Ok")
        val r2 = form.submit()
        assertTrue(r2 is FormicaResult.Valid)
        assertNotNull(submitted)
        assertEquals("Ok", submitted!!.firstName)
    }

    @Test
    fun clear_updates_data_only_and_leaves_field_state_as_is() {
        val form = Formica(Profile("Ann", "keep", 21))
        val noteField =
            form.registerField(id = Note, validators = emptySet(), validateOnChange = false)

        // Change field & data to "text"
        form.onChange(Note, "text")
        assertEquals("text", noteField.value.value)
        assertEquals("text", form.data.value.note)

        // Clear via form.clear -> data changes, field state remains "text"
        form.clear(Note)
        assertNull(form.data.value.note)
        assertEquals("text", noteField.value.value) // unchanged!
        // Caller can optionally call noteField.reset(...) or form.syncFromData()
    }
}
