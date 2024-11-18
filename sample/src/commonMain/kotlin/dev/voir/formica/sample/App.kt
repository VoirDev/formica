package dev.voir.formica.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.voir.formica.FormicaState
import dev.voir.formica.rules.NotEmptyRule
import dev.voir.formica.sample.ui.FormFieldWrapper
import dev.voir.formica.ui.Formica
import dev.voir.formica.ui.FormicaField
import dev.voir.formica.ui.rememberFormica

data class FormSchema(
    var text: String,
    var optionalText: String?,
)

@Composable
fun App() {
    val form = rememberFormica(
        initialData = FormSchema(
            text = "",
            optionalText = null
        ),
        onValid = { data ->
            // TODO
        },
        onInvalid = {
            // TODO
        }
    )

    val data by form.data.collectAsState()
    val state by form.state.collectAsState()

    Column {
        Formica(form = form) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormicaField(
                    name = FormSchema::text,
                    required = true,
                    validators = setOf(
                        NotEmptyRule(),
                    )
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
            }
        }

        Button(onClick = {
            if (state is FormicaState.Valid) {
                println(data)
            } else {
                println("Some of the form fields not valid")
            }
        }) {
            Text("Submit")
        }
    }
}
