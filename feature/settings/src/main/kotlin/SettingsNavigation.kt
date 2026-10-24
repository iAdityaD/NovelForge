package com.novelforge.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelforge.core.model.Book
import com.novelforge.presentation.SettingsViewModel

@Composable
fun SettingsNavigation(
    vm: SettingsViewModel,
    books: List<Book>,
    onImport: () -> Unit,
    onBackup: (String) -> Unit,
) {
    val preferences by vm.state.collectAsStateWithLifecycle()
    var page by rememberSaveable { mutableStateOf("") }
    var backupId by rememberSaveable { mutableStateOf<String?>(null) }
    var picker by remember { mutableStateOf(false) }
    val trash by vm.trash.collectAsStateWithLifecycle()
    fun back() {
        page = when (page) {
            "Workspace structure", "Writing limits" -> "Writing"
            else -> ""
        }
    }
    BackHandler(page.isNotEmpty()) { back() }
    val scrollState = rememberLazyListState()
    LaunchedEffect(page) { scrollState.scrollToItem(0) }
    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize().imePadding().scrollIndicator(scrollState),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            if (page.isEmpty()) Text("Settings", style = MaterialTheme.typography.headlineLarge)
            else
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ActionIcon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        if (page == "Workspace structure" || page == "Writing limits") "Back to writing settings"
                        else "Back to settings",
                        { back() },
                    )
                    Text(page, style = MaterialTheme.typography.headlineMedium)
                }
        }
        if (page.isEmpty()) {
            item {
                SettingsAction(
                    Icons.Outlined.Palette,
                    "Appearance",
                    "Light and dark themes",
                    { page = "Appearance" },
                )
                SettingsAction(
                    Icons.Outlined.EditNote,
                    "Editor",
                    "Typography and Markdown",
                    { page = "Editor" },
                )
                SettingsAction(
                    Icons.Outlined.Rule,
                    "Writing",
                    "Structure and limits",
                    { page = "Writing" },
                )

            }
            item {
                HorizontalDivider()
                SettingsAction(
                    Icons.Outlined.SaveAlt,
                    "Data & backup",
                    "Export and restore projects",
                    { page = "Data & backup" },
                )
                SettingsAction(
                    Icons.Outlined.RestoreFromTrash,
                    "Recently deleted",
                    "Restore files and folders",
                    { page = "Recently deleted" },
                )
                SettingsAction(
                    Icons.Outlined.Info,
                    "About",
                    "Version and privacy",
                    { page = "About" },
                )
            }
        }
        if (page == "Writing")
            item {
                SettingsAction(
                    Icons.Outlined.TextFields,
                    "Workspace structure",
                    "Folders for standard projects",
                    { page = "Workspace structure" },
                )
                SettingsAction(
                    Icons.Outlined.Rule,
                    "Writing limits",
                    "Synopsis word limit",
                    { page = "Writing limits" },
                )
            }
        if (page == "Writing limits")
            item { SynopsisLimitSetting(preferences.synopsisLimit) { vm.setSynopsisLimit(it) } }
        if (page == "Workspace structure")
            item { WorkspaceStructureSettings(preferences.starterFolders) { vm.setStarterFolders(it) } }
        if (page == "Appearance")
            item {
                SettingToggle(
                    "Dark theme",
                    "Charcoal surfaces with violet accents",
                    preferences.dark,
                    { vm.updateDark(it) },
                )
            }
        if (page == "Editor") {
            item {
                SettingToggle(
                    "Monospace editor",
                    "A code-editor feel for Markdown",
                    preferences.editorMono,
                    { vm.updateEditorMono(it) },
                )
            }
            item {
                SettingToggle(
                    "Markdown syntax colors",
                    "Highlight headings, emphasis, quotes and links",
                    preferences.syntaxColors,
                    { vm.updateSyntaxColors(it) },
                )
            }
            item {
                Text("Text size · ${preferences.fontSize.toInt()} sp")
                Slider(preferences.fontSize, { vm.setFont(it) }, valueRange = 14f..30f, steps = 15)
                Text("The first sentence opens a door.",
                    fontSize = preferences.fontSize.sp,
                    fontFamily = if (preferences.editorMono) FontFamily.Monospace else FontFamily.Default,
                    modifier = Modifier.padding(vertical = 16.dp))
            }
        }
        if (page == "Data & backup") {
            item {
                Text("Project recovery backup", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Choose a project to back up.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Box {
                    OutlinedButton(
                        onClick = { picker = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            books.find { it.id == backupId }?.title ?: "Select workspace",
                            Modifier.weight(1f),
                        )
                        Icon(Icons.Outlined.ExpandMore, null)
                    }
                    DropdownMenu(picker, { picker = false }) {
                        books.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.title) },
                                onClick = {
                                    backupId = b.id
                                    picker = false
                                },
                            )
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = { backupId?.let(onBackup) },
                    enabled = books.any { it.id == backupId },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Export backup")
                }
                TextButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                    Text("Restore from backup")
                }
            }
            item {
                SurfaceCard {
                    Text(
                        "You control the destination",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Includes documents, folders, custom type labels, planning/manuscript roles, trash, checkpoints and writing history. App settings and unsaved drafts are not included.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Export before uninstalling or switching development builds.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        if (page == "Recently deleted") {
            if (trash.isEmpty())
                item {
                    EmptyState(
                        "Trash is empty",
                        "Files and folders you delete will appear here.",
                        Icons.Outlined.DeleteOutline,
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
        if (page == "About")
            item {
                SurfaceCard {
                    Text("NovelForge", style = MaterialTheme.typography.headlineMedium)
                    Text("0.6.0 · Writing workspace", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Your stories stay in a local SQLite database. No internet permission, analytics service, or account. The explorer organizes files inside your workspace; it does not browse arbitrary Android folders.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    description: String,
    value: Boolean,
    change: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(value, change)
    }
}

@Composable
private fun SynopsisLimitSetting(limit: Int, onSave: (Int) -> Unit) {
    var input by rememberSaveable(limit) { mutableStateOf(limit.toString()) }
    val value = input.toIntOrNull()
    val valid = value != null && value in 50..10000
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = input, onValueChange = { input = it },
            label = { Text("Synopsis word limit") },
            supportingText = { Text(if (valid) "50–10,000 words" else "Enter a number from 50 to 10,000") },
            isError = !valid, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Existing summaries are never shortened automatically.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = { value?.let(onSave) }, enabled = valid && value != limit,
            modifier = Modifier.fillMaxWidth()) { Text("Save limit") }
    }
}
