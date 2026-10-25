package com.novelforge.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.*

@Composable
fun CustomFieldValues(fields: List<FieldDefinition>, entry: Entry, entries: List<EntrySummary>, dismiss: () -> Unit, save: (Map<String, List<String>>) -> Unit) {
    var values by remember(entry.id) { mutableStateOf(entry.metadata) }
    val error = fields.firstNotNullOfOrNull { field -> runCatching { validateFieldValue(field, values[field.id].orEmpty()) }.exceptionOrNull()?.let { "${field.name}: ${it.message}" } }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Custom fields") }, text = {
        Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (fields.isEmpty()) Text("Add fields in Settings → Writing → Custom fields.")
            fields.forEach { field ->
                val current = values[field.id].orEmpty()
                fun update(next: List<String>) { values = values + (field.id to next) }
                when (field.type) {
                    FieldType.CHOICE, FieldType.MULTI_CHOICE, FieldType.BOOLEAN, FieldType.REFERENCE -> {
                        var expanded by remember(field.id) { mutableStateOf(false) }
                        val options = when (field.type) {
                            FieldType.BOOLEAN -> listOf("true" to "Yes", "false" to "No")
                            FieldType.REFERENCE -> entries.filter { it.bookId == entry.bookId && it.kind != "Folder" }.map { it.id.toString() to "${it.title} · ${it.id}" }
                            else -> field.options.map { it to it }
                        }
                        Text(field.name, style = MaterialTheme.typography.labelLarge)
                        Box {
                            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(current.joinToString { value -> options.find { it.first == value }?.second ?: value }.ifBlank { "Choose" }) }
                            DropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 280.dp)) {
                                DropdownMenuItem(text = { Text("Clear") }, onClick = { update(emptyList()); expanded = false })
                                options.forEach { (key, label) -> DropdownMenuItem(text = { Text(if (key in current) "✓ $label" else label) }, onClick = {
                                    if (field.type == FieldType.MULTI_CHOICE) update(if (key in current) current - key else current + key)
                                    else { update(listOf(key)); expanded = false }
                                }) }
                            }
                        }
                    }
                    else -> OutlinedTextField(current.firstOrNull().orEmpty(), { update(if (it.isBlank()) emptyList() else listOf(it.take(20000))) }, label = { Text(field.name) }, singleLine = field.type != FieldType.MULTILINE, maxLines = 5, modifier = Modifier.fillMaxWidth(), supportingText = { if (field.type == FieldType.DATE) Text("YYYY-MM-DD") })
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(onClick = { save(values); dismiss() }, enabled = error == null) { Text("Save fields") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}
