package com.novelforge.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.*
import com.novelforge.domain.workspace.*
import java.text.SimpleDateFormat
import java.util.*

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
    excludedChapters: Set<Long> = emptySet(),
) {
    var grid by rememberSaveable { mutableStateOf(true) }
    var filter by rememberSaveable { mutableStateOf("All") }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var genreFilter by rememberSaveable { mutableStateOf<String?>(null) }
    val availableGenres = books.flatMap { it.genres }.distinct().sorted()
    val visible =
        books.filter {
            (filter == "All" || it.status == filter) &&
                (genreFilter == null || genreFilter in it.genres)
        }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                ScreenHeading("Library", "Your stories. Your worlds.") {
                    ActionIcon(Icons.Outlined.FileDownload, "Import backup", onImport)
                    ActionIcon(Icons.Outlined.Search, "Search all workspaces", onSearch)
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
                    TextButton(onClick = { showFilters = true }) {
                        Icon(Icons.Outlined.FilterList, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        val count = (if (filter != "All") 1 else 0) + (if (genreFilter != null) 1 else 0)
                        Text(if (count == 0) "Filter" else "Filter · $count")
                    }
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
                                Modifier.weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onBook(book.id) }
                                    .padding(bottom = 12.dp)
                            ) {
                                WorkspaceCard(book, entries.filter { it.bookId == book.id && it.id !in excludedChapters }, false)
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            else
                items(visible, key = { it.id }) { book ->
                    SurfaceCard(Modifier.fillMaxWidth().clickable { onBook(book.id) }) {
                        WorkspaceCard(book, entries.filter { it.bookId == book.id && it.id !in excludedChapters }, true)
                    }
                }
            if (books.isEmpty()) item { TextButton(onClick = onDemo) { Text("Open demo") } }
        }
        ExtendedFloatingActionButton(
            onClick = onNew,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).testTag("new-workspace"),
            icon = { Icon(Icons.Outlined.Add, null) },
            text = { Text("New workspace") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    }
    if (showFilters) {
        var status by remember { mutableStateOf(filter) }
        var genre by remember { mutableStateOf(genreFilter) }
        AlertDialog(
            onDismissRequest = { showFilters = false },
            title = { Text("Filter library") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    ChoiceField("Status", if (status == "All") "All statuses" else status, listOf("All statuses") + bookStatuses) {
                        status = if (it == "All statuses") "All" else it
                    }
                    ChoiceField("Genre", genre ?: "All genres", listOf("All genres") + availableGenres) {
                        genre = if (it == "All genres") null else it
                    }
                    Text("Status and genre filters are combined.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { status = "All"; genre = null }) { Text("Reset filters") }
                }
            },
            confirmButton = { TextButton(onClick = { filter = status; genreFilter = genre; showFilters = false }) { Text("Apply filters") } },
            dismissButton = { TextButton(onClick = { showFilters = false }) { Text("Cancel") } },
        )
    }

}

@Composable
private fun WorkspaceCard(book: Book, entries: List<EntrySummary>, compact: Boolean) {
    val words = entries.filter { it.area == MANUSCRIPT_AREA && it.kind != "Folder" }.sumOf { it.words }
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
        if (book.target > 0) LinearProgressIndicator(
            progress = { (words.toFloat() / book.target).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
        Spacer(Modifier.height(7.dp))
        Text(
            if (book.target > 0) "${number(words)} / ${number(book.target)} words" else "${number(words)} words",
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
    starterFolders: List<StarterFolder>,
    onBack: () -> Unit,
    onCreate: (String, String, List<String>, Int, Int, Boolean, String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var genres by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var standard by rememberSaveable { mutableStateOf(true) }
    var templateId by rememberSaveable { mutableStateOf("novel") }
    var details by rememberSaveable { mutableStateOf(false) }
    val recipe = projectRecipe(templateId, starterFolders)
    var cover by rememberSaveable { mutableIntStateOf(0) }
    var preview by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ActionIcon(Icons.Outlined.Close, "Cancel workspace creation", onBack)
            Text("New workspace", style = MaterialTheme.typography.titleLarge)
        }
        LazyColumn(
            Modifier.weight(1f).testTag("workspace-form"),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item { OutlinedTextField(title, { title = it.take(200) }, label = { Text("Novel title") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            item {
                Text("Choose your starting point", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                StructureChoice("Standard structure", "Start with a writing template.", standard) { standard = true }
                Spacer(Modifier.height(10.dp))
                StructureChoice("Build from scratch", "No preset folders or documents.", !standard) { standard = false }
            }
            if (standard) item {
                ChoiceField("Template", recipe.name, projectRecipes.map { it.name }) { selected ->
                    templateId = projectRecipes.first { it.name == selected }.id
                }
                TextButton(onClick = { preview = !preview }) {
                    Text(if (preview) "Hide starter folders" else "Preview starter folders")
                    Icon(if (preview) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null)
                }
                if (preview) recipe.folders.filter { it.enabled }.forEach { Text("• ${it.name}", modifier = Modifier.padding(vertical = 4.dp)) }

            }
            item { TextButton(onClick = { details = !details }) { Text(if (details) "Hide optional details" else "Optional details"); Icon(if (details) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null) } }
            if (details) item { OutlinedTextField(author, { author = it.take(120) }, label = { Text("Author (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            if (details) item { GenreInput(genres, { genres = it }) }
            if (details) item {
                Text("Workspace color", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Iris", "Ocean", "Ember", "Moss").forEachIndexed { i, name ->
                        FilterChip(selected = cover == i, onClick = { cover = i }, label = { Text(name) })
                    }
                }
            }
        }
        Button(onClick = { onCreate(title.trim(), author.trim(), genres, cover, 0, standard, templateId) }, enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth().padding(20.dp).height(52.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Create workspace")
        }
    }
}

@Composable
private fun StructureChoice(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).selectable(selected = selected, role = androidx.compose.ui.semantics.Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .3f),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
