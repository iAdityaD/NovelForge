package com.novelforge.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.*
import com.novelforge.presentation.SettingsViewModel

@Composable
fun ProjectTemplateSettings(vm: SettingsViewModel, books: List<Book>, templates: List<SavedTemplate>) {
    var adding by rememberSaveable { mutableStateOf(false) }
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { adding = true }, enabled = books.isNotEmpty() && templates.size < 50, modifier = Modifier.fillMaxWidth()) { Text("Save project structure") }
        if (books.isEmpty()) Text("Create a project to save its structure as a template.", style = MaterialTheme.typography.bodySmall)
        templates.forEach { template ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(template.name, style = MaterialTheme.typography.titleMedium)
                    Text("${template.nodes.size} items", style = MaterialTheme.typography.bodySmall)
                }
                ActionIcon(Icons.Outlined.DeleteOutline, "Delete template ${template.name}", { deleting = template.id })
            }
        }
    }
    if (adding) {
        var name by rememberSaveable { mutableStateOf("") }
        var sourceId by rememberSaveable { mutableStateOf(books.firstOrNull()?.id) }
        var includeDocuments by rememberSaveable { mutableStateOf(false) }
        var menu by remember { mutableStateOf(false) }
        var saving by remember { mutableStateOf(false) }
        val error by vm.error.collectAsState()
        LaunchedEffect(error) { if (error != null) saving = false }
        AlertDialog(onDismissRequest = { if (!saving) adding = false }, title = { Text("Save project structure") }, text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(name, { name = it.take(80) }, label = { Text("Template name") }, singleLine = true)
                Box {
                    OutlinedButton(onClick = { menu = true }, modifier = Modifier.fillMaxWidth()) { Text(books.find { it.id == sourceId }?.title ?: "Choose project", Modifier.weight(1f)); Icon(Icons.Outlined.ExpandMore, null) }
                    DropdownMenu(menu, { menu = false }, modifier = Modifier.heightIn(max = 320.dp)) {
                        books.forEach { book -> DropdownMenuItem(text = { Text(book.title) }, onClick = { sourceId = book.id; menu = false }) }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(includeDocuments, { includeDocuments = it })
                    Text("Include empty documents", Modifier.weight(1f))
                }
                Text("Keeps names, types and folder order. Writing content is not copied.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }, confirmButton = { TextButton(onClick = {
            if (!saving) { saving = true; vm.saveTemplate(requireNotNull(sourceId), name, includeDocuments) { adding = false } }
        }, enabled = name.isNotBlank() && books.any { it.id == sourceId } && !saving) { Text("Save template") } }, dismissButton = { TextButton(onClick = { adding = false }, enabled = !saving) { Text("Cancel") } })
    }
    deleting?.let { id -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Delete template?") }, text = { Text("Projects created from it will be kept.") }, confirmButton = { TextButton(onClick = { vm.removeTemplate(id); deleting = null }) { Text("Delete template") } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } }) }
}
