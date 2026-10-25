package com.novelforge.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.*

@Composable
fun DocumentDestinationDialog(
    heading: String, initialTitle: String, entries: List<EntrySummary>, source: Boolean = false,
    busy: Boolean = false, onDismiss: () -> Unit,
    onSave: (String, Long?, String?, String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var parent by rememberSaveable { mutableStateOf<Long?>(null) }
    var newFolder by rememberSaveable { mutableStateOf(false) }
    var folderName by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val folders = entries.filter { it.kind == "Folder" }
    fun path(folder: EntrySummary): String {
        val names = mutableListOf(folder.title)
        var next = folder.folderId
        val seen = mutableSetOf(folder.id)
        while (next != null && seen.add(next)) {
            val ancestor = folders.find { it.id == next } ?: break
            names.add(0, ancestor.title); next = ancestor.folderId
        }
        return names.joinToString(" / ")
    }
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text(heading) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it.take(200) }, label = { Text(if (source) "Link name" else "Filename") }, singleLine = true)
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(folders.find { it.id == parent }?.let(::path) ?: "Workspace root", Modifier.weight(1f))
                    Icon(Icons.Outlined.ExpandMore, null)
                }
                DropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 300.dp)) {
                    DropdownMenuItem(text = { Text("Workspace root") }, onClick = { parent = null; expanded = false })
                    folders.forEach { f -> DropdownMenuItem(text = { Text(path(f)) }, onClick = { parent = f.id; expanded = false }) }
                }
            }
            TextButton(onClick = { newFolder = !newFolder }) { Text(if (newFolder) "Use existing folder" else "+ New folder") }
            if (newFolder) OutlinedTextField(folderName, { folderName = it.take(200) }, label = { Text("New folder name") }, singleLine = true)
            if (source) OutlinedTextField(url, { url = it }, label = { Text("Website URL") }, singleLine = true)
        }
    }, confirmButton = { TextButton(onClick = { onSave(title, parent, folderName.takeIf { newFolder }, url) }, enabled = !busy && title.isNotBlank() && (!newFolder || folderName.isNotBlank()) && (!source || url.isNotBlank())) { Text(if (source) "Attach" else "Save") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } })
}
