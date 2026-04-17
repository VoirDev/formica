package dev.voir.formica.core

import kotlin.test.*

class FormicaTest {

    data class UserForm(
        val email: String = "",
        val name: String = "",
        val age: Int? = null,
        val active: Boolean = true,
        val note: String? = null
    )

    private class TestAdapter(
        override val fields: List<FormicaFieldDefinition<UserForm, *>>
    ) : FormicaValidationAdapter<UserForm> {

        override fun validate(data: UserForm): FormicaValidationResult<UserForm> {
            val errors = buildMap<String, String> {
                if (data.email.isBlank()) {
                    put("email", "Email is required")
                } else if (!data.email.contains("@")) {
                    put("email", "Email is invalid")
                }

                if (data.name.isBlank()) {
                    put("name", "Name is required")
                }

                if (data.age != null && data.age < 18) {
                    put("age", "Must be at least 18")
                }

                if (data.note != null && data.note.isEmpty()) {
                    put("note", "Note cannot be empty if provided")
                }
            }

            return FormicaValidationResult(
                data = data,
                fieldErrors = errors,
                formErrors = if (data.email == "global@error.com") {
                    listOf("Global form error")
                } else {
                    emptyList()
                }
            )
        }

        override fun validateField(
            data: UserForm,
            fieldKey: String
        ): FormicaFieldValidationResult {
            return when (fieldKey) {
                "email" -> when {
                    data.email.isBlank() -> FormicaFieldValidationResult.Error("Email is required")
                    !data.email.contains("@") -> FormicaFieldValidationResult.Error("Email is invalid")
                    else -> FormicaFieldValidationResult.Success
                }

                "name" -> if (data.name.isBlank()) {
                    FormicaFieldValidationResult.Error("Name is required")
                } else {
                    FormicaFieldValidationResult.Success
                }

                "age" -> if (data.age != null && data.age < 18) {
                    FormicaFieldValidationResult.Error("Must be at least 18")
                } else {
                    FormicaFieldValidationResult.Success
                }

                "note" -> if (data.note != null && data.note.isEmpty()) {
                    FormicaFieldValidationResult.Error("Note cannot be empty if provided")
                } else {
                    FormicaFieldValidationResult.Success
                }

                else -> FormicaFieldValidationResult.Skip
            }
        }
    }

    private fun adapter(
        emailValidateOnChange: Boolean = true
    ): TestAdapter {
        val emailField = DefaultFormicaFieldDefinition(
            key = "email",
            property = UserForm::email,
            set = { data, value -> data.copy(email = value ?: "") },
            clear = { data -> data.copy(email = "") },
            validateOnChange = emailValidateOnChange
        )

        val nameField = DefaultFormicaFieldDefinition(
            key = "name",
            property = UserForm::name,
            set = { data, value -> data.copy(name = value ?: "") },
            clear = { data -> data.copy(name = "") }
        )

        val ageField = DefaultFormicaFieldDefinition(
            key = "age",
            property = UserForm::age,
            set = { data, value -> data.copy(age = value) },
            clear = { data -> data.copy(age = null) }
        )

        val activeField = DefaultFormicaFieldDefinition(
            key = "active",
            property = UserForm::active,
            set = { data, value -> data.copy(active = value ?: false) },
            clear = { data -> data.copy(active = false) }
        )

        val noteField = DefaultFormicaFieldDefinition(
            key = "note",
            property = UserForm::note,
            set = { data, value -> data.copy(note = value) },
            clear = { data -> data.copy(note = null) },
            isVisible = { it.active },
            isEnabled = { it.active }
        )

        return TestAdapter(
            fields = listOf(emailField, nameField, ageField, activeField, noteField)
        )
    }

