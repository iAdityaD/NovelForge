package com.novelforge.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novelforge.app.data.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val lessons =
    listOf(
        Triple(
            "01",
            "Your workspace, explained",
            "A novel is a workspace. Choose your starter folders when you create it, or begin with an empty workspace.\n\nUse the file explorer to add Chapters, Characters, Lore, Plot and Metadata. These are starting points, not rules: add a folder for each world, kingdom, volume or research topic.\n\nTap a folder to expand it and make it the destination for new files. Use ⋯ to rename, move or trash an item. Hold and drag onto a folder to move it. A file’s type stays the same when you move it, so analytics remain accurate.",
        ),
        Triple(
            "02",
            "Link characters & worlds",
            "Create a character file first. Open your chapter and place the cursor where you want a reference. Tap the link icon on the editor toolbar, then choose the character.\n\nNovelForge inserts a stable wiki link such as [[42|Mara]]. The number identifies the file; Mara is its display label. Renaming the file keeps the connection intact, although the label in old links remains as written.\n\nIn reading mode, tap the link to open the character. Workspace → Graph shows connections. Editor → ⋯ → Story references shows incoming backlinks. Links stay within the workspace.\n\nUse the picker instead of inventing a numeric ID. A trashed target can be restored in Settings.",
        ),
        Triple(
            "03",
            "From idea to first draft",
            "Start with a promise: who wants what, and why can’t they have it? Capture it in Metadata.\n\nIn Plot, outline the opening, escalation, turning point, climax and consequence. Give each scene a goal, an obstacle and a change.\n\nIn Characters, record desire, fear, flaw and relationships. In Lore, define only the rules that affect the story. For multiple worlds, add nested folders under Lore and link shared concepts rather than duplicating them.\n\nDraft a chapter without polishing every sentence. Use status to distinguish Draft, In progress, Revised and Final. A workspace is completed only when you choose Mark Completed in its menu.",
        ),
        Triple(
            "04",
            "Write, preview, revise",
            "Write Markdown in the editor: ## Heading, **bold**, *italic*, > quote and - bullet. The toolbar wraps selected text or inserts syntax at the cursor. Callouts use > [!NOTE].\n\nChoose proportional or monospace text and syntax colors in Settings → Editor. Focus mode hides the formatting toolbar. Reading mode previews the text; use the fullscreen control to hide navigation.\n\nSave a checkpoint before major revisions. Up to 20 manual checkpoints are kept for each file. Undo is temporary; checkpoints survive closing the app.\n\nRead for structure first, then clarity and rhythm. Pages in Analytics are estimates of 250 manuscript words, not printed pagination.",
        ),
        Triple(
            "05",
            "Keep your stories safe",
            "Edits save locally after a short pause and before navigation. Wait for Saved locally before closing; a sudden process kill can lose the most recent unsaved fragment.\n\nIn Settings → Data & backup, choose a workspace and export a portable backup with Android’s document picker. Restore creates a separate copy, including folder structure and wiki links.\n\nBackups contain active files and workspace metadata. Trash, checkpoints, activity history and reading preferences remain on this device.\n\nThere is no automatic cloud sync. You can choose a cloud provider in Android’s file picker. Export before uninstalling or replacing development builds.",
        ),
    )

