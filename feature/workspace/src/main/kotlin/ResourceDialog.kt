package com.novelforge.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.*
import com.novelforge.domain.workspace.MetadataPolicy

/** Categories are existing folders, never a fixed taxonomy imposed on scratch workspaces. */
@Composable
fun ResourceDialog(folders: List<EntrySummary>, saving: Boolean, onDismiss: () -> Unit,
    onSave: (String, Long?, String?, String?, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var folder by rememberSaveable { mutableStateOf<Long?>(null) }
    var createCategory by rememberSaveable { mutableStateOf(folders.isEmpty()) }
    var categoryName by rememberSaveable { mutableStateOf("") }
    var external by rememberSaveable { mutableStateOf(false) }
    var url by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val validCategory = if (createCategory) runCatching { MetadataPolicy.folder(categoryName) }.isSuccess else folders.any { it.id == folder }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New resource") }, text = {
        Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it.take(200) }, label = { Text("Resource title") }, singleLine = true)
            Text("Category", style = MaterialTheme.typography.titleSmall)
            Text("Categories are folders in this workspace. Your resource is saved inside the one you choose.", style = MaterialTheme.typography.bodySmall)
            folders.forEach { item ->
                Row(Modifier.fillMaxWidth().clickable { folder = item.id; createCategory = false }) {
                    RadioButton(selected = !createCategory && folder == item.id, onClick = { folder = item.id; createCategory = false })
                    Text(resourceFolderPath(item, folders), Modifier.padding(top = 12.dp))
                }
            }
            TextButton(onClick = { createCategory = true; folder = null }) { Text("+ New category") }
            TextButton(onClick = { createCategory = true; folder = null; categoryName = "External sources"; external = true }) { Text("+ External sources category") }
            if (createCategory) OutlinedTextField(categoryName, { categoryName = it.take(200) }, label = { Text("Category name") }, singleLine = true)
            Row {
                Checkbox(external, { external = it })
                Text("External website or article", Modifier.padding(top = 12.dp))
            }
            if (external) {
                OutlinedTextField(url, { url = it }, label = { Text("Source URL") }, placeholder = { Text("https://example.com/article") }, singleLine = true)
                Text("Saves the link and your notes locally. Article content is not downloaded.", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.heightIn(max = 150.dp))
        }
    }, confirmButton = {
        TextButton(enabled = !saving && title.isNotBlank() && validCategory && (!external || url.isNotBlank()), onClick = {
            onSave(title, if (createCategory) null else folder, if (createCategory) categoryName else null, if (external) url else null, notes)
        }) { Text("Create resource") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

internal fun resourceFolderPath(folder: EntrySummary, folders: List<EntrySummary>): String {
    val parts = mutableListOf(folder.title)
    val seen = mutableSetOf(folder.id)
    var parent = folder.folderId
    while (parent != null && seen.add(parent)) {
        val item = folders.find { it.id == parent } ?: break
        parts.add(0, item.title); parent = item.folderId
    }
    return parts.joinToString(" / ")
}

@Composable
fun SynopsisDialog(book: Book, limit: Int, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by rememberSaveable(book.id) { mutableStateOf(book.synopsis) }
    val valid = MetadataPolicy.acceptsSynopsis(book.synopsis, text, limit)
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Story synopsis") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(text, { text = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 260.dp), label = { Text("Short story summary") }, isError = !valid)
            Text("${wordCount(text)} / $limit words", color = if (valid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
            Text("Change the limit in Settings → Writing → Content limits.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(enabled = valid, onClick = { onSave(text) }) { Text("Save synopsis") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
