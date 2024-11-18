package dev.voir.formica.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.voir.formica.FormicaFieldResult
import dev.voir.formica.FormicaState
import dev.voir.formica.rules.NotEmptyRule
import dev.voir.formica.sample.ui.FormFieldWrapper
import dev.voir.formica.ui.Formica
import dev.voir.formica.ui.FormicaField
import dev.voir.formica.ui.rememberFormica

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
    var formError by remember { mutableStateOf<String?>(null) }

    Column {
        Formica(formica = formica) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormicaField(
                    name = FormSchema::text,
                    required = true,
                    validators = setOf(NotEmptyRule())
                ) {
                    FormFieldWrapper {
                        androidx.compose.material.TextField(
                            value = field.value ?: "",
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
                    Row {
                        Text("Activate additional text?")
                        Checkbox(
                            checked = field.value ?: false,
                            onCheckedChange = {
                                onChange(FormSchema::activateAdditionalText, it)
                            })
                    }
                }

                // if (form.value.activateAdditionalText) {
                FormicaField(
                    name = FormSchema::additionalText,
                    required = false,
                    customValidation = { value ->
                        println("Custom validation fire: on $data with $value")
                        if (data.activateAdditionalText && value.isNullOrBlank()) {
                            FormicaFieldResult.Error(message = "Field is required")
                        } else {

                            FormicaFieldResult.Success
                        }
                    }
                ) {
                    FormFieldWrapper {
                        androidx.compose.material.TextField(
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

        formError?.let {
            Text(text = it, color = Color.Red)
        }

        Button(
            onClick = {
                formError = null

                val state = formica.validate()
                if (state is FormicaState.Valid) {
                    formError = null
                    println(formica.data.value)
                } else if (state is FormicaState.Error) {
                    formError = state.message
                }
            }) {
            Text("Submit")
        }
    }
}
