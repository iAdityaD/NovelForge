package com.novelforge.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.novelforge.presentation.WorkspaceViewModel
import com.novelforge.core.model.*
import com.novelforge.domain.workspace.*
import com.novelforge.domain.story.*
import com.novelforge.domain.analytics.*

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
            Box {
                ActionIcon(Icons.Outlined.MoreHoriz, "Workspace actions", { more = true })
                DropdownMenu(more, { more = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit workspace details") },
                        onClick = {
                            more = false
                            onEdit()
                        },
                    )
                    bookStatuses.forEach { status ->
                        DropdownMenuItem(
                            text = { Text("Mark $status") },
                            onClick = {
                                vm.setBookStatus(book, status)
                                more = false
                            },
                        )
                    }
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
                "Hold & drag to a folder · ⋯ for more actions",
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
        name?.contains("Lore", true) == true -> "Worldbuilding"
        name?.contains("Plot", true) == true -> "Plot thread"
        name?.contains("Chapters", true) == true -> "Chapter"
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
) {
    val expanded = remember(book.id) { mutableStateMapOf<Long, Boolean>() }
    val bounds = remember { mutableMapOf<Long, Rect>() }
    var dragging by remember { mutableStateOf<Long?>(null) }
    var point by remember { mutableStateOf(Offset.Zero) }
    var action by remember { mutableStateOf<Pair<String, EntrySummary>?>(null) }
    val rows =
        remember(entries, expanded.toMap()) {
            val list = mutableListOf<Pair<EntrySummary, Int>>()
            val seen = mutableSetOf<Long>()
            fun add(parent: Long?, depth: Int) {
                entries
                    .filter { it.folderId == parent }
                    .sortedWith(
                        compareBy<EntrySummary> { it.kind != "Folder" }
                            .thenBy { it.position }
                            .thenBy { it.title }
                    )
                    .forEach { e ->
                        if (seen.add(e.id)) {
                            list.add(e to depth)
                            if (e.kind == "Folder" && expanded[e.id] == true) add(e.id, depth + 1)
                        }
                    }
            }
            add(null, 0)
            list
        }
    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp, 6.dp, 12.dp, 20.dp)) {
        item {
            Row(
                Modifier.fillMaxWidth()
                    .onGloballyPositioned { bounds[0] = it.boundsInRoot() }
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected == null)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = .4f)
                        else MaterialTheme.colorScheme.background
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
                    "/ ${book.title.replace('\n',' ')}",
                    fontFamily = FontFamily.Monospace,
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (hover || selected == e.id) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.background
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
                                if (target != null) vm.relocate(e.id, target.takeIf { it != 0L })
                                dragging = null
                            },
                        )
                    }
                    .clickable {
                        if (folder) {
                            expanded[e.id] = expanded[e.id] != true
                            onSelect(e.id)
                        } else onOpen(e.id)
                    }
                    .padding(
                        start = (8 + depth.coerceAtMost(6) * 16).dp,
                        end = 0.dp,
                        top = 4.dp,
                        bottom = 4.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (folder)
                    Icon(
                        if (expanded[e.id] == true) Icons.Outlined.ExpandMore
                        else Icons.Outlined.ChevronRight,
                        null,
                        Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                else Spacer(Modifier.width(18.dp))
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
                                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                                ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                }
                Box {
                    ActionIcon(Icons.Outlined.MoreHoriz, "Actions for ${e.title}", { menu = true })
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
