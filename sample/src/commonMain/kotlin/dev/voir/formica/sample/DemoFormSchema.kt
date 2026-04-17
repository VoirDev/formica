package dev.voir.formica.sample

import dev.voir.formica.schema.*

val DemoFormSchema = schema<DemoFormData> {
    field(
        property = DemoFormData::email,
        set = { data, value -> data.copy(email = value ?: "") }
    ) {
        required("Email is required")
        email("Email is invalid")
    }

    field(
        property = DemoFormData::name,
        set = { data, value -> data.copy(name = value ?: "") }
    ) {
        notBlank("Name is required")
        minLength(2, "Name must have at least 2 characters")
        maxLength(50, "Name must have at most 50 characters")
    }

    field(
        property = DemoFormData::nickname,
        set = { data, value -> data.copy(nickname = value) },
        clear = { data -> data.copy(nickname = null) }
    ) {
        notEmpty("Nickname cannot be empty if provided")
        maxLength(20, "Nickname must have at most 20 characters")
    }

    field(
        property = DemoFormData::password,
        set = { data, value -> data.copy(password = value ?: "") }
    ) {
        required("Password is required")
        strongPassword()
    }

    field(
        property = DemoFormData::website,
        set = { data, value -> data.copy(website = value) },
        clear = { data -> data.copy(website = null) }
    ) {
        url(
            protocolRequired = true,
            message = "Website must be a valid URL with http:// or https://"
        )
    }

    field(
        property = DemoFormData::age,
        set = { data, value -> data.copy(age = value) },
        clear = { data -> data.copy(age = null) }
    ) {
        min(18, message = { "Age must be at least $it" })
        max(120, message = { "Age must be at most $it" })
    }

    field(
        property = DemoFormData::score,
        set = { data, value -> data.copy(score = value) },
        clear = { data -> data.copy(score = null) }
    ) {
        range(
            min = 0.0,
            max = 100.0,
            inclusive = true,
            message = { lo, hi -> "Score must be between $lo and $hi" }
        )
    }

    field(
        property = DemoFormData::acceptedTerms,
        set = { data, value -> data.copy(acceptedTerms = value ?: false) }
    ) {
        checked("You must accept terms")
    }

    field(
        property = DemoFormData::hasSecondaryAddress,
        set = { data, value -> data.copy(hasSecondaryAddress = value ?: false) }
    )

    field(
        property = DemoFormData::secondaryAddress,
        set = { data, value -> data.copy(secondaryAddress = value) },
        clear = { data -> data.copy(secondaryAddress = null) }
    ) {
        visibleWhen { it.hasSecondaryAddress }
        enabledWhen { it.hasSecondaryAddress }
        notBlank("Secondary address is required when enabled")
        minLength(5, "Secondary address must have at least 5 characters")
    }

    field(
        property = DemoFormData::manualValidationField,
        set = { data, value -> data.copy(manualValidationField = value ?: "") }
    ) {
        validateOnChange(false)
        notBlank("This field is validated only when requested")
        minLength(4, "Manual field must have at least 4 characters")
    }

    objectRule { data ->
        val fieldErrors = mutableMapOf<String, String>()
        val formErrors = mutableListOf<String>()

        if (data.name.isNotBlank() && data.email.isNotBlank() && data.name == data.email) {
            fieldErrors["name"] = "Name must not be the same as email"
        }

        if (data.age != null && data.score != null && data.age < 21 && data.score > 90.0) {
            formErrors += "Users under 21 cannot have score above 90 in this demo rule"
        }

        ObjectRuleResult(
            fieldErrors = fieldErrors,
            formErrors = formErrors
        )
    }
}
