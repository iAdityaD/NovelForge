package com.novelforge.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.novelforge.app.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScreenHeading(title: String, subtitle: String, action: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        action()
    }
}

@Composable
fun LibraryScreen(
    books: List<Book>,
    entries: List<EntrySummary>,
    onBook: (String) -> Unit,
    onNew: () -> Unit,
    onImport: () -> Unit,
    onDemo: () -> Unit,
    onContinue: (Long) -> Unit,
    onSearch: () -> Unit,
) {
    var grid by rememberSaveable { mutableStateOf(true) }
    var filter by rememberSaveable { mutableStateOf("All") }
    val visible = books.filter { filter == "All" || it.status == filter }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                ScreenHeading("Library", "Your stories. Your worlds.") {
                    ActionIcon(Icons.Outlined.Search, "Search all workspaces", onSearch)
                }
            }
            item {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("All", "In progress", "Completed", "On hold").forEach { label ->
                        FilterChip(
                            selected = filter == label,
                            onClick = { filter = label },
                            label = { Text(label) },
                        )
                    }
                }
            }
            val recent = entries.filter { it.kind != "Folder" }.maxByOrNull { it.updated }
            if (recent != null)
                item {
                    SurfaceCard(Modifier.fillMaxWidth().clickable { onContinue(recent.id) }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Icon(
                                Icons.Outlined.History,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(Modifier.weight(1f)) {
                                Eyebrow("CONTINUE WRITING")
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    recent.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    books.find { it.id == recent.bookId }?.title?.replace('\n', ' ')
                                        ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                            Icon(
                                Icons.Outlined.ArrowOutward,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Workspaces",
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text("${visible.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ActionIcon(
                        if (grid) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                        "Toggle workspace layout",
                        { grid = !grid },
                    )
                }
            }
            if (visible.isEmpty())
                item {
                    EmptyState(
                        if (books.isEmpty()) "Build your first world" else "No workspaces here",
                        "A novel is a workspace. Keep its chapters, characters and lore together.",
                        Icons.Outlined.CreateNewFolder,
                        "New workspace",
                        onNew,
                    )
                }
            if (grid)
                items(visible.chunked(2)) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        row.forEach { book ->
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable {
                                    onBook(book.id)
                                }
                            ) {
                                WorkspaceCard(book, entries.filter { it.bookId == book.id }, false)
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            else
                items(visible, key = { it.id }) { book ->
                    SurfaceCard(Modifier.fillMaxWidth().clickable { onBook(book.id) }) {
                        WorkspaceCard(book, entries.filter { it.bookId == book.id }, true)
                    }
                }
            item {
                Row {
                    TextButton(onClick = onImport) {
                        Icon(Icons.Outlined.FileDownload, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Import backup")
                    }
                    if (books.isEmpty()) TextButton(onClick = onDemo) { Text("Open demo") }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNew,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            icon = { Icon(Icons.Outlined.Add, null) },
            text = { Text("New workspace") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun WorkspaceCard(book: Book, entries: List<EntrySummary>, compact: Boolean) {
    val words = entries.filter { it.kind == "Chapter" }.sumOf { it.words }
    if (compact)
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookCover(book, Modifier.size(56.dp, 72.dp), true)
            Column(Modifier.weight(1f)) {
                Text(
                    book.title.replace('\n', ' '),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                )
                Text(
                    "${book.genre} · ${number(words)} words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    book.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Icon(Icons.Outlined.ChevronRight, null)
        }
    else {
        BookCover(book, Modifier.fillMaxWidth().aspectRatio(.85f))
        Spacer(Modifier.height(12.dp))
        Text(
            book.title.replace('\n', ' '),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            book.genre,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { (words.toFloat() / book.target).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
        Spacer(Modifier.height(7.dp))
        Text(
            "${number(words)} / ${number(book.target)} words",
            style =
                MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Edited ${SimpleDateFormat("dd MMM",Locale.getDefault()).format(Date(book.updated))}",
            style =
                MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun NewWorkspaceScreen(
    onBack: () -> Unit,
    onCreate: (String, String, String, Int, Int, String, Set<String>) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var genre by rememberSaveable { mutableStateOf("Fantasy") }
    var selected by remember { mutableStateOf(workspaceTemplates.map { it.name }.toSet()) }
    var cover by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ActionIcon(Icons.Outlined.Close, "Cancel workspace creation", onBack)
            Text(
                "New workspace",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Text(
                    "A home for your next novel.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Start with a structure. Make it your own.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                OutlinedTextField(
                    title,
                    { title = it.take(200) },
                    label = { Text("Novel title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        author,
                        { author = it.take(120) },
                        label = { Text("Author") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        genre,
                        { genre = it.take(80) },
                        label = { Text("Genre") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
            }
            item {
                Text("Workspace color", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) { i ->
                        FilterChip(
                            selected = cover == i,
                            onClick = { cover = i },
                            label = { Text(listOf("Iris", "Ocean", "Ember", "Moss")[i]) },
                        )
                    }
                }
            }
            item {
                Text(
                    "Choose your starter folders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Everything is optional. Add files and nested folders whenever you need them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(workspaceTemplates) { t ->
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            selected =
                                if (t.name in selected) selected - t.name else selected + t.name
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = t.name in selected,
                        onCheckedChange = {
                            selected = if (it) selected + t.name else selected - t.name
                        },
                    )
                    Column(Modifier.weight(1f)) {
                        Text(t.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            t.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Button(
            onClick = {
                onCreate(title.trim(), author.trim(), genre.trim(), cover, 80000, "", selected)
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(20.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text("Create workspace")
        }
    }
}
