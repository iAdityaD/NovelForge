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
    val locations by vm.locations.collectAsState()
    val selected = locations[book.id]
    var folder by remember { mutableStateOf<Boolean?>(null) }
    var locationMenu by remember { mutableStateOf(false) }
    val destination = selected?.takeIf { id -> entries.any { it.id == id && it.kind == "Folder" } }
    val current = entries.find { it.id == destination }
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Write", Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
            ActionIcon(Icons.Outlined.CreateNewFolder, "Add folder", { folder = true })
            ActionIcon(Icons.Outlined.NoteAdd, "Add file", { folder = false })
        }
        if (destination != null) TextButton(onClick = { vm.selectLocation(book.id, current?.folderId) }) {
            Icon(Icons.Outlined.ArrowUpward, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("Up one folder")
        }
        Box(Modifier.weight(1f)) {
            key(book.id, destination) {
                ExplorerTree(vm, book, entries, onOpen, { vm.selectLocation(book.id, it) }, destination, Modifier.fillMaxSize(), rootLabel = current?.title ?: "Entire workspace", arrange = true, rootId = destination, onRootClick = { locationMenu = true })
            }
            DropdownMenu(locationMenu, { locationMenu = false }, modifier = Modifier.heightIn(max = 360.dp)) {
                DropdownMenuItem(text = { Text("Entire workspace") }, onClick = { vm.selectLocation(book.id, null); locationMenu = false })
                entries.filter { it.kind == "Folder" }.forEach { f -> DropdownMenuItem(text = { Text(resourceFolderPath(f, entries.filter { it.kind == "Folder" })) }, onClick = { vm.selectLocation(book.id, f.id); locationMenu = false }) }
            }
        }
    }
    folder?.let { isFolder -> WorkspaceNodeDialog(vm, book.id, entries, current?.area ?: MANUSCRIPT_AREA, destination, isFolder, { folder = null }) { id -> folder = null; if (!isFolder) onOpen(id) } }

}

@Composable
fun WorkspaceNodeDialog(vm: WorkspaceViewModel, bookId: String, entries: List<EntrySummary>, area: String, parent: Long?, folder: Boolean, onDismiss: () -> Unit, onCreated: (Long) -> Unit) {
    var documentArea by rememberSaveable { mutableStateOf(area) }
    var title by rememberSaveable { mutableStateOf("") }
    var label by rememberSaveable { mutableStateOf("") }
    var source by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf(parent) }
    var submitting by remember { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf("") }
    var newCategory by rememberSaveable { mutableStateOf(false) }
    val folders = entries.filter { it.kind == "Folder" }
    // Errors are surfaced by the feature host; allow retry after the operation ends.
    val error by vm.error.collectAsState()
    LaunchedEffect(error) { if (error != null) submitting = false }
    AlertDialog(onDismissRequest = { if (!submitting) onDismiss() }, title = { Text(if (folder) "New folder" else "New file") },
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
                ChoiceField("Use for", if (documentArea == MANUSCRIPT_AREA) "Manuscript" else "Reference", listOf("Manuscript", "Reference")) { documentArea = if (it == "Manuscript") MANUSCRIPT_AREA else PLAN_AREA }
                if (!folder) {
                    OutlinedTextField(label, { label = it.take(80) }, label = { Text("Type label (optional)") }, singleLine = true)
                    if (documentArea == PLAN_AREA) {
                        TextButton(onClick = { newCategory = !newCategory }) { Text(if (newCategory) "Use existing location" else "+ New category") }
                        if (newCategory) OutlinedTextField(category, { category = it.take(200) }, label = { Text("Category name") }, singleLine = true)
                        OutlinedTextField(source, { source = it }, label = { Text("Source URL (optional)") }, singleLine = true)
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = {
            submitting = true
            vm.createNode(bookId, title, folder, documentArea, destination, label, source.takeIf { it.isNotBlank() }, if (newCategory && documentArea == PLAN_AREA) category else null, onCreated)
        }, enabled = title.isNotBlank() && (!newCategory || category.isNotBlank()) && !submitting) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !submitting) { Text("Cancel") } })
}