    @Test
    fun `initial state is pristine and uses initial data`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(
                email = "a@b.com",
                name = "John",
                age = 20,
                active = true,
                note = "hi"
            )
        )

        assertEquals("a@b.com", formica.data.value.email)
        assertEquals("John", formica.data.value.name)
        assertFalse(formica.isDirty.value)
        assertFalse(formica.isTouched.value)
        assertFalse(formica.hasErrors.value)
        assertFalse(formica.canSubmit.value)

        val emailField = formica.fieldState(UserForm::email)
        assertEquals("a@b.com", emailField.value.value)
        assertNull(emailField.error.value)
        assertFalse(emailField.dirty.value)
        assertFalse(emailField.touched.value)
        assertTrue(emailField.visible.value)
        assertTrue(emailField.enabled.value)
    }

    @Test
    fun `onChange updates immutable data and field state`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm()
        )

        formica.onChange(UserForm::email, "john@example.com")

        assertEquals("john@example.com", formica.data.value.email)
        assertTrue(formica.isDirty.value)
        assertTrue(formica.isTouched.value)

        val emailField = formica.fieldState(UserForm::email)
        assertEquals("john@example.com", emailField.value.value)
        assertTrue(emailField.dirty.value)
        assertTrue(emailField.touched.value)
    }

    @Test
    fun `validateField stores field error`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "", name = "John")
        )

        val result = formica.validateField(UserForm::email)

        assertIs<FormicaFieldValidationResult.Error>(result)
        assertEquals("Email is required", result.message)

        val emailField = formica.fieldState(UserForm::email)
        assertEquals("Email is required", emailField.error.value)
        assertTrue(formica.hasErrors.value)
    }

    @Test
    fun `validateField stores success for valid field`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "john@example.com", name = "John")
        )

        val result = formica.validateField(UserForm::email)

        assertIs<FormicaFieldValidationResult.Success>(result)
        assertNull(formica.fieldState(UserForm::email).error.value)
        assertFalse(formica.hasErrors.value)
    }

    @Test
    fun `validate validates all visible enabled fields`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "bad", name = "", age = 12, active = true, note = "")
        )

        val result = formica.validate()

        assertFalse(result.isValid)
        assertEquals("Email is invalid", result.fieldErrors["email"])
        assertEquals("Name is required", result.fieldErrors["name"])
        assertEquals("Must be at least 18", result.fieldErrors["age"])
        assertEquals("Note cannot be empty if provided", result.fieldErrors["note"])

        assertEquals(result.fieldErrors, formica.fieldErrors.value)
        assertIs<FormicaSubmitResult.Error>(formica.submitResult.value)
        assertTrue(formica.hasErrors.value)
    }

    @Test
    fun `validate succeeds when data is valid`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(
                email = "john@example.com",
                name = "John",
                age = 30,
                active = true,
                note = "ok"
            )
        )

        val result = formica.validate()

        assertTrue(result.isValid)
        assertTrue(formica.fieldErrors.value.isEmpty())
        assertTrue(formica.formErrors.value.isEmpty())
        assertIs<FormicaSubmitResult.Success>(formica.submitResult.value)
        assertFalse(formica.hasErrors.value)
    }

    @Test
    fun `submit calls callback only when validation succeeds`() {
        var submitted: UserForm? = null

        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "john@example.com", name = "John"),
            onSubmit = { submitted = it }
        )

        val result = formica.submit()

        assertTrue(result.isValid)
        assertEquals(UserForm(email = "john@example.com", name = "John"), submitted)
    }

    @Test
    fun `submit does not call callback when validation fails`() {
        var submitted: UserForm? = null

        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "", name = ""),
            onSubmit = { submitted = it }
        )

        val result = formica.submit()

        assertFalse(result.isValid)
        assertNull(submitted)
        assertIs<FormicaSubmitResult.Error>(formica.submitResult.value)
    }

    @Test
    fun `reset restores pristine state and clears validation`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "start@example.com", name = "Start")
        )

        formica.onChange(UserForm::email, "bad")
        formica.validateField(UserForm::email)

        assertTrue(formica.isDirty.value)
        assertTrue(formica.isTouched.value)
        assertEquals("Email is invalid", formica.fieldState(UserForm::email).error.value)

        formica.reset()

        assertEquals("start@example.com", formica.data.value.email)
        assertFalse(formica.isDirty.value)
        assertFalse(formica.isTouched.value)
        assertFalse(formica.hasErrors.value)
        assertIs<FormicaSubmitResult.NoAttempt>(formica.submitResult.value)

        val emailField = formica.fieldState(UserForm::email)
        assertEquals("start@example.com", emailField.value.value)
        assertFalse(emailField.dirty.value)
        assertFalse(emailField.touched.value)
        assertNull(emailField.error.value)
        assertIs<FormicaFieldValidationResult.Skip>(emailField.validationResult.value)
    }

    @Test
    fun `replaceData behaves like reset to new snapshot`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "a@b.com", name = "Old")
        )

        formica.onChange(UserForm::name, "Changed")

        formica.replaceData(
            UserForm(
                email = "new@example.com",
                name = "New",
                age = 40,
                active = true,
                note = "hello"
            )
        )

        assertEquals("new@example.com", formica.data.value.email)
        assertEquals("New", formica.data.value.name)
        assertFalse(formica.isDirty.value)
        assertFalse(formica.isTouched.value)
        assertNull(formica.fieldState(UserForm::name).error.value)
    }

    @Test
    fun `clear uses field clear function and updates data`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "john@example.com", name = "John", note = "abc")
        )

        formica.clear(UserForm::note)

        assertNull(formica.data.value.note)
        assertTrue(formica.fieldState(UserForm::note).touched.value)
        assertTrue(formica.fieldState(UserForm::note).dirty.value)
    }

    @Test
    fun `hidden or disabled field is skipped during field validation`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(
                email = "john@example.com",
                name = "John",
                active = false,
                note = ""
            )
        )

        val noteState = formica.fieldState(UserForm::note)
        assertFalse(noteState.visible.value)
        assertFalse(noteState.enabled.value)

        val result = formica.validateField(UserForm::note)

        assertIs<FormicaFieldValidationResult.Skip>(result)
        assertIs<FormicaFieldValidationResult.Skip>(noteState.validationResult.value)
        assertNull(noteState.error.value)
    }

    @Test
    fun `hidden or disabled field is ignored in full validation`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(
                email = "john@example.com",
                name = "John",
                active = false,
                note = ""
            )
        )

        val result = formica.validate()

        assertTrue(result.isValid)
        assertFalse(result.fieldErrors.containsKey("note"))
        assertNull(formica.fieldState(UserForm::note).error.value)
    }

    @Test
    fun `availability reacts when controlling data changes`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(active = true, note = "hello")
        )

        assertTrue(formica.fieldState(UserForm::note).visible.value)
        assertTrue(formica.fieldState(UserForm::note).enabled.value)

        formica.onChange(UserForm::active, false)

        assertFalse(formica.fieldState(UserForm::note).visible.value)
        assertFalse(formica.fieldState(UserForm::note).enabled.value)
    }

    @Test
    fun `validateOnChange true validates immediately`() {
        val formica = Formica(
            adapter = adapter(emailValidateOnChange = true),
            initialData = UserForm()
        )

        formica.onChange(UserForm::email, "bad")

        assertEquals("Email is invalid", formica.fieldState(UserForm::email).error.value)
        assertTrue(formica.hasErrors.value)
    }

    @Test
    fun `validateOnChange false does not validate automatically`() {
        val formica = Formica(
            adapter = adapter(emailValidateOnChange = false),
            initialData = UserForm()
        )

        formica.onChange(UserForm::email, "bad")

        assertNull(formica.fieldState(UserForm::email).error.value)
        assertFalse(formica.hasErrors.value)

        val result = formica.validateField(UserForm::email)
        assertIs<FormicaFieldValidationResult.Error>(result)
        assertEquals("Email is invalid", formica.fieldState(UserForm::email).error.value)
    }

    @Test
    fun `snapshot returns current aggregate state`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "", name = "")
        )

        formica.onChange(UserForm::email, "bad")
        formica.validateField(UserForm::email)

        val snapshot = formica.snapshot()

        assertEquals("bad", snapshot.data.email)
        assertTrue(snapshot.isDirty)
        assertTrue(snapshot.isTouched)
        assertTrue(snapshot.hasErrors)
        assertFalse(snapshot.canSubmit)
        assertEquals("Email is invalid", snapshot.fieldErrors["email"])
    }

    @Test
    fun `fieldSnapshot returns current field aggregate state`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm()
        )

        formica.onChange(UserForm::name, "John")

        val snapshot = formica.fieldSnapshot(UserForm::name)

        assertEquals("name", snapshot.key)
        assertEquals("John", snapshot.value)
        assertTrue(snapshot.dirty)
        assertTrue(snapshot.touched)
        assertTrue(snapshot.visible)
        assertTrue(snapshot.enabled)
        assertNull(snapshot.error)
    }

    @Test
    fun `formErrors are propagated from adapter`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "global@error.com", name = "John")
        )

        val result = formica.validate()

        assertFalse(result.isValid)
        assertEquals(listOf("Global form error"), result.formErrors)
        assertEquals(listOf("Global form error"), formica.formErrors.value)
        assertIs<FormicaSubmitResult.Error>(formica.submitResult.value)
    }

    @Test
    fun `canSubmit is true when dirty and no current field errors`() {
        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm(email = "john@example.com", name = "John")
        )

        formica.onChange(UserForm::name, "Johnny")

        assertTrue(formica.isDirty.value)
        assertFalse(formica.hasErrors.value)
        assertTrue(formica.canSubmit.value)
    }

    @Test
    fun `fieldState throws for unknown property`() {
        data class Other(val value: String)

        val formica = Formica(
            adapter = adapter(),
            initialData = UserForm()
        )

        try {
            @Suppress("UNCHECKED_CAST")
            formica.fieldState(Other::value as kotlin.reflect.KProperty1<UserForm, String>)
            kotlin.test.fail("Expected error for unknown property")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("is not registered in adapter"))
        }
    }
}
