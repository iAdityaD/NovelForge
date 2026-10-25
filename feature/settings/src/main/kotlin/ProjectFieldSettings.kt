package com.novelforge.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.*
import com.novelforge.presentation.SettingsViewModel
import java.util.UUID

@Composable
fun ProjectFieldSettings(vm: SettingsViewModel, books: List<Book>) {
    var bookId by remember { mutableStateOf(books.firstOrNull()?.id) }
    var picker by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FieldDefinition?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<FieldDefinition?>(null) }
    val book = books.find { it.id == bookId }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box {
            OutlinedButton(onClick = { picker = true }, modifier = Modifier.fillMaxWidth()) { Text(book?.title ?: "Choose project") }
            DropdownMenu(picker, { picker = false }, modifier = Modifier.heightIn(max = 320.dp)) { books.forEach { item -> DropdownMenuItem(text = { Text(item.title) }, onClick = { bookId = item.id; picker = false }) } }
        }
        if (book != null) {
            Button(onClick = { adding = true }, enabled = book.fields.size < 100) { Text("Add field") }
            book.fields.forEach { field ->
                ListItem(headlineContent = { Text(field.name) }, supportingContent = { Text(field.type.name.lowercase().replace('_', ' ')) }, trailingContent = {
                    Row { TextButton(onClick = { editing = field }) { Text("Edit") }; TextButton(onClick = { deleting = field }) { Text("Remove") } }
                })
            }
        }
    }
    if ((adding || editing != null) && book != null) {
        val original = editing
        var name by remember { mutableStateOf(original?.name.orEmpty()) }
        var type by remember { mutableStateOf(original?.type ?: FieldType.TEXT) }
        var choices by remember { mutableStateOf(original?.options?.joinToString("\n").orEmpty()) }
        var typePicker by remember { mutableStateOf(false) }
        var busy by remember { mutableStateOf(false) }
        val error by vm.error.collectAsState()
        LaunchedEffect(error) { if (error != null) busy = false }
        val candidate = FieldDefinition(original?.id ?: "new", name.trim(), type, choices.lines().map { it.trim() }.filter { it.isNotEmpty() })
        val valid = runCatching { validateFields(listOf(candidate)) }.isSuccess
        fun close() { adding = false; editing = null }
        AlertDialog(onDismissRequest = { if (!busy) close() }, title = { Text(if (original == null) "Add field" else "Edit field") }, text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it.take(80) }, label = { Text("Field name") }, singleLine = true)
                Box {
                    OutlinedButton(onClick = { typePicker = true }, enabled = original == null) { Text(type.name.lowercase().replace('_', ' ')) }
                    DropdownMenu(typePicker, { typePicker = false }, modifier = Modifier.heightIn(max = 300.dp)) { FieldType.entries.forEach { value -> DropdownMenuItem(text = { Text(value.name.lowercase().replace('_', ' ')) }, onClick = { type = value; typePicker = false }) } }
                }
                if (type in listOf(FieldType.CHOICE, FieldType.MULTI_CHOICE)) OutlinedTextField(choices, { choices = it }, label = { Text("Choices, one per line") }, maxLines = 6)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }, confirmButton = { TextButton(onClick = {
            busy = true
            val next = candidate.copy(id = original?.id ?: UUID.randomUUID().toString())
            vm.saveFields(book.id, if (original == null) book.fields + next else book.fields.map { if (it.id == next.id) next else it }) { close() }
        }, enabled = valid && !busy) { Text("Save field") } }, dismissButton = { TextButton(onClick = { close() }, enabled = !busy) { Text("Cancel") } })
    }
    deleting?.let { field ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Remove ${field.name}?") }, text = { Text("The field will be hidden. Stored document values are retained in backups.") }, confirmButton = { TextButton(onClick = { book?.let { vm.saveFields(it.id, it.fields.filterNot { f -> f.id == field.id }) { deleting = null } } }) { Text("Remove field") } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } })
    }
}