@Composable
fun GuideScreen() {
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    BackHandler(selected != null) { selected = null }
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (selected == null) {
            item { ScreenHeading("Guide", "Make the app part of your writing process.") }
            item {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Start small. Connect as you go.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "A chapter, a character, a world. Everything else grows from there.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(lessons.size) { i ->
                val lesson = lessons[i]
                SurfaceCard(Modifier.fillMaxWidth().clickable { selected = i }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            lesson.first,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text(
                            lesson.second,
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Icon(Icons.Outlined.ChevronRight, null)
                    }
                }
            }
            item {
                Text(
                    "Offline guidance · No account or AI service required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val lesson = lessons[selected!!]
            item {
                TextButton(onClick = { selected = null }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                    Spacer(Modifier.width(8.dp))
                    Text("All guides")
                }
            }
            item {
                Eyebrow("GUIDE ${lesson.first}")
                Spacer(Modifier.height(12.dp))
                Text(
                    lesson.second,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(lesson.third.split("\n\n")) {
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun AnalyticsScreen(books: List<Book>, entries: List<EntrySummary>, days: List<WritingDay>) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var dropdown by remember { mutableStateOf(false) }
    val book = books.find { it.id == selected }
    val chosen = if (book == null) entries else entries.filter { it.bookId == book.id }
    val activity = if (book == null) days else days.filter { it.bookId == book.id }
    val chapters = chosen.filter { it.kind == "Chapter" }
    val words = chapters.sumOf { it.words }
    val today = LocalDate.now()
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item { ScreenHeading("Analytics", "A little progress, every day.") }
        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocalFireDepartment,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "${writingStreak(activity,today)} day streak",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${if(book==null)"Across all workspaces" else book.title.replace('\n',' ')}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (offset in 6 downTo 0) {
                        val date = today.minusDays(offset.toLong())
                        val active = activity.any { it.day == date.toString() && it.edits > 0 }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                Modifier.size(32.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(
                                        if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (active)
                                    Icon(
                                        Icons.Outlined.Check,
                                        null,
                                        Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                else Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                date.dayOfWeek.getDisplayName(
                                    TextStyle.NARROW,
                                    Locale.getDefault(),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "A day counts when you save a text edit. Tracking starts with this version.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Box {
                OutlinedButton(
                    onClick = { dropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Outlined.FolderOpen, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        book?.title?.replace('\n', ' ') ?: "All workspaces",
                        Modifier.weight(1f),
                        maxLines = 1,
                    )
                    Icon(Icons.Outlined.ExpandMore, null)
                }
                DropdownMenu(dropdown, { dropdown = false }) {
                    DropdownMenuItem(
                        text = { Text("All workspaces") },
                        onClick = {
                            selected = null
                            dropdown = false
                        },
                    )
                    books.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b.title.replace('\n', ' ')) },
                            onClick = {
                                selected = b.id
                                dropdown = false
                            },
                        )
                    }
                }
            }
        }
        if (book == null) {
            item {
                Text(
                    "Your writing portfolio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                MetricRow(
                    listOf(
                        "Workspaces" to books.size.toString(),
                        "In progress" to books.count { it.status == "In progress" }.toString(),
                    )
                )
            }
            item {
                MetricRow(
                    listOf(
                        "Completed" to books.count { it.status == "Completed" }.toString(),
                        "On hold" to books.count { it.status == "On hold" }.toString(),
                    )
                )
            }
        } else
            item {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Eyebrow("MANUSCRIPT GOAL")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${((words.toFloat()/book.target)*100).toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "${number(words)} of ${number(book.target)} words",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { (words.toFloat() / book.target).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        book.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        item {
            MetricRow(
                listOf(
                    "Manuscript words" to number(words),
                    "Estimated pages" to number((words + 249) / 250),
                )
            )
        }
        item {
            MetricRow(
                listOf(
                    "Chapters" to chapters.size.toString(),
                    "Character profiles" to chosen.count { it.kind == "Character" }.toString(),
                )
            )
        }
        if (book != null) {
            item {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Text("Chapter pipeline", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    statuses.forEach { status ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text(
                                status,
                                Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                chapters.count { it.status == status }.toString(),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
            item {
                MetricRow(
                    listOf(
                        "Lore files" to
                            chosen
                                .count {
                                    it.kind in listOf("Worldbuilding", "Location", "Research")
                                }
                                .toString(),
                        "All files" to chosen.count { it.kind != "Folder" }.toString(),
                    )
                )
            }
        }
        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Text("Words added · last 7 days", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                val values =
                    (6 downTo 0).map { offset ->
                        activity
                            .filter { it.day == today.minusDays(offset.toLong()).toString() }
                            .sumOf { it.words }
                    }
                val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
                Row(
                    Modifier.fillMaxWidth().height(110.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    values.forEach { n ->
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(number(n), style = MaterialTheme.typography.labelSmall)
                            Box(
                                Modifier.fillMaxWidth()
                                    .height((6 + 70f * n / max).dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = if (n == 0) .15f else .85f
                                        )
                                    )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Positive net word changes between saves. Page estimates use 250 manuscript words per page.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetricRow(metrics: List<Pair<String, String>>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        metrics.forEach { (label, value) ->
            SurfaceCard(Modifier.weight(1f)) {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
