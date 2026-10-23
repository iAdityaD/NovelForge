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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelforge.core.model.*
import com.novelforge.domain.preferences.*
import com.novelforge.domain.manuscript.*
import com.novelforge.presentation.*
import kotlinx.coroutines.launch

@Composable
fun ForgeApp() {
    val workspace: WorkspaceViewModel = hiltViewModel()
    val editor: EditorViewModel = hiltViewModel()
    val settings: SettingsViewModel = hiltViewModel()
    val reader: ReaderViewModel = hiltViewModel()
    val library: LibraryViewModel = hiltViewModel()
    val analytics: AnalyticsViewModel = hiltViewModel()
    val backups: BackupViewModel = hiltViewModel()
    val nav: NavigationViewModel = hiltViewModel()
    val workspaceUi by workspace.state.collectAsStateWithLifecycle()
    val editorUi by editor.state.collectAsStateWithLifecycle()
    val prefs by settings.state.collectAsStateWithLifecycle()
    val books = workspaceUi.books
    val entries = workspaceUi.entries
    val excludedChapters = workspaceUi.placements.filter { !it.included }.map { it.documentId }.toSet()
    val days by analytics.days.collectAsStateWithLifecycle()
    val busy by backups.busy.collectAsStateWithLifecycle()
    val workspaceError by workspace.error.collectAsStateWithLifecycle()
    val editorError by editor.error.collectAsStateWithLifecycle()
    val backupError by backups.error.collectAsStateWithLifecycle()
    val settingsError by settings.error.collectAsStateWithLifecycle()
    val error = workspaceError ?: editorError ?: backupError ?: settingsError ?: editorUi.error
    var screen by rememberSaveable { mutableStateOf(Destination.Library) }
    var bookId by rememberSaveable { mutableStateOf(nav.lastBook()) }
    var workspaceTab by rememberSaveable { mutableIntStateOf(0) }
    var editBook by remember { mutableStateOf<Book?>(null) }
    var showEntry by remember { mutableStateOf<String?>(null) }
    var entryFolder by remember { mutableStateOf<Long?>(null) }
    var drawerFolder by rememberSaveable { mutableStateOf<Long?>(null) }
    var newDrawerFolder by remember { mutableStateOf(false) }
    var exportBook by rememberSaveable { mutableStateOf("") }
    var markdown by rememberSaveable { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    LaunchedEffect(screen, editBook) {
        if (screen !in listOf(Destination.Editor, Destination.New) && editBook == null) {
            focus.clearFocus()
            keyboard?.hide()
        }
    }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val selected = books.find { it.id == bookId }
    val bookEntries = entries.filter { it.bookId == bookId }
    fun project(id: String) {
        if (bookId != id) workspaceTab = 0
        bookId = id
        nav.rememberBook(id)
        screen = Destination.Project
    }
    fun open(id: Long, read: Boolean? = null) {
        editor.openEntry(id) {
            bookId = editor.state.value.entry?.bookId ?: bookId
            screen = if (read == true || (read == null && editor.state.value.entry?.kind == "Guide")) Destination.Reader else Destination.Editor
        }
    }
    val export =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream")
        ) { uri ->
            if (uri != null)
                backups.export(uri.toString(), exportBook, markdown) {
                    scope.launch { snack.showSnackbar("Export saved") }
                }
        }
    val import =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) backups.importBackup(uri.toString()) { project(it) }
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
        if (screen in listOf(Destination.Editor, Destination.Reader) && editorUi.entry == null) {
            val id = nav.lastEntry()
            if (id > 0) editor.openEntry(id) {} else screen = Destination.Library
        }
    }
    LaunchedEffect(editorUi.entry?.id) { drawerFolder = editorUi.entry?.folderId }
    LaunchedEffect(editorUi.entry?.deleted) {
        if (
            editorUi.entry?.deleted == true &&
                screen in listOf(Destination.Editor, Destination.Reader)
        )
            screen = Destination.Project
    }
    BackHandler(screen != Destination.Library || drawer.isOpen) {
        if (drawer.isOpen) scope.launch { drawer.close() }
        else if (screen in listOf(Destination.Editor, Destination.Reader))
            editor.leave { screen = Destination.Project }
        else screen = Destination.Library
    }
    val roots =
        listOf(Destination.Library, Destination.Guide, Destination.Analytics, Destination.Settings)
    if (editBook == null) {
                            ModalNavigationDrawer(
                                drawerState = drawer,
                                gesturesEnabled = screen in listOf(Destination.Project, Destination.Editor, Destination.Reader),
                                drawerContent = {
                                    if (drawer.isOpen || drawer.targetValue == DrawerValue.Open) ModalDrawerSheet(
                                        Modifier.fillMaxWidth(.9f).widthIn(max = 360.dp),
                                        drawerContainerColor = MaterialTheme.colorScheme.background,
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth()
                                                .padding(start = 16.dp, end = 4.dp),
                                            verticalAlignment =
                                                androidx.compose.ui.Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                selected?.title?.replace('\n', ' ') ?: "Workspace",
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1,
                                                overflow =
                                                    androidx.compose.ui.text.style.TextOverflow
                                                        .Ellipsis,
                                            )
                                            ActionIcon(
                                                Icons.Outlined.Close,
                                                "Close file explorer",
                                                { scope.launch { drawer.close() } },
                                            )
                                        }
                                        Row(
                                            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                            verticalAlignment =
                                                androidx.compose.ui.Alignment.CenterVertically,
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    scope.launch { drawer.close() }
                                                    editor.leave { screen = Destination.Project }
                                                },
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text("Open workspace")
                                            }
                                            ActionIcon(
                                                Icons.Outlined.NoteAdd,
                                                "New file",
                                                {
                                                    entryFolder = drawerFolder
                                                    showEntry =
                                                        defaultKindForFolder(
                                                            bookEntries
                                                                .find { it.id == drawerFolder }
                                                                ?.title
                                                        )
                                                    scope.launch { drawer.close() }
                                                },
                                            )
                                            ActionIcon(
                                                Icons.Outlined.CreateNewFolder,
                                                "New folder",
                                                { newDrawerFolder = true },
                                            )
                                        }
                                        HorizontalDivider()
                                        selected?.let { b ->
                                            ExplorerTree(
                                                workspace,
                                                b,
                                                bookEntries,
                                                { id ->
                                                    scope.launch { drawer.close() }
                                                    open(id)
                                                },
                                                { drawerFolder = it },
                                                drawerFolder,
                                                Modifier.weight(1f),
                                                activeFileId = editorUi.entry?.id,
                                            )
                                        }
                                    }
                                },
                            ) {

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            if (screen in roots)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                ) {
                    listOf(
                            Triple(Destination.Library, "Library", Icons.Outlined.GridView),
                            Triple(Destination.Guide, "Guide", Icons.Outlined.AutoStories),
                            Triple(Destination.Analytics, "Analytics", Icons.Outlined.BarChart),
                            Triple(Destination.Settings, "Settings", Icons.Outlined.Tune),
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
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) },
                label = "screen",
            ) { route ->
                when (route) {
                    Destination.Library ->
                        LibraryScreen(
                            books,
                            entries,
                            { project(it) },
                            { screen = Destination.New },
                            { importBackup() },
                            { workspace.loadDemo { project(it) } },
                            { open(it) },
                            { screen = Destination.Search },
                            excludedChapters,
                        )
                    Destination.New ->
                        NewWorkspaceScreen(prefs.starterFolders, { screen = Destination.Library }) { title, author, genre, cover, target, standard ->
                            workspace.createBook(title, author, genre, cover, target, standard) { created ->
                                project(created.bookId)
                                workspaceTab = 0
                            }
                        }
                    Destination.Guide -> GuideScreen()
                    Destination.Analytics -> AnalyticsScreen(books, entries, days, excludedChapters)
                    Destination.Settings ->
                        SettingsNavigation(
                            settings,
                            books,
                            { importBackup() },
                            { backup(it, false) },
                        )
                    Destination.Search ->
                        Column {
                            TextButton(onClick = { screen = Destination.Library }) {
                                Text("Back to library")
                            }
                            SearchScreen(library, books, { open(it) })
                        }
                    Destination.Project ->
                        selected?.let { b ->
                            WorkspaceStudio(
                                workspace, b, bookEntries, workspaceTab,
                                { workspaceTab = it },
                                { screen = Destination.Library },
                                { open(it) }, { open(it, true) }, { editBook = b },
                                { folder, kind -> entryFolder = folder; showEntry = kind },
                                { backup(b.id, it) }, { scope.launch { drawer.open() } }, prefs.synopsisLimit,
                            )
                        }
                    Destination.Editor ->
                        editorUi.entry?.let { e ->
                                EditorScreen(
                                    editor,
                                    prefs,
                                    e,
                                    selected,
                                    bookEntries,
                                    { editor.leave { screen = Destination.Project } },
                                    { editor.leave { screen = Destination.Reader } },
                                    { open(it) },
                                    {
                                        workspace.deleteEntry(e.id) { screen = Destination.Project }
                                    },
                                    { scope.launch { drawer.open() } },
                                )
                        }
                    Destination.Reader ->
                        editorUi.entry?.let { e ->
                            ReaderScreen(
                                reader,
                                e,
                                selected,
                                manuscriptChapters(bookEntries, workspaceUi.placements),
                                { screen = Destination.Project },
                                { screen = Destination.Editor },
                                { open(it, true) },
                                { scope.launch { drawer.open() } },
                            )
                        }
                }
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
    } // Shared workspace drawer
    if (newDrawerFolder)
        NameDialog("New folder", "Folder name", onDismiss = { newDrawerFolder = false }) { name ->
            workspace.createFolder(bookId, name, drawerFolder)
            newDrawerFolder = false
        }
    }
    editBook?.let { b ->
        NovelDetailsScreen(b, { editBook = null }) { updated ->
            workspace.editBook(updated) { editBook = null }
        }
    }
    showEntry?.let { kind ->
        EntryDialog(kind, { showEntry = null }, manuscriptOnly = screen == Destination.Project && kind == "Chapter") { title, type ->
            workspace.createEntry(bookId, title, type, null, entryFolder) {
                showEntry = null
                open(it)
            }
        }
    }
    error?.let { message ->
        AlertDialog(
            onDismissRequest = {
                workspace.clearError()
                editor.clearError()
                backups.clearError()
                settings.clearError()
                editor.clearSessionError()
            },
            title = { Text("Couldn’t complete that") },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        workspace.clearError()
                        editor.clearError()
                        backups.clearError()
                        settings.clearError()
                        editor.clearSessionError()
                    }
                ) {
                    Text("OK")
                }
            },
        )
    }
}

private enum class Destination {
    Library,
    Guide,
    Analytics,
    Settings,
    New,
    Search,
    Project,
    Editor,
    Reader,
}
