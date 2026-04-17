package dev.voir.formica.schema

import dev.voir.formica.core.FormicaFieldValidationResult
import kotlin.test.*

class SchemaBuilderTest {

    data class User(
        val email: String = "",
        val name: String = "",
        val address: String? = null,
        val age: Int? = null,
        val accepted: Boolean = false,
        val active: Boolean = true
    )

    private val userSchema = schema<User> {
        field(
            property = User::email,
            set = { data, value -> data.copy(email = value ?: "") }
        ) {
            required("Email is required")
            email("Email is invalid")
        }

        field(
            property = User::name,
            set = { data, value -> data.copy(name = value ?: "") }
        ) {
            notBlank("Name is required")
            maxLength(10)
        }

        field(
            property = User::address,
            set = { data, value -> data.copy(address = value) },
            clear = { data -> data.copy(address = null) }
        ) {
            visibleWhen { it.active }
            enabledWhen { it.active }
            notBlank("Address cannot be blank if provided")
        }

        field(
            property = User::age,
            set = { data, value -> data.copy(age = value) },
            clear = { data -> data.copy(age = null) }
        ) {
            min(18)
        }

        field(
            property = User::accepted,
            set = { data, value -> data.copy(accepted = value ?: false) }
        ) {
            checked("Must accept terms")
        }

        objectRule { user ->
            if (user.name == user.email && user.name.isNotBlank()) {
                ObjectRuleResult(
                    fieldErrors = mapOf("name" to "Name must not equal email"),
                    formErrors = listOf("Form level issue")
                )
            } else {
                ObjectRuleResult()
            }
        }
    }

    @Test
    fun `schema exposes registered fields`() {
        assertEquals(5, userSchema.fields.size)
        assertEquals(
            listOf("email", "name", "address", "age", "accepted"),
            userSchema.fields.map { it.key }
        )
    }

    @Test
    fun `field metadata is preserved`() {
        val address = userSchema.fields.first { it.key == "address" }

        assertEquals("address", address.key)
        assertEquals("address", address.property.name)
        assertTrue(address.validateOnChange)
    }

    @Test
    fun `validateField returns first field error`() {
        val result = userSchema.validateField(
            data = User(email = "", name = "John"),
            fieldKey = "email"
        )

        assertIs<FormicaFieldValidationResult.Error>(result)
        assertEquals("Email is required", result.message)
    }

    @Test
    fun `validateField succeeds for valid field`() {
        val result = userSchema.validateField(
            data = User(email = "john@example.com"),
            fieldKey = "email"
        )

        assertEquals(FormicaFieldValidationResult.Success, result)
    }

    @Test
    fun `validateField returns skip for unknown key`() {
        val result = userSchema.validateField(
            data = User(),
            fieldKey = "missing"
        )

        assertEquals(FormicaFieldValidationResult.Skip, result)
    }

    @Test
    fun `validate collects field errors`() {
        val result = userSchema.validate(
            User(
                email = "bad",
                name = "",
                address = "",
                age = 15,
                accepted = false,
                active = true
            )
        )

        assertFalse(result.isValid)
        assertEquals("Email is invalid", result.fieldErrors["email"])
        assertEquals("Name is required", result.fieldErrors["name"])
        assertEquals("Address cannot be blank if provided", result.fieldErrors["address"])
        assertEquals("Must be >= 18.", result.fieldErrors["age"])
        assertEquals("Must accept terms", result.fieldErrors["accepted"])
    }

    @Test
    fun `validate succeeds for valid object`() {
        val result = userSchema.validate(
            User(
                email = "john@example.com",
                name = "John",
                address = "Street 1",
                age = 30,
                accepted = true
            )
        )

        assertTrue(result.isValid)
        assertTrue(result.fieldErrors.isEmpty())
        assertTrue(result.formErrors.isEmpty())
    }

    @Test
    fun `object rule adds field and form errors`() {
        val result = userSchema.validate(
            User(
                email = "same",
                name = "same",
                accepted = true
            )
        )

        assertFalse(result.isValid)
        assertEquals("Name must not equal email", result.fieldErrors["name"])
        assertEquals(listOf("Form level issue"), result.formErrors)
    }

    @Test
    fun `field level error wins over object rule duplicate`() {
        val result = userSchema.validate(
            User(
                email = "",
                name = "",
                accepted = true
            )
        )

        assertEquals("Name is required", result.fieldErrors["name"])
    }

    @Test
    fun `set function updates immutable data`() {
        val emailField = userSchema.fields.first { it.key == "email" }

        @Suppress("UNCHECKED_CAST")
        val typed = emailField as SchemaField<User, String>

        val updated = typed.set(
            User(email = "old@example.com"),
            "new@example.com"
        )

        assertEquals("new@example.com", updated.email)
    }

    @Test
    fun `clear function clears nullable data when provided`() {
        val addressField = userSchema.fields.first { it.key == "address" }

        @Suppress("UNCHECKED_CAST")
        val typed = addressField as SchemaField<User, String?>

        val cleared = typed.clear!!.invoke(
            User(address = "Street 1")
        )

        assertNull(cleared.address)
    }

    @Test
    fun `visibleWhen predicate is preserved`() {
        val addressField = userSchema.fields.first { it.key == "address" }

        @Suppress("UNCHECKED_CAST")
        val typed = addressField as SchemaField<User, String?>

        assertTrue(typed.isVisible(User(active = true)))
        assertFalse(typed.isVisible(User(active = false)))
    }

    @Test
    fun `enabledWhen predicate is preserved`() {
        val addressField = userSchema.fields.first { it.key == "address" }

        @Suppress("UNCHECKED_CAST")
        val typed = addressField as SchemaField<User, String?>

        assertTrue(typed.isEnabled(User(active = true)))
        assertFalse(typed.isEnabled(User(active = false)))
    }

    @Test
    fun `validateOnChange can be overridden`() {
        val schema = schema<User> {
            field(
                property = User::email,
                set = { data, value -> data.copy(email = value ?: "") }
            ) {
                validateOnChange(false)
                email()
            }
        }

        val field = schema.fields.first()

        assertFalse(field.validateOnChange)
    }
}
