package com.novelforge.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelforge.app.ForgeViewModel
import com.novelforge.app.data.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SearchScreen(vm: ForgeViewModel, books: List<Book>, onOpen: (Long) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(query) { vm.search(query) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Eyebrow("A THREAD THROUGH EVERY STORY")
            Spacer(Modifier.height(8.dp))
            Text("Find a thought.", style = MaterialTheme.typography.headlineLarge)
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search chapters, characters, notes…") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty())
                        ActionIcon(Icons.Outlined.Close, "Clear search", { query = "" })
                },
                singleLine = true,
            )
        }
        if (query.isBlank())
            item {
                EmptyState(
                    "Nothing gets lost here",
                    "Search the words inside every chapter and note, across all your novels. Everything stays on this device.",
                    Icons.Outlined.TravelExplore,
                )
            }
        else if (vm.results.isEmpty())
            item {
                EmptyState(
                    "No matching pages",
                    "Try another name, place, or phrase.",
                    Icons.Outlined.SearchOff,
                )
            }
        else {
            item { Eyebrow("${vm.results.size} MATCHING PAGES") }
            items(vm.results, key = { it.id }) { e ->
                Column {
                    Text(
                        books.find { it.id == e.bookId }?.title?.replace('\n', ' ') ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    EntryRow(e, onClick = { onOpen(e.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: ForgeViewModel, onImport: () -> Unit, onBackup: () -> Unit) {
    val context = LocalContext.current
    val trash by vm.trash.collectAsStateWithLifecycle()
    var showTrash by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf("Calculating…") }
    LaunchedEffect(Unit) {
        size =
            withContext(Dispatchers.IO) {
                val file = context.getDatabasePath("novelforge.db")
                val bytes =
                    listOf(file, java.io.File(file.path + "-wal"), java.io.File(file.path + "-shm"))
                        .sumOf { it.length() }
                if (bytes < 1048576) "${bytes/1024} KB" else "%.1f MB".format(bytes / 1048576.0)
            }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Eyebrow("A SPACE THAT FEELS LIKE YOU")
            Spacer(Modifier.height(8.dp))
            Text("Make it yours.", style = MaterialTheme.typography.headlineLarge)
        }
        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Private by design", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "No account. No tracking. No internet permission.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            SectionTitle("Appearance")
            ListItem(
                headlineContent = { Text("Dark studio") },
                supportingContent = { Text("A quieter workspace after hours") },
                leadingContent = { Icon(Icons.Outlined.DarkMode, null) },
                trailingContent = {
                    Switch(checked = vm.dark, onCheckedChange = { vm.updateDark(it) })
                },
                colors =
                    ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
            )
            Text(
                "Writing & reading text · ${vm.fontSize.toInt()} sp",
                Modifier.padding(top = 12.dp),
            )
            Slider(
                value = vm.fontSize,
                onValueChange = { vm.setFont(it) },
                valueRange = 14f..30f,
                steps = 15,
            )
        }
        item {
            SectionTitle("Your words, safely kept")
            Spacer(Modifier.height(12.dp))
            SurfaceCard(Modifier.fillMaxWidth()) {
                Row {
                    Text("Local project data", Modifier.weight(1f))
                    Text(size, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Text is stored once. Covers are drawn on-device. Search indexes and the latest 20 manual checkpoints per page are included.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            SettingsAction(
                Icons.Outlined.Backup,
                "Back up current project",
                "Choose a folder outside the app",
                onBackup,
            )
            SettingsAction(
                Icons.Outlined.FileDownload,
                "Restore a backup",
                "Imports a separate copy of your novel",
                onImport,
            )
            SettingsAction(
                Icons.Outlined.RestoreFromTrash,
                "Recently deleted",
                "${trash.size} pages you can restore",
                { showTrash = true },
            )
        }
        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Eyebrow("A NOTE ABOUT BACKUPS")
                Spacer(Modifier.height(10.dp))
                Text(
                    "Uninstalling the app removes its local data. Save a project backup to a location you control before changing devices.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Portable backups include active pages and book details. Manual revision history, trash, and reading positions stay on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("novelforge", style = MaterialTheme.typography.titleLarge)
                Text(
                    "0.1.0 · A little space for big stories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (showTrash)
        ModalBottomSheet(onDismissRequest = { showTrash = false }) {
            Column(Modifier.padding(24.dp)) {
                Text("Recently deleted", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.heightIn(max = 450.dp)) {
                    if (trash.isEmpty())
                        item {
                            EmptyState(
                                "Nothing in the wings",
                                "Deleted pages will appear here until restored.",
                            )
                        }
                    items(trash, key = { it.id }) { e ->
                        ListItem(
                            headlineContent = { Text(e.title) },
                            supportingContent = { Text(e.kind) },
                            trailingContent = {
                                TextButton(onClick = { vm.restoreEntry(e.id) }) { Text("Restore") }
                            },
                        )
                    }
                }
            }
        }
}

@Composable
fun SettingsAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null) },
        trailingContent = { Icon(Icons.Outlined.ChevronRight, null) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
    )
}
