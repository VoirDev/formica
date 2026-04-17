package dev.voir.formica.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.voir.formica.compose.FormicaFieldController

@Composable
fun FormicaStringField(
    label: String,
    field: FormicaFieldController<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = field.value ?: "",
            onValueChange = field.onChange,
            isError = field.showError,
            enabled = field.enabled,
            label = { Text(label) }
        )

        if (field.showError && field.error != null) {
            Spacer(Modifier.height(4.dp))
            Text(field.error!!)
        }
    }
}

@Composable
fun FormicaNullableStringField(
    label: String,
    field: FormicaFieldController<String?>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = field.value.orEmpty(),
            onValueChange = field.onChange,
            isError = field.showError,
            enabled = field.enabled,
            label = { Text(label) }
        )

        if (field.showError && field.error != null) {
            Spacer(Modifier.height(4.dp))
            Text(field.error!!)
        }
    }
}

@Composable
fun FormicaIntField(
    label: String,
    field: FormicaFieldController<Int?>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = field.value?.toString().orEmpty(),
            onValueChange = { input ->
                field.onChange(input.toIntOrNull())
            },
            isError = field.showError,
            enabled = field.enabled,
            label = { Text(label) }
        )

        if (field.showError && field.error != null) {
            Spacer(Modifier.height(4.dp))
            Text(field.error!!)
        }
    }
}

@Composable
fun FormicaDoubleField(
    label: String,
    field: FormicaFieldController<Double?>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = field.value?.toString().orEmpty(),
            onValueChange = { input ->
                field.onChange(input.toDoubleOrNull())
            },
            isError = field.showError,
            enabled = field.enabled,
            label = { Text(label) }
        )

        if (field.showError && field.error != null) {
            Spacer(Modifier.height(4.dp))
            Text(field.error!!)
        }
    }
}

@Composable
fun FormicaCheckboxField(
    label: String,
    field: FormicaFieldController<Boolean>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row {
            Checkbox(
                checked = field.value ?: false,
                onCheckedChange = field.onChange,
                enabled = field.enabled
            )
            Text(label)
        }

        if (field.showError && field.error != null) {
            Spacer(Modifier.height(4.dp))
            Text(field.error!!)
        }
    }
}
