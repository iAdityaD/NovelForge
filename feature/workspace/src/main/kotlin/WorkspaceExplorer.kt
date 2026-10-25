package com.novelforge.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import kotlinx.coroutines.delay
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.alpha
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

fun defaultKindForFolder(name: String?) =
    when {
        name?.contains("Characters", true) == true -> "Character"
        (name?.contains("Lore", true) == true || name?.equals("Story World", true) == true) -> "Worldbuilding"
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
    rootLabel: String = "Workspace root",
    arrange: Boolean = false,
    rootId: Long? = null,
    onRootClick: (() -> Unit)? = null,
) {
    var query by rememberSaveable(book.id) { mutableStateOf("") }
    val listState = rememberLazyListState()
    val expanded = remember(book.id) { mutableStateMapOf<Long, Boolean>() }
    val bounds = remember { mutableMapOf<Long, Rect>() }
    var dragging by remember { mutableStateOf<Long?>(null) }
    var point by remember { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(Rect.Zero) }
    var filters by remember { mutableStateOf(false) }
    var viewMenu by remember { mutableStateOf(false) }
    var typeFilter by rememberSaveable(book.id, rootLabel) { mutableStateOf<String?>(null) }
    var statusFilter by rememberSaveable(book.id, rootLabel) { mutableStateOf<String?>(null) }
    val types = entries.filter { it.kind != "Folder" }.map { it.kind }.distinct().sorted()
    LaunchedEffect(dragging, point, viewport) {
        if (dragging != null && arrange) {
            val direction = when {
                point.y < viewport.top + 80 -> -1
                point.y > viewport.bottom - 80 -> 1
                else -> 0
            }
            while (direction != 0) { listState.scrollBy(direction * 18f); delay(16) }
        }
    }
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
        remember(query, entries, typeFilter, statusFilter) {
            if (query.isBlank() && typeFilter == null && statusFilter == null) null
            else
                buildSet<Long> {
                    val byId = entries.associateBy { it.id }
                    entries
                        .filter { it.title.contains(query, true) && (typeFilter == null || it.kind == typeFilter) && (statusFilter == null || it.status == statusFilter) }
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
        remember(entries, expanded.toMap(), visibleIds, rootId) {
            val list = mutableListOf<Pair<EntrySummary, Int>>()
            val seen = mutableSetOf<Long>()
            fun add(parent: Long?, depth: Int) {
                entries
                    .filter { it.folderId == parent && (visibleIds == null || it.id in visibleIds) }
                    .sortedWith(
                        compareBy<EntrySummary> { it.position }.thenBy { it.id }
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
            add(rootId, 0)
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
            ActionIcon(Icons.Outlined.FilterList, "Filter files", { filters = true })
            Box {
                ActionIcon(Icons.Outlined.MoreVert, "Outline options", { viewMenu = true })
                DropdownMenu(viewMenu, { viewMenu = false }) {
                    DropdownMenuItem(text = { Text("Expand all folders") }, onClick = { entries.filter { it.kind == "Folder" }.forEach { expanded[it.id] = true }; viewMenu = false })
                    DropdownMenuItem(text = { Text("Collapse all folders") }, onClick = { expanded.clear(); viewMenu = false })
                    DropdownMenuItem(text = { Text("Clear search and filters") }, onClick = { query = ""; typeFilter = null; statusFilter = null; viewMenu = false })
                }
            }
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().onGloballyPositioned { viewport = it.boundsInRoot() }.testTag("explorer-tree").scrollIndicator(listState),
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
                        .clickable { onRootClick?.invoke() ?: onSelect(null) }
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
                        rootLabel,
                        modifier = Modifier.weight(1f),
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (onRootClick != null) Icon(Icons.Outlined.ExpandMore, "Choose location")
                }
            }
            if (entries.none { it.folderId == rootId })
                item {
                    EmptyState(
                        "An open canvas",
                        "Add your first file or folder using the buttons above.",
                        Icons.Outlined.FolderOpen,
                    )
                }
            if (visibleIds != null && rows.isEmpty())
                item { Text("No matching files", Modifier.padding(16.dp)) }
            items(rows, key = { it.first.id }) { (e, depth) ->
                DisposableEffect(e.id) { onDispose { bounds.remove(e.id) } }
                var menu by remember { mutableStateOf(false) }
                val folder = e.kind == "Folder"
                val hover =
                    dragging != null &&
                        dragging != e.id &&
                        (folder || arrange) &&
                        bounds[e.id]?.contains(point) == true
                val dropColor = MaterialTheme.colorScheme.primary
                Row(
                    Modifier.fillMaxWidth()
                        .alpha(if (arrange && dragging == e.id) .45f else 1f)
                        .drawWithContent {
                            drawContent()
                            if (hover && arrange) {
                                val inside = folder && bounds[e.id]?.let { point.y in (it.top + it.height * .3f)..(it.bottom - it.height * .3f) } == true
                                val y = if (point.y > (bounds[e.id]?.center?.y ?: point.y)) size.height - 1.dp.toPx() else 1.dp.toPx()
                                if (!inside) drawLine(dropColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 2.dp.toPx())
                            }
                        }
                        .onGloballyPositioned { bounds[e.id] = it.boundsInRoot() }
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (hover || activeFileId == e.id)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f)
                            else if (selected == e.id)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f)
                            else Color.Transparent
                        )
                        .border(if (hover && arrange) 1.dp else 0.dp, if (hover && arrange) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(4.dp))
                        .pointerInput(e.id, entries, arrange) {
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
                                                            it.id == id && (arrange || it.kind == "Folder")
                                                        })
                                            }
                                            ?.key
                                    if (target != null) {
                                        val targetNode = entries.find { it.id == target }
                                        val rect = bounds[target]
                                        val inside = targetNode?.kind == "Folder" && rect != null && point.y in (rect.top + rect.height * .3f)..(rect.bottom - rect.height * .3f)
                                        if (target == 0L) vm.relocate(e.id, rootId)
                                        else if (inside || !arrange) { vm.relocate(e.id, target); expanded[target] = true }
                                        else vm.placeBefore(e.id, target, point.y > (rect?.center?.y ?: point.y))
                                    }
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
                    if (arrange) Icon(Icons.Outlined.DragHandle, "Drag ${e.title} to reorder", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    else Spacer(Modifier.width(if (arrange) 12.dp else 24.dp))
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
                    if (hover && arrange) {
                        val rect = bounds[e.id]
                        val inside = folder && rect != null && point.y in (rect.top + rect.height * .3f)..(rect.bottom - rect.height * .3f)
                        Text(if (inside) "Inside" else if (point.y < (rect?.center?.y ?: point.y)) "Before" else "After", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
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
                                        it.folderId == e.folderId
                                    }
                                    .sortedWith(
                                        compareBy<EntrySummary> { it.position }.thenBy { it.id }
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
                            DropdownMenuItem(text = { Text(if (e.area == MANUSCRIPT_AREA) "Use as reference" else "Include in manuscript") }, onClick = { menu = false; action = "Role" to e })
                            listOf("Rename", "Duplicate", "Move", "Trash").forEach { name ->
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
    if (filters) AlertDialog(onDismissRequest = { filters = false }, title = { Text("Filter files") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OptionalFileFilter("Type", typeFilter, types) { typeFilter = it }
            OptionalFileFilter("Status", statusFilter, entries.filter { it.kind != "Folder" }.map { it.status }.distinct()) { statusFilter = it }
            TextButton(onClick = { typeFilter = null; statusFilter = null }) { Text("Reset filters") }
        }
    }, confirmButton = { TextButton(onClick = { filters = false }) { Text("Done") } })
    action?.let { (name, e) ->
        when (name) {
            "Duplicate" -> LaunchedEffect(e.id) { vm.duplicate(e.id); action = null }
            "Role" -> AlertDialog(onDismissRequest = { action = null }, title = { Text(if (e.area == MANUSCRIPT_AREA) "Use as reference?" else "Include in manuscript?") }, text = { Text(if (e.kind == "Folder") "Applies to this folder and everything inside it. Manuscript items are included in export and writing totals." else "Manuscript files are included in export and writing totals. References remain available in the workspace.") }, confirmButton = { TextButton(onClick = { vm.setArea(e.id, if (e.area == MANUSCRIPT_AREA) PLAN_AREA else MANUSCRIPT_AREA); action = null }) { Text("Apply") } }, dismissButton = { TextButton(onClick = { action = null }) { Text("Cancel") } })
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
                                entries.filter { candidate ->
                                    if (candidate.kind != "Folder") false else {
                                        var ancestor: Long? = candidate.id
                                        val seen = mutableSetOf<Long>()
                                        while (ancestor != null && ancestor != e.id && seen.add(ancestor)) ancestor = entries.find { it.id == ancestor }?.folderId
                                        ancestor != e.id
                                    }
                                },
                                key = { it.id },
                            ) { f ->
                                TextButton(
                                    onClick = {
                                        vm.relocate(e.id, f.id)
                                        action = null
                                    }
                                ) {
                                    Text(resourceFolderPath(f, entries.filter { it.kind == "Folder" }))
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = { action = null }) { Text("Cancel") } },
                )
        }
    }
}

@Composable
private fun OptionalFileFilter(label: String, selected: String?, options: List<String>, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selected ?: "Any", Modifier.weight(1f)); Icon(Icons.Outlined.ExpandMore, null)
            }
            DropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
                DropdownMenuItem(text = { Text("Any") }, onClick = { onSelect(null); expanded = false })
                options.forEach { value -> DropdownMenuItem(text = { Text(value) }, onClick = { onSelect(value); expanded = false }) }
            }
        }
    }
}
