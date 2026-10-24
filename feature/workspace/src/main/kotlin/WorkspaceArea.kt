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
import com.novelforge.presentation.WorkspaceViewModel

@Composable
internal fun WorkspaceArea(vm: WorkspaceViewModel, book: Book, entries: List<EntrySummary>, area: String, arrange: Boolean, onOpen: (Long) -> Unit, modifier: Modifier) {
    var selected by rememberSaveable(book.id, area) { mutableStateOf<Long?>(null) }
    var add by remember { mutableStateOf(false) }
    var folder by remember { mutableStateOf<Boolean?>(null) }
    val visible = remember(entries, area) { areaEntries(entries, area) }
    val destination = selected?.takeIf { id -> visible.any { it.id == id && it.kind == "Folder" } }
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (area == PLAN_AREA) "Resources" else if (arrange) "Arrange manuscript" else "Manuscript", Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
            Box {
                ActionIcon(Icons.Outlined.Add, if (area == PLAN_AREA) "Add planning item" else "Add manuscript item", { add = true })
                DropdownMenu(add, { add = false }) {
                    DropdownMenuItem(text = { Text(if (area == PLAN_AREA) "New resource" else "New document") }, onClick = { add = false; folder = false })
                    DropdownMenuItem(text = { Text("New folder") }, onClick = { add = false; folder = true })
                }
            }
        }
        ExplorerTree(vm, book, visible, onOpen, { selected = it }, destination, Modifier.weight(1f), rootLabel = if (area == PLAN_AREA) "Plan" else "Manuscript", arrange = arrange)
    }
    folder?.let { isFolder -> WorkspaceNodeDialog(vm, book.id, visible, area, destination, isFolder, { folder = null }) { id -> folder = null; if (isFolder) selected = id else onOpen(id) } }
}

@Composable
internal fun WorkspaceNodeDialog(vm: WorkspaceViewModel, bookId: String, entries: List<EntrySummary>, area: String, parent: Long?, folder: Boolean, onDismiss: () -> Unit, onCreated: (Long) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var label by rememberSaveable { mutableStateOf("") }
    var source by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf(parent) }
    var submitting by remember { mutableStateOf(false) }
    val folders = entries.filter { it.kind == "Folder" }
    // Errors are surfaced by the feature host; allow retry after the operation ends.
    val error by vm.error.collectAsState()
    LaunchedEffect(error) { if (error != null) submitting = false }
    AlertDialog(onDismissRequest = { if (!submitting) onDismiss() }, title = { Text(if (folder) "New folder" else if (area == PLAN_AREA) "New resource" else "New document") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(title, { title = it.take(200) }, label = { Text(if (folder) "Folder name" else "Title") }, singleLine = true)
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(folders.find { it.id == destination }?.title ?: "Top level", Modifier.weight(1f)); Icon(Icons.Outlined.ExpandMore, null) }
                    DropdownMenu(expanded, { expanded = false }) {
                        DropdownMenuItem(text = { Text("Top level") }, onClick = { destination = null; expanded = false })
                        folders.forEach { f -> DropdownMenuItem(text = { Text(resourceFolderPath(f, folders)) }, onClick = { destination = f.id; expanded = false }) }
                    }
                }
                if (!folder) {
                    OutlinedTextField(label, { label = it.take(80) }, label = { Text("Type label (optional)") }, singleLine = true)
                    if (area == PLAN_AREA) OutlinedTextField(source, { source = it }, label = { Text("Source URL (optional)") }, singleLine = true)
                }
            }
        }, confirmButton = { TextButton(onClick = {
            submitting = true
            vm.createNode(bookId, title, folder, area, destination, label, source.takeIf { it.isNotBlank() }, onCreated)
        }, enabled = title.isNotBlank() && !submitting) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !submitting) { Text("Cancel") } })
}
