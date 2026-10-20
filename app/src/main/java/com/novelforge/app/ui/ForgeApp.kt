package com.novelforge.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelforge.app.ForgeViewModel
import com.novelforge.app.data.*
import kotlinx.coroutines.launch

@Composable
fun ForgeApp(vm: ForgeViewModel) {
    val books by vm.books.collectAsStateWithLifecycle()
    val entries by vm.summaries.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf("library") }
    var bookId by rememberSaveable { mutableStateOf(vm.prefs.getString("lastBook", "") ?: "") }
    var showBook by remember { mutableStateOf(false) }
    var editBook by remember { mutableStateOf<Book?>(null) }
    var showEntry by remember { mutableStateOf<String?>(null) }
    var exportBook by remember { mutableStateOf("") }
    var markdown by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val selected = books.find { it.id == bookId }
    val bookEntries = entries.filter { it.bookId == bookId }
    fun project(id: String) {
        bookId = id
        vm.prefs.edit().putString("lastBook", id).apply()
        screen = "project"
    }
    fun open(id: Long, read: Boolean = false) {
        vm.openEntry(id) {
            bookId = vm.editor?.bookId ?: bookId
            screen = if (read) "reader" else "editor"
        }
    }
    val export =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream")
        ) { uri ->
            if (uri != null)
                vm.export(uri, exportBook, markdown) {
                    scope.launch { snack.showSnackbar("Export saved to your selected destination") }
                }
        }
    val import =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) vm.importBackup(uri) { project(it) }
        }
    fun backup(id: String, md: Boolean) {
        exportBook = id
        markdown = md
        export.launch(if (md) "NovelForge-manuscript.md" else "NovelForge-backup.novelforge.json")
    }
    LaunchedEffect(Unit) {
        if (screen in listOf("editor", "reader") && vm.editor == null) {
            val last = vm.prefs.getLong("lastEntry", 0)
            if (last > 0) vm.openEntry(last) {} else screen = "project"
        }
    }
    BackHandler(screen != "library") {
        if (screen == "editor" || screen == "reader") vm.leave { screen = "project" }
        else screen = "library"
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            if (screen !in listOf("editor", "reader"))
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    listOf(
                            Triple("library", "Library", Icons.Outlined.AutoStories),
                            Triple("project", "Workspace", Icons.Outlined.DashboardCustomize),
                            Triple("search", "Search", Icons.Outlined.Search),
                            Triple("settings", "Settings", Icons.Outlined.Tune),
                        )
                        .forEach { (route, label, icon) ->
                            NavigationBarItem(
                                selected = screen == route,
                                onClick = { screen = route },
                                icon = { Icon(icon, label) },
                                label = { Text(label) },
                                colors =
                                    NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                            )
                        }
                }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                "library" ->
                    LibraryScreen(
                        books,
                        entries,
                        onBook = { project(it) },
                        onNew = { showBook = true },
                        onImport = {
                            import.launch(
                                arrayOf(
                                    "application/json",
                                    "application/octet-stream",
                                    "text/plain",
                                )
                            )
                        },
                        onDemo = { vm.loadDemo { project(it) } },
                        onContinue = { open(it) },
                        onSearch = { screen = "search" },
                    )
                "project" ->
                    if (selected == null)
                        EmptyState(
                            "Choose your next world",
                            "Open a novel from your library, or create a new one.",
                            action = "Open library",
                            onAction = { screen = "library" },
                        )
                    else
                        ProjectScreen(
                            selected,
                            bookEntries,
                            onOpen = { open(it) },
                            onRead = { open(it, true) },
                            onAdd = { showEntry = it },
                            onEdit = { editBook = selected },
                            onExport = { backup(bookId, it) },
                            onMove = { id, delta -> vm.moveEntry(id, delta) },
                            onStatus = { id, status -> vm.boardStatus(id, status) },
                            onArc = { id, arc -> vm.assignArc(id, arc) },
                            connections = vm.connections,
                            onConnections = { vm.loadConnections(bookId) },
                        )
                "editor" ->
                    vm.editor?.let { e ->
                        EditorScreen(
                            vm,
                            e,
                            selected,
                            bookEntries,
                            onBack = { vm.leave { screen = "project" } },
                            onRead = { vm.leave { screen = "reader" } },
                            onOpen = { open(it) },
                            onDelete = { vm.deleteEntry(e.id) { screen = "project" } },
                        )
                    }
                "reader" ->
                    vm.editor?.let { e ->
                        ReaderScreen(
                            vm,
                            e,
                            selected,
                            bookEntries,
                            onBack = { screen = "project" },
                            onEdit = { screen = "editor" },
                            onOpen = { open(it, true) },
                        )
                    }
                "search" -> SearchScreen(vm, books, onOpen = { open(it) })
                "settings" ->
                    SettingsScreen(
                        vm,
                        onImport = {
                            import.launch(
                                arrayOf(
                                    "application/json",
                                    "application/octet-stream",
                                    "text/plain",
                                )
                            )
                        },
                        onBackup = {
                            if (selected != null) backup(bookId, false)
                            else
                                scope.launch {
                                    snack.showSnackbar("Open a project before creating a backup")
                                }
                        },
                    )
            }
            if (vm.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
    if (showBook)
        BookDialog(null, onDismiss = { showBook = false }) {
            title,
            author,
            genre,
            cover,
            target,
            synopsis ->
            vm.createBook(title, author, genre, cover, target, synopsis) { id ->
                showBook = false
                project(id)
            }
        }
    editBook?.let { book ->
        BookDialog(book, onDismiss = { editBook = null }) {
            title,
            author,
            genre,
            cover,
            target,
            synopsis ->
            vm.editBook(
                book.copy(
                    title = title,
                    author = author,
                    genre = genre,
                    cover = cover,
                    target = target,
                    synopsis = synopsis,
                )
            )
            editBook = null
        }
    }
    showEntry?.let { kind ->
        EntryDialog(kind, onDismiss = { showEntry = null }) { title, type ->
            vm.createEntry(bookId, title, type, null) { id ->
                showEntry = null
                open(id)
            }
        }
    }
    vm.error?.let { message ->
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            title = { Text("We couldn't complete that") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { vm.clearError() }) { Text("OK") } },
        )
    }
}
