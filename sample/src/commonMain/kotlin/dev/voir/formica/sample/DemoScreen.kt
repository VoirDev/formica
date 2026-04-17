package dev.voir.formica.sample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.voir.formica.compose.Formica
import dev.voir.formica.compose.rememberFormica

@Composable
fun DemoScreen() {
    val initialData = remember {
        DemoFormData(
            email = "",
            name = "",
            nickname = null,
            password = "",
            website = null,
            age = null,
            score = null,
            acceptedTerms = false,
            hasSecondaryAddress = false,
            secondaryAddress = null,
            manualValidationField = ""
        )
    }

    val formica = rememberFormica(
        adapter = DemoFormSchema,
        initialData = initialData
    )

    Formica(formica) { form ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Formica demo")

            Divider()

            Text("Required + email")
            Field(DemoFormData::email) { field ->
                FormicaStringField(
                    label = "Email",
                    field = field
                )
            }

            Text("Required + min/max length")
            Field(DemoFormData::name) { field ->
                FormicaStringField(
                    label = "Name",
                    field = field
                )
            }

            Text("Optional field: null is allowed, empty string is not")
            Field(DemoFormData::nickname) { field ->
                FormicaNullableStringField(
                    label = "Nickname (optional)",
                    field = field
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { formica.clear(DemoFormData::nickname) }
                ) {
                    Text("Clear nickname")
                }
            }

            Divider()

            Text("Strong password")
            Field(DemoFormData::password) { field ->
                FormicaStringField(
                    label = "Password",
                    field = field
                )
            }

            Text("Optional URL with protocol required")
            Field(DemoFormData::website) { field ->
                FormicaNullableStringField(
                    label = "Website",
                    field = field
                )
            }

            Divider()

            Text("Int min/max")
            Field(DemoFormData::age) { field ->
                FormicaIntField(
                    label = "Age",
                    field = field
                )
            }

            Text("Double range")
            Field(DemoFormData::score) { field ->
                FormicaDoubleField(
                    label = "Score",
                    field = field
                )
            }

            Divider()

            Text("Checkbox with checked() validation")
            Field(DemoFormData::acceptedTerms) { field ->
                FormicaCheckboxField(
                    label = "Accept terms",
                    field = field
                )
            }

            Divider()

            Text("Conditional field visibility / enabled state")
            Field(DemoFormData::hasSecondaryAddress) { field ->
                FormicaCheckboxField(
                    label = "Provide secondary address",
                    field = field
                )
            }

            Field(DemoFormData::secondaryAddress) { field ->
                FormicaNullableStringField(
                    label = "Secondary address",
                    field = field
                )
            }

            Divider()

            Text("Manual validation field")
            Field(DemoFormData::manualValidationField) { field ->
                FormicaStringField(
                    label = "Manual validation field",
                    field = field
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            field.validate()
                        },
                        enabled = field.enabled
                    ) {
                        Text("Validate this field")
                    }

                    Button(
                        onClick = field.clear,
                        enabled = field.enabled
                    ) {
                        Text("Clear this field")
                    }
                }
            }

            Divider()

            Text("Form actions")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        form.validate()
                    }
                ) {
                    Text("Validate form")
                }

                Button(
                    onClick = {
                        form.submit()
                    },
                    enabled = true
                ) {
                    Text("Submit")
                }

                Button(
                    onClick = form.reset
                ) {
                    Text("Reset")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        form.replaceData(
                            DemoFormData(
                                email = "preset@example.com",
                                name = "Preset User",
                                nickname = "PresetNick",
                                password = "Password1!",
                                website = "https://example.com",
                                age = 30,
                                score = 88.5,
                                acceptedTerms = true,
                                hasSecondaryAddress = true,
                                secondaryAddress = "Preset secondary address",
                                manualValidationField = "abcd"
                            )
                        )
                    }
                ) {
                    Text("Load preset")
                }

                Button(
                    onClick = {
                        form.replaceData(DemoFormData())
                    }
                ) {
                    Text("Load empty")
                }
            }

            Divider()

            Text("Form state")
            Text("isDirty = ${form.isDirty}")
            Text("isTouched = ${form.isTouched}")
            Text("hasErrors = ${form.hasErrors}")
            Text("canSubmit = ${form.canSubmit}")
            Text("submitResult = ${form.submitResult}")

            Divider()

            Text("Field errors")
            if (form.fieldErrors.isEmpty()) {
                Text("No field errors")
            } else {
                form.fieldErrors.forEach { (key, value) ->
                    Text("$key: $value")
                }
            }

            Divider()

            Text("Form errors")
            if (form.formErrors.isEmpty()) {
                Text("No form errors")
            } else {
                form.formErrors.forEach { error ->
                    Text(error)
                }
            }

            Divider()

            Text("Current data")
            Text(form.data.toString())
        }
    }
}
