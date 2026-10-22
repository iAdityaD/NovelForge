package com.novelforge.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelforge.core.model.*
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*
import com.novelforge.presentation.WorkspaceViewModel

@Composable
fun WorkspaceScreen(
    vm: WorkspaceViewModel,
    book: Book,
    entries: List<EntrySummary>,
    onBack: () -> Unit,
    onOpen: (Long) -> Unit,
    onEdit: () -> Unit,
    onAdd: (Long?, String) -> Unit,
    onExport: (Boolean) -> Unit,
) {
    val ui by vm.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable(book.id) { mutableIntStateOf(0) }
    var selectedFolder by rememberSaveable(book.id) { mutableStateOf<Long?>(null) }
    var folderDialog by remember { mutableStateOf(false) }
    var more by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Delete entire workspace?") },
        text = { Text("Are you sure you want to delete “${book.title}”? This permanently deletes all its folders, documents, checkpoints, trash and writing history. Export a backup first if you want to keep the novel.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; vm.deleteBook(book.id, onBack) }) { Text("Delete workspace", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Keep workspace") } },
    )
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionIcon(Icons.AutoMirrored.Outlined.ArrowBack, "Back to library", onBack)
            Column(Modifier.weight(1f)) {
                Text(
                    book.title.replace('\n', ' '),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "WORKSPACE · ${book.genre}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ActionIcon(Icons.Outlined.Edit, "Edit workspace details", onEdit)
            Box {
                ActionIcon(Icons.Outlined.MoreHoriz, "Workspace actions", { more = true })
                DropdownMenu(more, { more = false }) {
                    DropdownMenuItem(
                        text = { Text("Export manuscript") },
                        onClick = {
                            more = false
                            onExport(true)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Back up workspace") },
                        onClick = {
                            more = false
                            onExport(false)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete workspace", color = MaterialTheme.colorScheme.error) },
                        onClick = { more = false; confirmDelete = true },
                    )
                }
            }
        }
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = tab == 0,
                onClick = { tab = 0 },
                label = { Text("Files") },
                leadingIcon = { Icon(Icons.Outlined.FolderOpen, null, Modifier.size(16.dp)) },
            )
            FilterChip(
                selected = tab == 1,
                onClick = {
                    tab = 1
                    vm.loadConnections(book.id)
                },
                label = { Text("Graph") },
                leadingIcon = { Icon(Icons.Outlined.Hub, null, Modifier.size(16.dp)) },
            )
            Spacer(Modifier.weight(1f))
            Text(
                book.status,
                Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (tab == 0) {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "EXPLORER",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ActionIcon(
                    Icons.Outlined.NoteAdd,
                    "New file",
                    {
                        onAdd(
                            selectedFolder,
                            defaultKindForFolder(entries.find { it.id == selectedFolder }?.title),
                        )
                    },
                )
                ActionIcon(Icons.Outlined.CreateNewFolder, "New folder", { folderDialog = true })
            }
            ExplorerTree(
                vm,
                book,
                entries,
                onOpen,
                { selectedFolder = it },
                selectedFolder,
                Modifier.weight(1f),
            )
            Text(
                "Select a folder for new items · tap its arrow to expand",
                Modifier.fillMaxWidth().padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text("Everything is connected.", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Tap a node to open its file. Pinch to zoom, drag to pan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item {
                    InteractiveConnections(
                        entries.filter { it.kind != "Folder" },
                        ui.connections,
                        onOpen,
                    )
                }
                if (ui.connections.isEmpty())
                    item {
                        EmptyState(
                            "Start with one link",
                            "Use the link button in your editor to connect a chapter to a character or a world-building note.",
                            Icons.Outlined.Link,
                        )
                    }
                items(ui.connections) { (a, b) ->
                    SurfaceCard(Modifier.fillMaxWidth()) {
                        TextButton(onClick = { onOpen(a) }) {
                            Text(entries.find { it.id == a }?.title ?: "File")
                        }
                        Text("→ references", style = MaterialTheme.typography.labelSmall)
                        TextButton(onClick = { onOpen(b) }) {
                            Text(entries.find { it.id == b }?.title ?: "File")
                        }
                    }
                }
            }
        }
    }
    if (folderDialog)
        NameDialog("New folder", "Folder name", onDismiss = { folderDialog = false }) { name ->
            vm.createFolder(book.id, name, selectedFolder)
            folderDialog = false
        }
}

fun defaultKindForFolder(name: String?) =
    when {
        name?.contains("Characters", true) == true -> "Character"
        (name?.contains("Lore", true) == true || name?.contains("World", true) == true) -> "Worldbuilding"
        name?.contains("Plot", true) == true -> "Plot thread"
        (name?.contains("Chapters", true) == true || name?.contains("Manuscript", true) == true) -> "Chapter"
        else -> "Note"
    }

@Composable
fun NameDialog(
    title: String,
    label: String,
    initial: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                name,
                { name = it.take(200) },
                label = { Text(label) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun ExplorerTree(
    vm: WorkspaceViewModel,
    book: Book,
    entries: List<EntrySummary>,
    onOpen: (Long) -> Unit,
    onSelect: (Long?) -> Unit,
    selected: Long?,
    modifier: Modifier = Modifier,
    activeFileId: Long? = null,
) {
    var query by rememberSaveable(book.id) { mutableStateOf("") }
    val listState = rememberLazyListState()
    val expanded = remember(book.id) { mutableStateMapOf<Long, Boolean>() }
    val bounds = remember { mutableMapOf<Long, Rect>() }
    var dragging by remember { mutableStateOf<Long?>(null) }
    var point by remember { mutableStateOf(Offset.Zero) }
    var action by remember { mutableStateOf<Pair<String, EntrySummary>?>(null) }
    LaunchedEffect(activeFileId, entries) {
        var parent = entries.find { it.id == activeFileId }?.folderId
        val seen = mutableSetOf<Long>()
        while (parent != null && seen.add(parent)) {
            expanded[parent] = true
            parent = entries.find { it.id == parent }?.folderId
        }
    }
    val visibleIds =
        remember(query, entries) {
            if (query.isBlank()) null
            else
                buildSet<Long> {
                    val byId = entries.associateBy { it.id }
                    entries
                        .filter { it.title.contains(query, true) }
                        .forEach { entry ->
                            add(entry.id)
                            var parent = entry.folderId
                            val seen = mutableSetOf<Long>()
                            while (parent != null && seen.add(parent)) {
                                add(parent)
                                parent = byId[parent]?.folderId
                            }
                        }
                }
        }
    val rows =
        remember(entries, expanded.toMap(), visibleIds) {
            val list = mutableListOf<Pair<EntrySummary, Int>>()
            val seen = mutableSetOf<Long>()
            fun add(parent: Long?, depth: Int) {
                entries
                    .filter { it.folderId == parent && (visibleIds == null || it.id in visibleIds) }
                    .sortedWith(
                        compareBy<EntrySummary> { it.kind != "Folder" }
                            .thenBy { it.position }
                            .thenBy { it.title }
                    )
                    .forEach { e ->
                        if (seen.add(e.id)) {
                            list.add(e to depth)
                            if (
                                e.kind == "Folder" && (expanded[e.id] == true || visibleIds != null)
                            )
                                add(e.id, depth + 1)
                        }
                    }
            }
            add(null, 0)
            list
        }
    LaunchedEffect(activeFileId, rows.map { it.first.id }) {
        if (activeFileId != null && query.isBlank()) {
            val index = rows.indexOfFirst { it.first.id == activeFileId }
            if (index >= 0) listState.scrollToItem(index + 1)
        }
    }
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                query,
                { query = it },
                placeholder = { Text("Find a file") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Outlined.Search, null, Modifier.size(18.dp)) },
                trailingIcon = {
                    if (query.isNotEmpty())
                        ActionIcon(Icons.Outlined.Close, "Clear explorer search", { query = "" })
                },
            )
            ActionIcon(
                Icons.Outlined.UnfoldMore,
                "Expand all folders",
                { entries.filter { it.kind == "Folder" }.forEach { expanded[it.id] = true } },
            )
            ActionIcon(Icons.Outlined.UnfoldLess, "Collapse all folders", { expanded.clear() })
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().testTag("explorer-tree"),
            state = listState,
            contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 16.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth()
                        .onGloballyPositioned { bounds[0] = it.boundsInRoot() }
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (selected == null)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = .4f)
                            else Color.Transparent
                        )
                        .clickable { onSelect(null) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.FolderSpecial,
                        null,
                        Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Workspace root",
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (entries.isEmpty())
                item {
                    EmptyState(
                        "An open canvas",
                        "Add your first file or folder using the buttons above.",
                        Icons.Outlined.FolderOpen,
                    )
                }
            if (query.isNotBlank() && rows.isEmpty())
                item { Text("No matching files", Modifier.padding(16.dp)) }
            items(rows, key = { it.first.id }) { (e, depth) ->
                DisposableEffect(e.id) { onDispose { bounds.remove(e.id) } }
                var menu by remember { mutableStateOf(false) }
                val folder = e.kind == "Folder"
                val hover =
                    dragging != null &&
                        dragging != e.id &&
                        folder &&
                        bounds[e.id]?.contains(point) == true
                Row(
                    Modifier.fillMaxWidth()
                        .onGloballyPositioned { bounds[e.id] = it.boundsInRoot() }
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (hover || activeFileId == e.id)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f)
                            else if (selected == e.id)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f)
                            else Color.Transparent
                        )
                        .pointerInput(e.id, entries) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { local ->
                                    dragging = e.id
                                    point = (bounds[e.id]?.topLeft ?: Offset.Zero) + local
                                },
                                onDrag = { change, delta ->
                                    change.consume()
                                    point += delta
                                },
                                onDragCancel = { dragging = null },
                                onDragEnd = {
                                    val target =
                                        bounds.entries
                                            .firstOrNull { (id, rect) ->
                                                id != e.id &&
                                                    rect.contains(point) &&
                                                    (id == 0L ||
                                                        entries.any {
                                                            it.id == id && it.kind == "Folder"
                                                        })
                                            }
                                            ?.key
                                    if (target != null)
                                        vm.relocate(e.id, target.takeIf { it != 0L })
                                    dragging = null
                                },
                            )
                        }
                        .clickable {
                            if (folder) {
                                onSelect(e.id)
                            } else onOpen(e.id)
                        }
                        .padding(
                            start = (8 + depth.coerceAtMost(6) * 16).dp,
                            end = 0.dp,
                            top = 0.dp,
                            bottom = 0.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (folder)
                        IconButton(
                            onClick = { expanded[e.id] = expanded[e.id] != true },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                if (expanded[e.id] == true || visibleIds != null)
                                    Icons.Outlined.ExpandMore
                                else Icons.Outlined.ChevronRight,
                                if (expanded[e.id] == true) "Collapse ${e.title}"
                                else "Expand ${e.title}",
                                Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    else Spacer(Modifier.width(48.dp))
                    Icon(
                        if (folder) Icons.Outlined.Folder else kindIcon(e.kind),
                        null,
                        Modifier.size(19.dp),
                        tint =
                            if (folder) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            e.title + (if (folder) "" else ".md"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (folder) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!folder)
                            Text(
                                "${e.kind} · ${number(e.words)} words",
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing =
                                            androidx.compose.ui.unit.TextUnit.Unspecified
                                    ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                    }
                    Box {
                        ActionIcon(
                            Icons.Outlined.MoreHoriz,
                            "Actions for ${e.title}",
                            { menu = true },
                        )
                        DropdownMenu(menu, { menu = false }) {
                            val siblings =
                                entries
                                    .filter {
                                        it.folderId == e.folderId && (it.kind == "Folder") == folder
                                    }
                                    .sortedWith(
                                        compareBy<EntrySummary> { it.position }.thenBy { it.title }
                                    )
                            DropdownMenuItem(
                                text = { Text("Move up") },
                                enabled = siblings.firstOrNull()?.id != e.id,
                                onClick = {
                                    vm.moveEntry(e.id, -1)
                                    menu = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Move down") },
                                enabled = siblings.lastOrNull()?.id != e.id,
                                onClick = {
                                    vm.moveEntry(e.id, 1)
                                    menu = false
                                },
                            )
                            listOf("Rename", "Move", "Trash").forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        menu = false
                                        action = name to e
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    action?.let { (name, e) ->
        when (name) {
            "Rename" ->
                NameDialog(
                    "Rename ${if(e.kind=="Folder")"folder" else "file"}",
                    "Name",
                    e.title,
                    { action = null },
                ) {
                    vm.renameEntry(e.id, it)
                    action = null
                }
            "Trash" ->
                AlertDialog(
                    onDismissRequest = { action = null },
                    title = { Text("Move to trash?") },
                    text = {
                        Text(
                            if (e.kind == "Folder")
                                "This folder and all files inside it will move to Recently deleted. You can restore them together."
                            else "You can restore this file in Settings → Recently deleted."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.deleteEntry(e.id) {
                                    action = null
                                    onSelect(null)
                                }
                            }
                        ) {
                            Text("Move to trash")
                        }
                    },
                    dismissButton = { TextButton(onClick = { action = null }) { Text("Cancel") } },
                )
            "Move" ->
                AlertDialog(
                    onDismissRequest = { action = null },
                    title = { Text("Move to folder") },
                    text = {
                        LazyColumn(Modifier.heightIn(max = 400.dp)) {
                            item {
                                TextButton(
                                    onClick = {
                                        vm.relocate(e.id, null)
                                        action = null
                                    }
                                ) {
                                    Text("Workspace root")
                                }
                            }
                            items(
                                entries.filter { it.kind == "Folder" && it.id != e.id },
                                key = { it.id },
                            ) { f ->
                                TextButton(
                                    onClick = {
                                        vm.relocate(e.id, f.id)
                                        action = null
                                    }
                                ) {
                                    Text(f.title)
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = { action = null }) { Text("Cancel") } },
                )
        }
    }
}
