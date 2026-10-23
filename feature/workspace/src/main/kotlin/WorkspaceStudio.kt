package com.novelforge.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelforge.core.model.*
import com.novelforge.domain.manuscript.*
import com.novelforge.presentation.WorkspaceViewModel

/** Workspace views share canonical documents; folder location never determines manuscript order. */
@Composable
fun WorkspaceStudio(
    vm: WorkspaceViewModel, book: Book, entries: List<EntrySummary>, tab: Int,
    onTab: (Int) -> Unit, onBack: () -> Unit, onOpen: (Long) -> Unit,
    onRead: (Long) -> Unit, onEdit: () -> Unit, onAdd: (Long?, String) -> Unit,
    onExport: (Boolean) -> Unit, onExplorer: () -> Unit,
) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val chapters = remember(entries, ui.placements) { manuscriptChapters(entries, ui.placements) }
    var menu by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf(false) }
    var category by rememberSaveable(book.id) { mutableStateOf("Character") }
    var query by rememberSaveable(book.id) { mutableStateOf("") }
    var graph by rememberSaveable(book.id) { mutableStateOf(false) }
    val categories = listOf("Character", "Location", "Theme", "Prop", "Worldbuilding", "Plot thread", "Timeline event", "Research", "Backstory", "Note")
    val latest = entries.filter { it.kind == "Chapter" }.maxByOrNull { it.updated }
    val overview = entries.firstOrNull { it.id == book.overviewId }
    LaunchedEffect(book.id, graph) { if (graph) vm.loadConnections(book.id) }
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
                    DropdownMenuItem(text = { Text("Delete workspace", color = MaterialTheme.colorScheme.error) }, onClick = { menu = false; delete = true })
                }
            }
        }
        Row(Modifier.padding(start = 20.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(book.status, Modifier.weight(1f), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            TextButton(onClick = onExplorer) { Icon(Icons.Outlined.FolderOpen, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Files") }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        LazyColumn(Modifier.weight(1f).fillMaxWidth().testTag("workspace-content"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (tab) {
                0 -> {
                    item {
                        Text("Your story, at a glance", style = MaterialTheme.typography.headlineMedium)
                        if (book.genre.isNotBlank()) Text(book.genre, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (book.synopsis.isNotBlank()) item { Text(book.synopsis, maxLines = 5, overflow = TextOverflow.Ellipsis) }
                    item {
                        FilledTonalButton(onClick = { if (latest != null) onOpen(latest.id) else onAdd(null, "Chapter") }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                            Icon(Icons.Outlined.EditNote, null); Spacer(Modifier.width(10.dp)); Text(if (latest == null) "Start writing" else "Continue writing")
                        }
                        latest?.let { Text(it.title, Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    item {
                        OutlinedButton(onClick = { if (overview != null) onRead(overview.id) else onAdd(null, "Novel Profile") }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (overview == null) "Create novel overview" else "Open novel overview")
                        }
                    }
                    item { Text("Recent documents", style = MaterialTheme.typography.titleMedium) }
                    items(entries.filter { it.kind != "Folder" }.sortedByDescending { it.updated }.take(5), key = { it.id }) { doc -> StudioDocument(doc, { onOpen(doc.id) }) }
                    item {
                        val words = chapters.sumOf { it.words }
                        Text("${"%,d".format(words)} manuscript words", style = MaterialTheme.typography.titleMedium)
                        if (book.target > 0) {
                            LinearProgressIndicator(progress = { (words.toFloat() / book.target).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
                            Text("Goal: ${"%,d".format(book.target)} words", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                1 -> {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Story bible", Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
                            ActionIcon(Icons.Outlined.Hub, "Show connections", { graph = !graph })
                            ActionIcon(Icons.Outlined.Add, "New story document", { onAdd(null, category) })
                        }
                    }
                    if (graph) {
                        item { InteractiveConnections(entries.filter { it.kind != "Folder" }, ui.connections, onOpen) }
                        item { Text("Use the editor’s link picker to connect story documents.", style = MaterialTheme.typography.bodySmall) }
                    } else {
                        item { ChoiceField("Collection", category, categories) { category = it } }
                        item { OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search story documents") }, singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }) }
                        val documents = entries.filter { it.kind == category && it.title.contains(query, true) }
                        if (documents.isEmpty()) item { EmptyState("Room for an idea", "Planning is optional. Add a reference when your story needs one.", action = "Create $category", onAction = { onAdd(null, category) }) }
                        items(documents, key = { it.id }) { doc -> StudioDocument(doc, { onOpen(doc.id) }) }
                    }
                }
                2 -> {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Drafts", Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
                            TextButton(onClick = { onAdd(null, "Chapter") }) { Icon(Icons.Outlined.Add, null); Text("Chapter") }
                        }
                        Text("Write now. Arrange your manuscript whenever you’re ready.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val drafts = entries.filter { it.kind in listOf("Chapter", "Screenplay") }.sortedByDescending { it.updated }
                    if (drafts.isEmpty()) item { EmptyState("Your first line starts here", "You don’t need an outline to begin.", action = "Start writing", onAction = { onAdd(null, "Chapter") }) }
                    items(drafts, key = { it.id }) { doc -> StudioDocument(doc, { onOpen(doc.id) }) }
                }
                3 -> {
                    item {
                        Text("Manuscript", style = MaterialTheme.typography.headlineMedium)
                        Text("Reading and export follow this order. Your folders stay independent.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (chapters.isEmpty()) item { Text("No chapters included yet. Create a chapter or include one below.") }
                    itemsIndexed(chapters, key = { _, e -> e.id }) { index, doc ->
                        Column {
                            StudioDocument(doc, { onOpen(doc.id) }, "${index + 1} · ${doc.words} words")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                ActionIcon(Icons.Outlined.ArrowUpward, "Move ${doc.title} earlier", { vm.moveChapter(doc.id, -1) }, index > 0)
                                ActionIcon(Icons.Outlined.ArrowDownward, "Move ${doc.title} later", { vm.moveChapter(doc.id, 1) }, index < chapters.lastIndex)
                                TextButton(onClick = { vm.includeChapter(doc.id, false) }) { Text("Unplace") }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                    val unplaced = entries.filter { it.kind == "Chapter" && chapters.none { c -> c.id == it.id } }
                    item { Text("Unplaced drafts · ${unplaced.size}", style = MaterialTheme.typography.titleMedium) }
                    items(unplaced, key = { it.id }) { doc ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { StudioDocument(doc, { onOpen(doc.id) }) }
                            TextButton(onClick = { vm.includeChapter(doc.id, true) }) { Text("Include") }
                        }
                    }
                    item { Text("Unplacing a chapter keeps its document and links. Find it in Write or Files.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    item { OutlinedButton(onClick = { onAdd(null, "Chapter") }, modifier = Modifier.fillMaxWidth()) { Text("Add chapter") } }
                }
                4 -> {
                    item { Text("Reading room", style = MaterialTheme.typography.headlineMedium); Text("Your manuscript, in reading order.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (chapters.isNotEmpty()) item { FilledTonalButton(onClick = { onRead(chapters.first().id) }, modifier = Modifier.fillMaxWidth()) { Text("Read from the beginning") } }
                    else item { EmptyState("A book waiting to happen", "Include chapters in Arrange to preview your manuscript.", action = "Arrange manuscript", onAction = { onTab(3) }) }
                    itemsIndexed(chapters, key = { _, e -> e.id }) { index, doc -> StudioDocument(doc, { onRead(doc.id) }, "Chapter ${index + 1} · ${doc.words} words") }
                }
            }
        }
        NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
            listOf("Overview" to Icons.Outlined.Home, "Plan" to Icons.Outlined.ListAlt, "Write" to Icons.Outlined.EditNote, "Arrange" to Icons.Outlined.AccountTree, "Read" to Icons.Outlined.AutoStories).forEachIndexed { i, (label, icon) ->
                NavigationBarItem(selected = tab == i, onClick = { onTab(i) }, icon = { Icon(icon, null) }, label = { Text(label, maxLines = 1) }, modifier = Modifier.testTag("workspace-tab-$i"))
            }
        }
    }
    if (delete) AlertDialog(onDismissRequest = { delete = false }, title = { Text("Delete entire workspace?") },
        text = { Text("Delete “${book.title}” and all its documents, checkpoints, trash and writing history permanently? Export a backup first if you want to keep it.") },
        confirmButton = { TextButton(onClick = { delete = false; vm.deleteBook(book.id, onBack) }) { Text("Delete workspace", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { delete = false }) { Text("Keep workspace") } })
}

@Composable
private fun StudioDocument(doc: EntrySummary, onOpen: () -> Unit, detail: String = "${doc.kind} · ${doc.words} words") {
    Row(Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (doc.kind == "Character") Icons.Outlined.PersonOutline else Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(doc.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
