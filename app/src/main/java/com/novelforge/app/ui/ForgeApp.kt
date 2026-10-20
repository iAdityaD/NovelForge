package com.novelforge.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
    val days by vm.writingDays.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf("library") }
    var bookId by rememberSaveable { mutableStateOf(vm.prefs.getString("lastBook", "") ?: "") }
    var editBook by remember { mutableStateOf<Book?>(null) }
    var showEntry by remember { mutableStateOf<String?>(null) }
    var entryFolder by remember { mutableStateOf<Long?>(null) }
    var exportBook by rememberSaveable { mutableStateOf("") }
    var markdown by rememberSaveable { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val drawer = rememberDrawerState(DrawerValue.Closed)
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
                    scope.launch { snack.showSnackbar("Export saved") }
                }
        }
    val import =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) vm.importBackup(uri) { project(it) }
        }
    fun importBackup() {
        import.launch(arrayOf("application/json", "application/octet-stream", "text/plain"))
    }
    fun backup(id: String, md: Boolean) {
        exportBook = id
        markdown = md
        export.launch(if (md) "NovelForge-manuscript.md" else "NovelForge-backup.novelforge.json")
    }
    LaunchedEffect(Unit) {
        if (screen in listOf("editor", "reader") && vm.editor == null) {
            val id = vm.prefs.getLong("lastEntry", 0)
            if (id > 0) vm.openEntry(id) {} else screen = "library"
        }
    }
    LaunchedEffect(vm.editor?.deleted) {
        if (vm.editor?.deleted == true && screen in listOf("editor", "reader")) screen = "project"
    }
    BackHandler(screen != "library") {
        if (screen in listOf("editor", "reader")) vm.leave { screen = "project" }
        else screen = "library"
    }
    val roots = listOf("library", "guide", "analytics", "settings")
    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            if (screen in roots)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                ) {
                    listOf(
                            Triple("library", "Library", Icons.Outlined.GridView),
                            Triple("guide", "Guide", Icons.Outlined.AutoStories),
                            Triple("analytics", "Analytics", Icons.Outlined.BarChart),
                            Triple("settings", "Settings", Icons.Outlined.Tune),
                        )
                        .forEach { (route, label, icon) ->
                            NavigationBarItem(
                                selected = screen == route,
                                onClick = { screen = route },
                                icon = { Icon(icon, null) },
                                label = { Text(label) },
                                colors =
                                    NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                            )
                        }
                }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) },
                label = "screen",
            ) { route ->
                when (route) {
                    "library" ->
                        LibraryScreen(
                            books,
                            entries,
                            { project(it) },
                            { screen = "new" },
                            { importBackup() },
                            { vm.loadDemo { project(it) } },
                            { open(it) },
                            { screen = "search" },
                        )
                    "new" ->
                        NewWorkspaceScreen({ screen = "library" }) {
                            title,
                            author,
                            genre,
                            cover,
                            target,
                            synopsis,
                            folders ->
                            vm.createBook(title, author, genre, cover, target, synopsis, folders) {
                                project(it)
                            }
                        }
                    "guide" -> GuideScreen()
                    "analytics" -> AnalyticsScreen(books, entries, days)
                    "settings" ->
                        SettingsNavigation(vm, books, { importBackup() }, { backup(it, false) })
                    "search" ->
                        Column {
                            TextButton(onClick = { screen = "library" }) { Text("Back to library") }
                            SearchScreen(vm, books, { open(it) })
                        }
                    "project" ->
                        selected?.let { b ->
                            WorkspaceScreen(
                                vm,
                                b,
                                bookEntries,
                                { screen = "library" },
                                { open(it) },
                                { editBook = b },
                                { folder, kind ->
                                    entryFolder = folder
                                    showEntry = kind
                                },
                                { backup(b.id, it) },
                            )
                        }
                    "editor" ->
                        vm.editor?.let { e ->
                            ModalNavigationDrawer(
                                drawerState = drawer,
                                drawerContent = {
                                    ModalDrawerSheet(Modifier.fillMaxWidth(.88f)) {
                                        TextButton(
                                            onClick = {
                                                scope.launch { drawer.close() }
                                                vm.leave { screen = "project" }
                                            }
                                        ) {
                                            Text("Open workspace")
                                        }
                                        selected?.let { b ->
                                            ExplorerTree(
                                                vm,
                                                b,
                                                bookEntries,
                                                { id ->
                                                    scope.launch { drawer.close() }
                                                    open(id)
                                                },
                                                {},
                                                e.folderId,
                                                Modifier.weight(1f),
                                            )
                                        }
                                    }
                                },
                            ) {
                                EditorScreen(
                                    vm,
                                    e,
                                    selected,
                                    bookEntries,
                                    { vm.leave { screen = "project" } },
                                    { vm.leave { screen = "reader" } },
                                    { open(it) },
                                    { vm.deleteEntry(e.id) { screen = "project" } },
                                    { scope.launch { drawer.open() } },
                                )
                            }
                        }
                    "reader" ->
                        vm.editor?.let { e ->
                            ReaderScreen(
                                vm,
                                e,
                                selected,
                                bookEntries,
                                { screen = "project" },
                                { screen = "editor" },
                                { open(it, true) },
                            )
                        }
                }
            }
            if (vm.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
    editBook?.let { b ->
        BookDialog(b, { editBook = null }) { title, author, genre, cover, target, synopsis ->
            vm.editBook(
                b.copy(
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
        EntryDialog(kind, { showEntry = null }) { title, type ->
            vm.createEntry(bookId, title, type, null, entryFolder) {
                showEntry = null
                open(it)
            }
        }
    }
    vm.error?.let { message ->
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            title = { Text("Couldn’t complete that") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { vm.clearError() }) { Text("OK") } },
        )
    }
}
