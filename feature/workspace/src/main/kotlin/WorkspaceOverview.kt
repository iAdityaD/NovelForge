package com.novelforge.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.*
import com.novelforge.presentation.WorkspaceViewModel

@Composable
internal fun WorkspaceOverview(vm: WorkspaceViewModel, book: Book, entries: List<EntrySummary>, onEdit: () -> Unit, onSynopsis: () -> Unit, onWrite: () -> Unit, onOpen: (Long) -> Unit, modifier: Modifier) {
    val scroll = rememberLazyListState()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { vm.setCover(book.id, it.toString()) } }
    val manuscript = remember(entries) { orderedManuscript(entries) }
    val words = manuscript.sumOf { it.words }
    var coverMenu by remember { mutableStateOf(false) }
    var summaryExpanded by remember(book.id) { mutableStateOf(false) }
    LazyColumn(modifier.fillMaxWidth().testTag("workspace-content").scrollIndicator(scroll), state = scroll, contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    BookCover(book, Modifier.width(156.dp).height(218.dp))
                    FilledIconButton(onClick = { coverMenu = true }, modifier = Modifier.align(Alignment.BottomEnd).offset(12.dp, 10.dp)) { Icon(Icons.Outlined.AddPhotoAlternate, "Edit cover") }
                    DropdownMenu(coverMenu, { coverMenu = false }) {
                        DropdownMenuItem(text = { Text("Choose cover image") }, onClick = { coverMenu = false; launcher.launch("image/*") })
                        if (book.coverImage.isNotEmpty()) DropdownMenuItem(text = { Text("Remove cover image") }, onClick = { coverMenu = false; vm.setCover(book.id, null) })
                    }
                }
                Spacer(Modifier.height(28.dp))
                Text(book.title, style = MaterialTheme.typography.headlineMedium)
                if (book.author.isNotBlank()) Text(book.author, Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                FilledTonalButton(onClick = onWrite, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Icon(Icons.Outlined.EditNote, null); Spacer(Modifier.width(8.dp)); Text("Open writing workspace") }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Novel details", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                ActionIcon(Icons.Outlined.Edit, "Edit novel details", onEdit)
            }
            OverviewDetail("Title", book.title, onEdit)
            OverviewDetail("Author", book.author.ifBlank { "Add author" }, onEdit)
            OverviewDetail("Genres", book.genre.ifBlank { "Add genres" }, onEdit)
            OverviewDetail("Status", book.status, onEdit)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Synopsis", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                ActionIcon(Icons.Outlined.Edit, "Edit synopsis", onSynopsis)
            }
            Text(book.synopsis.ifBlank { "Add a short summary" }, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = if (summaryExpanded) Int.MAX_VALUE else 6, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            if (book.synopsis.isNotBlank()) TextButton(onClick = { summaryExpanded = !summaryExpanded }) { Text(if (summaryExpanded) "Show less" else "Show full synopsis") }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Manuscript", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                ActionIcon(Icons.Outlined.Edit, "Edit writing goal", onEdit)
            }
            Text("${number(words)} words · ${manuscript.size} documents", Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.bodyLarge)
            if (book.target > 0) {
                LinearProgressIndicator(progress = { (words.toFloat() / book.target).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Text("${number(book.target)} word goal", Modifier.padding(top = 10.dp), style = MaterialTheme.typography.labelMedium)
            } else TextButton(onClick = onEdit) { Text("Set a writing goal") }
        }
        book.overviewId?.takeIf { id -> entries.any { it.id == id } }?.let { id -> item { TextButton(onClick = { onOpen(id) }) { Text("Open project notes") } } }
    }
}

@Composable
private fun OverviewDetail(label: String, value: String, edit: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = edit).padding(vertical = 14.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyLarge)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
}
