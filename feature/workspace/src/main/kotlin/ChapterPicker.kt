package com.novelforge.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.EntrySummary

@Composable
fun ChapterPicker(entries: List<EntrySummary>, included: Set<Long>, busy: Boolean,
    onDismiss: () -> Unit, onWrite: () -> Unit, onAdd: (List<Long>) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf(listOf<Long>()) }
    val folders = entries.filter { it.kind == "Folder" }
    val chapters = entries.filter { it.kind == "Chapter" }.sortedWith(compareBy<EntrySummary> { it.position }.thenBy { it.id })
    val chosen = chapters.filter { it.id in selected && it.id !in included }
    val visible = chapters.filter { it.title.contains(query, true) }
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text("Add chapters") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(query, { query = it }, label = { Text("Search chapters") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            when {
                chapters.isEmpty() -> { Text("No chapters yet"); TextButton(onClick = onWrite) { Text("Go to Write") } }
                chapters.all { it.id in included } -> Text("All chapters added")
                visible.isEmpty() -> Text("No matching chapters")
            }
            LazyColumn(Modifier.heightIn(max = 340.dp)) {
                items(visible, key = { it.id }) { chapter ->
                    val added = chapter.id in included
                    Row(Modifier.fillMaxWidth().toggleable(value = added || chapter.id in selected, enabled = !added && !busy, role = Role.Checkbox,
                        onValueChange = { checked -> selected = if (checked) selected + chapter.id else selected - chapter.id }).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = added || chapter.id in selected, onCheckedChange = null, enabled = !added && !busy)
                        Column(Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(chapter.title, style = MaterialTheme.typography.titleSmall)
                            Text(folders.find { it.id == chapter.folderId }?.let { resourceFolderPath(it, folders) } ?: "Workspace root", style = MaterialTheme.typography.bodySmall)
                            if (added) Text("Added", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(enabled = chosen.isNotEmpty() && !busy, onClick = { onAdd(chosen.map { it.id }) }) { Text("Add selected (${chosen.size})") } },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Cancel") } })
}
