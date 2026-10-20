package com.novelforge.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelforge.app.ForgeViewModel
import com.novelforge.app.data.*
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    vm: ForgeViewModel,
    e: Entry,
    book: Book?,
    entries: List<EntrySummary>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    var appearance by remember { mutableStateOf(false) }
    var contents by remember { mutableStateOf(false) }
    var bookmarked by
        remember(e.id) { mutableStateOf(vm.prefs.getBoolean("bookmark-${e.id}", false)) }
    var controls by rememberSaveable { mutableStateOf(true) }
    val bg =
        if (vm.dark) MaterialTheme.colorScheme.background
        else if (vm.readerWarm) Color(0xFFFAF4E7) else Color(0xFFFFFEFA)
    val paragraphs = remember(e.body) { e.body.split(Regex("\n\\s*\n")).filter { it.isNotBlank() } }
    val chapters = entries.filter { it.kind == "Chapter" }
    val index = chapters.indexOfFirst { it.id == e.id }
    key(e.id) {
        val state =
            rememberLazyListState(
                vm.prefs.getInt("read-${e.id}", 0).coerceIn(0, paragraphs.size + 1),
                vm.prefs.getInt("offset-${e.id}", 0),
            )
        LaunchedEffect(state) {
            snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
                .distinctUntilChanged()
                .collect { (i, offset) ->
                    vm.prefs
                        .edit()
                        .putInt("read-${e.id}", i)
                        .putInt("offset-${e.id}", offset)
                        .apply()
                }
        }
        Column(Modifier.fillMaxSize().background(bg)) {
            if (controls)
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionIcon(Icons.AutoMirrored.Outlined.ArrowBack, "Back to workspace", onBack)
                    Text(
                        "READING ROOM",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ActionIcon(
                        if (bookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        if (bookmarked) "Remove bookmark" else "Bookmark chapter",
                        {
                            bookmarked = !bookmarked
                            vm.prefs.edit().putBoolean("bookmark-${e.id}", bookmarked).apply()
                        },
                    )
                    ActionIcon(
                        Icons.Outlined.FormatListNumbered,
                        "Table of contents",
                        { contents = true },
                    )
                    ActionIcon(
                        Icons.Outlined.TextFields,
                        "Reading appearance",
                        { appearance = true },
                    )
                    ActionIcon(Icons.Outlined.Edit, "Edit this chapter", onEdit)
                }
            LazyColumn(
                state = state,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 30.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(17.dp),
                    ) {
                        Eyebrow(
                            if (index >= 0) "CHAPTER ${(index+1).toString().padStart(2,'0')}"
                            else e.kind
                        )
                        Text(
                            e.title,
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                        )
                        Text("✦", color = MaterialTheme.colorScheme.secondary)
                        Text(
                            book?.title?.replace('\n', ' ') ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (paragraphs.isEmpty())
                    item {
                        EmptyState(
                            "A story waiting to happen",
                            "Write a little, then come back to see it as a reader would.",
                            action = "Start writing",
                            onAction = onEdit,
                        )
                    }
                itemsIndexed(paragraphs) { _, text ->
                    SelectionContainer {
                        MarkdownParagraph(text, vm.fontSize, vm.readerSerif, onLink = onOpen)
                    }
                }
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text("✦  ✦  ✦", color = MaterialTheme.colorScheme.secondary)
                        Text(
                            "End of ${if(e.kind=="Chapter") "chapter" else "page"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (index >= 0 && index < chapters.lastIndex)
                            FilledTonalButton(onClick = { onOpen(chapters[index + 1].id) }) {
                                Text("Next chapter")
                                Spacer(Modifier.width(9.dp))
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowForward,
                                    null,
                                    Modifier.size(17.dp),
                                )
                            }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionIcon(
                    Icons.Outlined.ChevronLeft,
                    "Previous chapter",
                    { onOpen(chapters[index - 1].id) },
                    index > 0,
                )
                Text(
                    if (index >= 0) "${index+1} / ${chapters.size}" else e.kind,
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ActionIcon(
                    if (controls) Icons.Outlined.Fullscreen else Icons.Outlined.FullscreenExit,
                    "Toggle reading controls",
                    { controls = !controls },
                )
                ActionIcon(
                    Icons.Outlined.ChevronRight,
                    "Next chapter",
                    { onOpen(chapters[index + 1].id) },
                    index >= 0 && index < chapters.lastIndex,
                )
            }
        }
    }
    if (appearance)
        ModalBottomSheet(onDismissRequest = { appearance = false }) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("Make yourself comfortable", style = MaterialTheme.typography.headlineMedium)
                Text("Text size · ${vm.fontSize.toInt()}")
                Slider(
                    value = vm.fontSize,
                    onValueChange = { vm.setFont(it) },
                    valueRange = 14f..30f,
                    steps = 15,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Literary serif", Modifier.weight(1f))
                    Switch(checked = vm.readerSerif, onCheckedChange = { vm.setSerif(it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Warm paper", Modifier.weight(1f))
                    Switch(
                        checked = vm.readerWarm,
                        onCheckedChange = { vm.setWarm(it) },
                        enabled = !vm.dark,
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    if (contents)
        ModalBottomSheet(onDismissRequest = { contents = false }) {
            Column(Modifier.padding(24.dp)) {
                Text("Contents", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    itemsIndexed(chapters) { i, c ->
                        ListItem(
                            headlineContent = { Text(c.title) },
                            trailingContent = {
                                if (vm.prefs.getBoolean("bookmark-${c.id}", false))
                                    Icon(Icons.Outlined.Bookmark, "Bookmarked")
                            },
                            leadingContent = {
                                Text(
                                    (i + 1).toString().padStart(2, '0'),
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    contents = false
                                    onOpen(c.id)
                                },
                        )
                    }
                }
            }
        }
}

@Composable
fun MarkdownParagraph(source: String, size: Float, serif: Boolean, onLink: (Long) -> Unit) {
    val heading = source.startsWith("#")
    val text =
        source
            .replace(Regex("^#{1,6}\\s+"), "")
            .replace(Regex("(?m)^- \\[ \\] "), "☐ ")
            .replace(Regex("(?m)^- \\[x\\] "), "☑ ")
            .replace(Regex("(?m)^- "), "• ")
    val accent = MaterialTheme.colorScheme.primary
    val annotated = buildAnnotatedString {
        val regex = Regex("\\[\\[(\\d+)\\|([^]]+)]]|\\*\\*(.+?)\\*\\*|\\*([^*]+)\\*")
        var start = 0
        regex.findAll(text).forEach { m ->
            append(text.substring(start, m.range.first))
            when {
                m.groupValues[1].isNotBlank() -> {
                    pushStringAnnotation("entry", m.groupValues[1])
                    withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Medium)) {
                        append(m.groupValues[2])
                    }
                    pop()
                }
                m.groupValues[3].isNotBlank() ->
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(m.groupValues[3]) }
                else ->
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(m.groupValues[4]) }
            }
            start = m.range.last + 1
        }
        append(text.substring(start))
    }
    ClickableText(
        text = annotated,
        style =
            TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = if (serif) FontFamily.Serif else FontFamily.SansSerif,
                fontSize = (if (heading) size + 5 else size).sp,
                lineHeight = (size * 1.75f).sp,
                fontWeight = if (heading) FontWeight.Medium else FontWeight.Normal,
            ),
        onClick = { offset ->
            annotated
                .getStringAnnotations("entry", offset, offset)
                .firstOrNull()
                ?.item
                ?.toLongOrNull()
                ?.let(onLink)
        },
    )
}
