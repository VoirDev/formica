# Formica

A Kotlin Compose Multiplatform library for managing form states, validations, and data binding in a
declarative way. This library aims to simplify form handling in Compose by providing hooks and
utilities for managing input states, validating fields, and handling errors

### Installation

Add the following dependency to your project:

```gradle
dependencies {
    implementation("dev.voir.formica:1.0.0-alpha01") // Not available on public now, you can publish to mavenLocal.
}
```

### How to use?

Create your form schema

```kotlin
data class FormSchema(
    val firstName: String,
    val lastName: String?,
    val email: String
)
```

Initialize form in your ```kotlin @Composable```

```kotlin
@Composable
fun YourFormComponent() {
    val form = rememberFormica(
        // Pass some initial data if necessary
        initialData = FormSchema(
            firstName = "",
            lastName = null,
            email = ""
        )
    )
    // You can access current form data as state
    val formData by form.collectDataAsState()

    // Or you can access current form result as state
    val formResult by form.collectResultAsState()

    // Pass formica instance
    Column {
        Formica(form) {
            FormicaField(
                name = FormSchema::firstName, // Bind property from schema with FormicaField
                validators = setOf(
                    // Pass validation rules
                    MinLengthRule(
                        2,
                        message = "First name should be at least 2 characters"
                    ),
                    MaxLengthRule(
                        64,
                        message = "First name should not be greater than 64 characters"
                    )
                )
            ) {
                // Access current field value with field.value
                // Modify value in form using onChange callback
                TextField(text = field.value, onValueChange = onChange(FormSchema::firstName, it))
            }
            /* Other form fields... */
        }

        Button(onClick = {
            // Validate form on submit
            val result = form.validate()
            if (result is FormicaResult.Valid) {
                // Form is valid, do some action...
            } else if (result is FormicaResult.Error) {
                // Form is not valid, check validation errors
            }
        }) {
            Text("Submit form")
        }
    }
}
```
