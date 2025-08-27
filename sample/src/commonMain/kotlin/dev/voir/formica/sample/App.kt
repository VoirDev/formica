package dev.voir.formica.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.voir.formica.FormicaFieldId
import dev.voir.formica.FormicaFieldResult
import dev.voir.formica.FormicaResult
import dev.voir.formica.ValidationRule
import dev.voir.formica.ValidationRules
import dev.voir.formica.sample.ui.FormFieldWrapper
import dev.voir.formica.ui.FormFieldPresence
import dev.voir.formica.ui.FormicaField
import dev.voir.formica.ui.FormicaProvider
import dev.voir.formica.ui.rememberFormica
import dev.voir.formica.ui.rememberFormicaFieldValue

data class FormSchema(
    var text: String,
    var numberRequired: Int,
    var numberOptional: Int?,
    var optionalText: String?,
    var activateAdditionalText: Boolean,
    var additionalText: String? = null,
)

val MainText = FormicaFieldId<FormSchema, String>(
    id = "text",
    get = { it.text },
    set = { d, v -> d.copy(text = v) }
)
val NumberRequired = FormicaFieldId<FormSchema, Int>(
    id = "numberRequired",
    get = { it.numberRequired },
    set = { d, v -> d.copy(numberRequired = v) }
)

val NumberOptional = FormicaFieldId<FormSchema, Int?>(
    id = "numberOptional",
    get = { it.numberOptional },
    set = { d, v -> d.copy(numberOptional = v) }
)

val OptionalText = FormicaFieldId<FormSchema, String?>(
    id = "optionalText",
    get = { it.optionalText },
    set = { d, v -> d.copy(optionalText = v) }
)

val ActivateAdditionalText = FormicaFieldId<FormSchema, Boolean>(
    id = "activateAdditionalText",
    get = { it.activateAdditionalText },
    set = { d, v -> d.copy(activateAdditionalText = v) }
)

val AdditionalText = FormicaFieldId<FormSchema, String?>(
    id = "additionalText",
    get = { it.additionalText },
    set = { d, v -> d.copy(additionalText = v) },
    clear = { d -> d.copy(additionalText = null) }
)


@Composable
fun App() {
    val verticalScroll = rememberScrollState()

    val formica = rememberFormica(
        initialData = FormSchema(
            text = "",
            numberRequired = 0,
            numberOptional = null,
            optionalText = null,
            activateAdditionalText = false,
            additionalText = null,
        )
    )

    var formError by remember { mutableStateOf<FormicaResult.Error?>(null) }
    var formResult by remember { mutableStateOf<FormSchema?>(null) }

    val isActive = rememberFormicaFieldValue(formica, ActivateAdditionalText)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp)
            .verticalScroll(verticalScroll)
    ) {
        FormicaProvider(formica) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FormicaField(
                    id = MainText,
                    validators = setOf(ValidationRules.required())
                ) { field ->
                    FormFieldWrapper {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = field.value.orEmpty(),
                            label = {
                                Text("Required text")
                            },
                            placeholder = {
                                Text("Some required text")
                            },
                            onValueChange = {
                                field.onChange(it)
                            },
                        )

                        if (field.error != null) {
                            Text(field.error!!, color = Color.Red)
                        }
                    }
                }

                FormicaField(id = OptionalText) { field ->
                    FormFieldWrapper {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = field.value.orEmpty(),
                            label = {
                                Text("Optional text")
                            },
                            placeholder = {
                                Text("Some optional text")
                            },
                            onValueChange = {
                                field.onChange(it)
                            },
                        )

                        if (field.error != null) {
                            Text(field.error!!, color = Color.Red)
                        }
                    }
                }

                // Number (required and must be >= 10 if provided)
                FormicaField(
                    id = NumberRequired,
                    validators = setOf(
                        ValidationRules.required(),
                        ValidationRules.min(10)
                    )
                ) { field ->
                    FormFieldWrapper {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = field.value?.toString().orEmpty(),
                            label = {
                                Text("Number (required and >= 10)")
                            },
                            placeholder = {
                                Text("1234")
                            },
                            onValueChange = { s ->
                                field.onChange(s.toIntOrNull())
                            }
                        )

                        if (field.error != null) {
                            Text(field.error!!, color = Color.Red)
                        }
                    }
                }

                // Number (optional, but must be >= 0 if provided)
                FormicaField(
                    id = NumberOptional,
                    validators = setOf(
                        ValidationRule { v ->
                            if (v == null) FormicaFieldResult.Success
                            else if (v < 0) FormicaFieldResult.Error("Number must be non-negative")
                            else FormicaFieldResult.Success
                        }
                    )
                ) { field ->
                    FormFieldWrapper {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = field.value?.toString().orEmpty(),
                            label = {
                                Text("Number (optional, but >= 0 if provided)")
                            },
                            placeholder = {
                                Text("1234")
                            },
                            onValueChange = { s ->
                                field.onChange(s.toIntOrNull())
                            }
                        )

                        if (field.error != null) {
                            Text(field.error!!, color = Color.Red)
                        }
                    }
                }

                FormicaField(id = ActivateAdditionalText) { field ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = field.value ?: false,
                            onCheckedChange = {
                                field.onChange(it)
                            })
                        Text("Activate additional text?")
                    }
                }

                FormicaField(
                    id = AdditionalText,
                    customValidation = { value ->
                        if (value.isNullOrBlank()) {
                            FormicaFieldResult.Error(message = "Field is required")
                        } else {
                            FormicaFieldResult.Success
                        }
                    }
                ) { field ->
                    FormFieldPresence(
                        form = formica,
                        id = AdditionalText,
                        present = isActive == true,
                        clearOnHide = true
                    ) {
                        FormFieldWrapper {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = field.value.orEmpty(),
                                label = {
                                    Text("Required text if checkbox activated")
                                },
                                placeholder = {
                                    Text("Some additional text")
                                },
                                onValueChange = {
                                    field.onChange(it)
                                },
                            )

                            if (field.error != null) {
                                Text(field.error!!, color = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // Reset result and error
                formResult = null
                formError = null

                val state = formica.validate()
                if (state is FormicaResult.Valid) {
                    formError = null
                    formResult = formica.data.value
                } else if (state is FormicaResult.Error) {
                    formError = state
                    formResult = null
                }
            }) {
            Text("Submit")
        }

        formError?.let {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Form submit error")
                Text(text = it.message, color = Color.Red)
                Text(text = it.fieldErrors.toString(), color = Color.Red)
            }
        }

        formResult?.let {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Form submit result")

                Text(text = it.toString())
            }
        }
    }
}
