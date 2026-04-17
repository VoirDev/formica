# Formica

**Formica** is a lightweight, Kotlin Multiplatform-friendly form engine with:

- Schema-based validation (no annotations, no reflection magic)
- Reactive form state (per-field + whole form)
- Compose-first API (but UI-agnostic core)
- Full control over validation behavior
- Immutable data support

---

## Features

- **Schema DSL** (clean, composable validation)
- **No annotations / no codegen**
- **Field-level + form-level validation**
- **Optional & conditional fields**
- **Reactive state (dirty, touched, errors, etc.)**
- **Compose integration**
- **Works in Kotlin Multiplatform**

---

## Modules

```text
formica.core     → runtime engine (state, validation, form logic)
formica.schema   → schema DSL + validation rules
formica.compose  → Compose integration layer
```

---

## Quick Start

### 1. Define your model

```kotlin
data class NewUser(
    val email: String = "",
    val name: String = "",
    val address: String? = null,
    val age: Int? = null,
    val acceptedTerms: Boolean = false
)
```

---

### 2. Create a schema

```kotlin
val NewUserSchema = schema<NewUser> {

    field(
        property = NewUser::email,
        set = { data, value -> data.copy(email = value ?: "") }
    ) {
        required("Email is required")
        email("Invalid email")
    }

    field(
        property = NewUser::name,
        set = { data, value -> data.copy(name = value ?: "") }
    ) {
        notBlank("Name is required")
        maxLength(50)
    }

    field(
        property = NewUser::address,
        set = { data, value -> data.copy(address = value) },
        clear = { data -> data.copy(address = null) }
    ) {
        notBlank("Address cannot be blank if provided")
    }

    field(
        property = NewUser::age,
        set = { data, value -> data.copy(age = value) },
        clear = { data -> data.copy(age = null) }
    ) {
        min(18)
    }

    field(
        property = NewUser::acceptedTerms,
        set = { data, value -> data.copy(acceptedTerms = value ?: false) }
    ) {
        checked("You must accept terms")
    }
}
```

---

### 3. Use in Compose

```kotlin
@Composable
fun NewUserScreen() {
    val formica = rememberFormica(
        adapter = NewUserSchema,
        initialData = NewUser()
    )

    Formica(formica) { form ->

        Field(NewUser::email) { field ->
            Column {
                OutlinedTextField(
                    value = field.value ?: "",
                    onValueChange = field.onChange,
                    isError = field.showError
                )

                if (field.showError && field.error != null) {
                    Text(field.error)
                }
            }
        }

        Button(
            onClick = form.submit,
            enabled = form.canSubmit
        ) {
            Text("Submit")
        }
    }
}
```

---

## Core Concepts

### Schema-first validation

No annotations. No reflection scanning.

Validation is explicitly defined:

```kotlin
required()
email()
min(18)
```

---

### Reactive form state

Each field tracks:

```kotlin
value
error
dirty
touched
visible
enabled
```

The form tracks:

```kotlin
isDirty
isTouched
hasErrors
canSubmit
fieldErrors
formErrors
submitResult
```

---

### Optional fields

```kotlin
val nickname: String? = null
```

Rules like `notBlank()`:

- skip if `null`
- validate if not null

---

### Conditional fields

```kotlin
visibleWhen { it.hasSecondaryAddress }
enabledWhen { it.hasSecondaryAddress }
```

Hidden/disabled fields are automatically ignored during validation.

---

### Manual validation

```kotlin
validateOnChange(false)
field.validate()
```

---

### Object-level validation

```kotlin
objectRule { data ->
    if (data.name == data.email) {
        ObjectRuleResult(
            fieldErrors = mapOf("name" to "Name must not equal email")
        )
    } else {
        ObjectRuleResult()
    }
}
```

---

## Built-in Rules

### Strings

- notEmpty()
- notBlank()
- email()
- strongPassword()
- url()
- minLength()
- maxLength()

### Numbers

- min()
- max()
- range()

### Boolean

- checked()

### Generic

- required()
- validateOnlyIf()

---
