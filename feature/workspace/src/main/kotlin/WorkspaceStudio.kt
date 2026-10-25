package com.novelforge.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.*
import com.novelforge.presentation.WorkspaceViewModel

/** Plan and manuscript are roles; Write owns editing and manuscript arrangement. */
@Composable
fun WorkspaceStudio(
    vm: WorkspaceViewModel, book: Book, entries: List<EntrySummary>, tab: Int,
    onTab: (Int) -> Unit, onBack: () -> Unit, onOpen: (Long) -> Unit,
    onEdit: () -> Unit, onExport: (Boolean) -> Unit, onExplorer: () -> Unit, synopsisLimit: Int = 500,
    onNewDraft: () -> Unit = {}, onAttachLink: () -> Unit = {},
) {
    var menu by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf(false) }
    var editSynopsis by rememberSaveable(book.id) { mutableStateOf(false) }
    var startDocument by rememberSaveable(book.id) { mutableStateOf(false) }
    val manuscript = remember(entries) { orderedManuscript(entries) }
    val latest = manuscript.maxByOrNull { it.updated }
    val currentTab = tab.coerceIn(0, 2)
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            ActionIcon(Icons.AutoMirrored.Outlined.ArrowBack, "Back to library", onBack)
            Text(book.title.replace('\n', ' '), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            ActionIcon(Icons.Outlined.Edit, "Edit workspace details", onEdit)
            Box {
                ActionIcon(Icons.Outlined.MoreHoriz, "Workspace actions", { menu = true })
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text("Export manuscript") }, onClick = { menu = false; onExport(true) })
                    DropdownMenuItem(text = { Text("Back up workspace") }, onClick = { menu = false; onExport(false) })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Delete workspace", color = MaterialTheme.colorScheme.error) }, onClick = { menu = false; delete = true })
                }
            }
        }
        Row(Modifier.padding(start = 20.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(book.status, Modifier.weight(1f), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            TextButton(onClick = onExplorer) { Icon(Icons.Outlined.FolderOpen, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Files") }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (currentTab == 0) {
            WorkspaceOverview(vm, book, entries, onEdit, { editSynopsis = true }, { onTab(1) }, onOpen, Modifier.weight(1f))
        } else if (currentTab == 1) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.EditNote, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onNewDraft) { Icon(Icons.Outlined.NoteAdd, null); Spacer(Modifier.width(8.dp)); Text("New file") }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onAttachLink) { Icon(Icons.Outlined.Link, null); Spacer(Modifier.width(8.dp)); Text("Attach link") }
            }
        } else {
            Column(Modifier.weight(1f).fillMaxWidth().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                OwlIcon(Modifier.size(72.dp))
                Spacer(Modifier.height(24.dp))
                Text("Your writing companion", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                Text("Coming soon", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
            listOf("Overview" to Icons.Outlined.Home, "Write" to Icons.Outlined.EditNote, "AI" to Icons.Outlined.AutoAwesome).forEachIndexed { i, (label, icon) ->
                NavigationBarItem(selected = currentTab == i, onClick = { onTab(i) }, icon = { if (i == 2) OwlIcon() else Icon(icon, null) }, label = { Text(label, maxLines = 1) }, modifier = Modifier.testTag("workspace-tab-$i"))
            }
        }
    }
    if (startDocument) WorkspaceNodeDialog(vm, book.id, entries, MANUSCRIPT_AREA, null, false, { startDocument = false }) { startDocument = false; onOpen(it) }
    if (editSynopsis) SynopsisDialog(book, synopsisLimit, { editSynopsis = false }) { text -> vm.editBook(book.copy(synopsis = text)) { editSynopsis = false } }
    if (delete) AlertDialog(onDismissRequest = { delete = false }, title = { Text("Delete entire workspace?") },
        text = { Text("Delete “${book.title}” and all its documents, checkpoints, trash and writing history permanently? Export a backup first if you want to keep it.") },
        confirmButton = { TextButton(onClick = { delete = false; vm.deleteBook(book.id, onBack) }) { Text("Delete workspace", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { delete = false }) { Text("Keep workspace") } })
}
