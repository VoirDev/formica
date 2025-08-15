# Formica

**Formica** is a **Kotlin Compose Multiplatform** library for managing **form state**,
**validation**, and **data binding** in a fully declarative way.  
It eliminates boilerplate by giving you **reactive fields**, **built-in validators**, and
**lens-based binding** for immutable form models.

---

## Features

- **Immutable form data model** — no reflection, no direct mutation.
- **Reactive fields** — each field tracks value, error, touched, dirty.
- **Composable API** — `FormicaField` for inline field binding, `FormicaFieldState` for external
  field state.
- **Built-in validation rules** — or plug in your own.
- **Scoped provider** — share the form across nested composables without prop drilling.
- **Type-safe** — works with your data class directly.

---

## Installation

Add the following dependency to your project:

```gradle
dependencies {
    implementation("dev.voir.formica:1.0.0-alpha02") // Not available on public now, you can publish to mavenLocal.
}
```

## Getting started

1. Define your form data schema

```kotlin
data class FormSchema(
    val firstName: String,
    val lastName: String?,
    val email: String,
    val subscribe: Boolean
)
```

2. Create Field IDs (lenses)

```kotlin
val FirstName = FormicaFieldId<FormSchema, String>(
    id = "firstName",
    get = { it.firstName },
    set = { data, value -> data.copy(firstName = value) },
    clear = { data -> data.copy(firstName = "") }
)

val LastName = FormicaFieldId<FormSchema, String?>(
    id = "lastName",
    get = { it.lastName },
    set = { data, value -> data.copy(lastName = value) },
    clear = { data -> data.copy(lastName = null) }
)

val Email = FormicaFieldId<FormSchema, String>(
    id = "email",
    get = { it.email },
    set = { data, value -> data.copy(email = value) }
)

val Subscribe = FormicaFieldId<FormSchema, Boolean>(
    id = "subscribe",
    get = { it.subscribe },
    set = { data, value -> data.copy(subscribe = value) }
)
```

3. Provide the form

```kotlin
@Composable
fun YourFormScreen() {
    val form = rememberFormica(
        initialData = FormSchema("", null, "", false),
        onSubmit = { data ->
            println("Form submitted: $data")
        }
    )

    FormicaProvider(form) {
        YourFormContent()
    }
}
```

4. Build form UI

```kotlin
@Composable
fun YourFormContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Text field example
        FormicaField(
            id = FirstName,
            validators = setOf(
                ValidationRules.minLength(2, "First name must be at least 2 characters"),
                ValidationRules.maxLength(64, "First name must not exceed 64 characters")
            )
        ) { field ->
            TextField(
                value = field.value.orEmpty(),
                onValueChange = field.onChange,
                label = { Text("First Name") },
                isError = field.error != null
            )
            field.error?.let { Text(it, color = Color.Red) }
        }

        // Checkbox example
        FormicaField(id = Subscribe) { field ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = field.value ?: false,
                    onCheckedChange = field.onChange
                )
                Text("Subscribe to newsletter")
            }
        }

        // Conditional field
        FormFieldPresence(
            form = formicaOf(),
            id = Subscribe,
            present = formicaOf<FormSchema>().data.value.subscribe
        ) {
            FormicaField(
                id = Email,
                validators = setOf(
                    ValidationRules.validateOnlyIf(
                        active = { formicaOf<FormSchema>().data.value.subscribe },
                        rule = ValidationRules.email()
                    )
                )
            ) { field ->
                TextField(
                    value = field.value.orEmpty(),
                    onValueChange = field.onChange,
                    label = { Text("Email Address") },
                    isError = field.error != null
                )
                field.error?.let { Text(it, color = Color.Red) }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            val result = formicaOf<FormSchema>().submit()
            if (result is FormicaResult.Valid) {
                println("Form valid, submitting...")
            }
        }) {
            Text("Submit")
        }
    }
}
```

## How to?

#### 1. Reading Field State Outside the Field

Sometimes you want to react to a field’s state elsewhere in the UI:

```kotlin
val subscribeState = rememberFormicaFieldState(Subscribe)

if (subscribeState?.value == true) {
    Text("Thanks for subscribing!")
}
```

#### 2. Use validation Rules

Formica ships with a set of ready-to-use rules:

```kotlin
ValidationRules.required()
ValidationRules.email()
ValidationRules.minLength(3)
ValidationRules.maxLength(50)
ValidationRules.range(0, 100)
ValidationRules.checked()
ValidationRules.validateOnlyIf({ condition }, ValidationRules.required())
```

Or create your own:

```kotlin
val mustBeFoo = ValidationRule<String?> { v ->
    if (v == "foo") FormicaFieldResult.Success
    else FormicaFieldResult.Error("Must be 'foo'")
}
```

## License

This project is licensed under the GNU Lesser General Public License v3.0.

See the full license at https://www.gnu.org/licenses/lgpl-3.0.txt
