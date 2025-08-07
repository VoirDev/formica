package dev.voir.formica.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.voir.formica.FormicaFieldResult
import dev.voir.formica.FormicaResult
import dev.voir.formica.collectDataAsState
import dev.voir.formica.rememberFormica
import dev.voir.formica.rules.NotEmptyRule
import dev.voir.formica.sample.ui.FormFieldWrapper
import dev.voir.formica.ui.Formica
import dev.voir.formica.ui.FormicaField

data class FormSchema(
    var text: String,
    var optionalText: String?,
    var activateAdditionalText: Boolean,
    var additionalText: String? = null,
)

@Composable
fun App() {
    val formica = rememberFormica(
        initialData = FormSchema(
            text = "",
            optionalText = null,
            activateAdditionalText = false,
            additionalText = null,
        )
    )
    val formData by formica.collectDataAsState()

    var formError by remember { mutableStateOf<String?>(null) }
    var formResult by remember { mutableStateOf<FormSchema?>(null) }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Formica(formica = formica) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FormicaField(
                    name = FormSchema::text,
                    required = true,
                    validators = setOf(NotEmptyRule())
                ) {
                    FormFieldWrapper {
                        androidx.compose.material.TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = field.value!!,
                            label = {
                                Text("Required text")
                            },
                            placeholder = {
                                Text("Some required text")
                            },
                            onValueChange = {
                                onChange(FormSchema::text, it)
                            },
                        )
                        error.value?.let {
                            Text(it, color = Color.Red)
                        }
                    }
                }

                FormicaField(name = FormSchema::optionalText, required = false) {
                    FormFieldWrapper {
                        androidx.compose.material.TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = field.value ?: "",
                            label = {
                                Text("Optional text")
                            },
                            placeholder = {
                                Text("Some optional text")
                            },
                            onValueChange = {
                                onChange(FormSchema::optionalText, it)
                            }
                        )
                        if (error.value != null) {
                            Text(error.value!!, color = Color.Red)
                        }
                    }
                }

                FormicaField(
                    name = FormSchema::activateAdditionalText,
                    required = true,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = field.value ?: false,
                            onCheckedChange = {
                                onChange(FormSchema::activateAdditionalText, it)
                            })
                        Text("Activate additional text?")
                    }
                }

                // if (form.value.activateAdditionalText) {
                FormicaField(
                    name = FormSchema::additionalText,
                    required = false,
                    customValidation = { value ->
                        if (formData.activateAdditionalText && value.isNullOrBlank()) {
                            FormicaFieldResult.Error(message = "Field is required")
                        } else {
                            FormicaFieldResult.Success
                        }
                    }
                ) {
                    FormFieldWrapper {
                        androidx.compose.material.TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = field.value ?: "",
                            label = {
                                Text("Required text if checkbox activated")
                            },
                            placeholder = {
                                Text("Some additional text")
                            },
                            onValueChange = {
                                onChange(FormSchema::additionalText, it)
                            },
                        )
                        error.value?.let {
                            Text(it, color = Color.Red)
                        }
                    }
                }
                //}
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
                    formError = state.message
                    formResult = null
                }
            }) {
            Text("Submit")
        }

        formError?.let {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Form submit error")
                Text(text = it, color = Color.Red)
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
