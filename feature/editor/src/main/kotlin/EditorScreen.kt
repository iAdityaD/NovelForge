package com.novelforge.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelforge.core.model.*
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*
import com.novelforge.presentation.EditorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    vm: EditorViewModel,
    preferences: com.novelforge.domain.preferences.AppPreferences,
    e: Entry,
    book: Book?,
    entries: List<EntrySummary>,
    onBack: () -> Unit,
    onRead: () -> Unit,
    onOpen: (Long) -> Unit,
    onDelete: () -> Unit,
    onExplorer: () -> Unit = onBack,
) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val refs by vm.references.collectAsStateWithLifecycle()
    var focus by rememberSaveable { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var statusMenu by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var field by remember(e.id) { mutableStateOf(TextFieldValue(e.body)) }
    LaunchedEffect(e.body) {
        if (field.text != e.body)
            field =
                TextFieldValue(e.body, TextRange(field.selection.end.coerceAtMost(e.body.length)))
    }
    fun insert(before: String, after: String = "") {
        val start = field.selection.min
        val end = field.selection.max
        val text =
            field.text.substring(0, start) +
                before +
                field.text.substring(start, end) +
                after +
                field.text.substring(end)
        field = TextFieldValue(text, TextRange(end + before.length))
        vm.updateBody(text)
    }
    val scroll = rememberScrollState()
    val words = remember(e.body) { wordCount(e.body) }
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionIcon(Icons.Outlined.MenuOpen, "Open file explorer", onExplorer)
            Column(Modifier.weight(1f)) {
                Text(
                    if (focus) "Focus mode" else book?.title?.replace('\n', ' ') ?: "Your story",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                Text(
                    ui.saveStatus,
                    Modifier.clickable { vm.flushBackground() },
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (ui.saveStatus.startsWith("Save failed")) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ActionIcon(
                if (focus) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen,
                if (focus) "Exit focus mode" else "Enter focus mode",
                { focus = !focus },
            )
            if (!focus) ActionIcon(Icons.Outlined.MenuBook, "Read this page", onRead)
            Box {
                ActionIcon(Icons.Outlined.MoreVert, "Page actions", { menu = true })
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Save checkpoint") },
                        onClick = {
                            menu = false
                            vm.checkpoint()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Revision history") },
                        onClick = {
                            menu = false
                            vm.loadRevisions()
                            sheet = "history"
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Story references") },
                        onClick = {
                            menu = false
                            vm.loadConnections(e.bookId)
                            sheet = "references"
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Writing guidance") },
                        onClick = {
                            menu = false
                            sheet = "guidance"
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Move to trash") },
                        onClick = {
                            menu = false
                            confirmDelete = true
                        },
                    )
                }
            }
        }
        if (!focus)
            TextButton(onClick = onBack) {
                Icon(Icons.Outlined.FolderOpen, null, Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${entries.find {it.id==e.folderId}?.title ?: "Workspace"} / ${e.title}.md",
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            Modifier.weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (!focus)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(e.kind, Modifier.weight(1f))
                    Box {
                        TextButton(onClick = { statusMenu = true }) {
                            StatusPill(e.status)
                            Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = statusMenu,
                            onDismissRequest = { statusMenu = false },
                        ) {
                            statuses.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        vm.setStatus(s)
                                        statusMenu = false
                                    },
                                )
                            }
                        }
                    }
                }
            BasicTextField(
                value = e.title,
                onValueChange = { vm.updateTitle(it.take(200)) },
                textStyle =
                    MaterialTheme.typography.headlineLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                modifier = Modifier.fillMaxWidth(),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (e.title.isEmpty())
                        Text(
                            "Untitled page",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    inner()
                },
            )
            BasicTextField(
                value = field,
                visualTransformation =
                    if (preferences.syntaxColors) MarkdownSyntax(MaterialTheme.colorScheme.primary)
                    else androidx.compose.ui.text.input.VisualTransformation.None,
                onValueChange = {
                    field = it
                    vm.updateBody(it.text)
                },
                modifier =
                    Modifier.fillMaxWidth().heightIn(min = 350.dp).semantics {
                        contentDescription = "Manuscript text"
                    },
                textStyle =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily =
                            if (e.kind == "Screenplay" || preferences.editorMono)
                                FontFamily.Monospace
                            else FontFamily.SansSerif,
                        fontSize = preferences.fontSize.sp,
                        lineHeight = (preferences.fontSize * 1.6f).sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (field.text.isEmpty())
                        Text(
                            "Let the story begin…",
                            fontFamily = FontFamily.Serif,
                            fontSize = preferences.fontSize.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    inner()
                },
            )
            Spacer(Modifier.height(120.dp))
        }
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                if (!focus)
                    Row(
                        Modifier.fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ActionIcon(
                            Icons.AutoMirrored.Outlined.Undo,
                            "Undo",
                            { vm.undo() },
                            ui.canUndo,
                        )
                        ActionIcon(
                            Icons.AutoMirrored.Outlined.Redo,
                            "Redo",
                            { vm.redo() },
                            ui.canRedo,
                        )
                        VerticalDivider(Modifier.height(22.dp))
                        ActionIcon(
                            Icons.Outlined.FormatBold,
                            "Bold selected text",
                            { insert("**", "**") },
                        )
                        ActionIcon(
                            Icons.Outlined.FormatItalic,
                            "Italic selected text",
                            { insert("*", "*") },
                        )
                        ActionIcon(Icons.Outlined.Title, "Heading", { insert("\n## ") })
                        ActionIcon(
                            Icons.AutoMirrored.Outlined.FormatListBulleted,
                            "Bullet list",
                            { insert("\n- ") },
                        )
                        ActionIcon(Icons.Outlined.Checklist, "Checklist", { insert("\n- [ ] ") })
                        ActionIcon(Icons.Outlined.FormatQuote, "Blockquote", { insert("\n> ") })
                        ActionIcon(Icons.Outlined.Info, "Callout", { insert("\n> [!NOTE]\n> ") })
                        ActionIcon(Icons.Outlined.Link, "Link a story note", { sheet = "links" })
                        if (e.kind == "Screenplay")
                            TextButton(onClick = { insert("\n\nINT. LOCATION - DAY\n\n") }) {
                                Text("Scene")
                            }
                    }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${number(words)} words · ${number(e.body.length)} chars",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${maxOf(1,(words+219)/220)} min read",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (focus) {
                        Spacer(Modifier.width(8.dp))
                        ActionIcon(Icons.Outlined.MenuBook, "Read", onRead)
                    }
                }
            }
        }
    }
    if (sheet.isNotEmpty())
        ModalBottomSheet(onDismissRequest = { sheet = "" }) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text(
                    when (sheet) {
                        "history" -> "Earlier versions"
                        "guidance" -> "Find your next sentence"
                        "links" -> "Connect your story"
                        else -> "Your story, close at hand"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(Modifier.height(16.dp))
                if (sheet == "guidance") {
                    listOf(
                            "What does this character want right now?",
                            "What stands in their way?",
                            "What can the reader see, hear, or feel?",
                            "What changes before this scene ends?",
                        )
                        .forEachIndexed { i, p ->
                            Text(
                                "0${i+1}   $p",
                                Modifier.padding(vertical = 12.dp),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    Text(
                        "These prompts work offline. Take what helps; leave what doesn't.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (sheet == "history") {
                    Text(
                        "Up to 20 manual checkpoints per page. Restoring creates an editable draft.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    LazyColumn(Modifier.heightIn(max = 400.dp)) {
                        if (refs.revisions.isEmpty())
                            item {
                                EmptyState(
                                    "No checkpoints yet",
                                    "Save a checkpoint before a major revision.",
                                )
                            }
                        items(refs.revisions, key = { it.id }) { r ->
                            ListItem(
                                headlineContent = { Text(r.title) },
                                supportingContent = {
                                    Text(
                                        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                                            .format(Date(r.created))
                                    )
                                },
                                trailingContent = {
                                    TextButton(
                                        onClick = {
                                            vm.restoreRevision(r)
                                            sheet = ""
                                        }
                                    ) {
                                        Text("Restore")
                                    }
                                },
                            )
                        }
                    }
                } else
                    LazyColumn(Modifier.heightIn(max = 440.dp)) {
                        if (sheet == "references") {
                            item { Eyebrow("LINKS TO THIS PAGE") }
                            if (refs.backlinks.isEmpty())
                                item {
                                    Text(
                                        "No incoming links yet.",
                                        Modifier.padding(vertical = 12.dp),
                                    )
                                }
                            items(refs.backlinks, key = { "back-${it.id}" }) { note ->
                                EntryRow(
                                    note,
                                    {
                                        onOpen(note.id)
                                        sheet = ""
                                    },
                                )
                            }
                            item {
                                Spacer(Modifier.height(16.dp))
                                Eyebrow("STORY PAGES")
                            }
                        }
                        items(
                            entries.filter { it.id != e.id && it.kind != "Folder" },
                            key = { it.id },
                        ) { note ->
                            EntryRow(
                                note,
                                onClick = {
                                    if (sheet == "links") insert("[[${note.id}|${note.title}]]")
                                    else onOpen(note.id)
                                    sheet = ""
                                },
                            )
                        }
                        if (entries.size <= 1)
                            item {
                                Text(
                                    "Add characters and notes from the Story bible to connect them here."
                                )
                            }
                    }
            }
        }
    if (confirmDelete)
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Move this page to trash?") },
            text = {
                Text("You can restore it from Settings. Your other pages will stay in place.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    }
                ) {
                    Text("Move to trash")
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
}
