package dev.voir.formica.schema

import dev.voir.formica.core.Formica
import dev.voir.formica.core.FormicaFieldValidationResult
import dev.voir.formica.core.FormicaSubmitResult
import kotlin.test.*

class SchemaCoreIntegrationTest {

    data class User(
        val email: String = "",
        val name: String = "",
        val active: Boolean = true,
        val note: String? = null
    )

    private val schema = schema<User> {
        field(
            property = User::email,
            set = { data, value -> data.copy(email = value ?: "") }
        ) {
            required("Email required")
            email("Email invalid")
        }

        field(
            property = User::name,
            set = { data, value -> data.copy(name = value ?: "") }
        ) {
            notBlank("Name required")
        }

        field(
            property = User::note,
            set = { data, value -> data.copy(note = value) },
            clear = { data -> data.copy(note = null) }
        ) {
            visibleWhen { it.active }
            enabledWhen { it.active }
            notBlank("Note invalid")
        }
    }

    @Test
    fun `schema works with formica core field validation`() {
        val formica = Formica(
            adapter = schema,
            initialData = User(email = "", name = "John")
        )

        val result = formica.validateField(User::email)

        assertIs<FormicaFieldValidationResult.Error>(result)
        assertEquals("Email required", result.message)
        assertEquals("Email required", formica.fieldState(User::email).error.value)
    }

    @Test
    fun `schema works with formica core full validation`() {
        val formica = Formica(
            adapter = schema,
            initialData = User(email = "bad", name = "")
        )

        val result = formica.validate()

        assertFalse(result.isValid)
        assertEquals("Email invalid", result.fieldErrors["email"])
        assertEquals("Name required", result.fieldErrors["name"])
        assertIs<FormicaSubmitResult.Error>(formica.submitResult.value)
    }

    @Test
    fun `hidden or disabled schema field is filtered by formica core`() {
        val formica = Formica(
            adapter = schema,
            initialData = User(
                email = "john@example.com",
                name = "John",
                active = false,
                note = ""
            )
        )

        val result = formica.validate()

        assertTrue(result.isValid)
        assertNull(result.fieldErrors["note"])
        assertNull(formica.fieldState(User::note).error.value)
    }

    @Test
    fun `schema set functions update immutable data through formica`() {
        val formica = Formica(
            adapter = schema,
            initialData = User()
        )

        formica.onChange(User::email, "john@example.com")
        formica.onChange(User::name, "John")

        assertEquals("john@example.com", formica.data.value.email)
        assertEquals("John", formica.data.value.name)
    }
}
