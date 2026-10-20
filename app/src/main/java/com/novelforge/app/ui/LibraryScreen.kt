package com.novelforge.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelforge.app.data.*
import java.text.NumberFormat

fun number(value: Int): String = NumberFormat.getIntegerInstance().format(value)

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
) {
    var grid by rememberSaveable { mutableStateOf(true) }
    val recent = entries.filter { it.kind == "Chapter" }.maxByOrNull { it.updated }
    val recentBook = books.find { it.id == recent?.bookId }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AutoStories,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(27.dp),
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    "novelforge",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f),
                )
                ActionIcon(Icons.Outlined.Search, "Search your writing", onSearch)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Eyebrow("YOUR PERSONAL WRITING STUDIO")
                Text(
                    "A home for your\nnext great story.",
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    "Small beginnings. Extraordinary worlds.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            if (recent != null && recentBook != null) {
                SurfaceCard(Modifier.fillMaxWidth().clickable { onContinue(recent.id) }) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BookCover(recentBook, Modifier.width(80.dp).height(114.dp), compact = true)
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Eyebrow("PICK UP WHERE YOU LEFT OFF")
                            Text(
                                recent.title,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${number(recent.words)} words · ${recent.status}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Continue writing",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowForward,
                                    null,
                                    Modifier.size(17.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            } else {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Eyebrow("THE BLANK PAGE IS AN INVITATION")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Every world begins\nwith a word.",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = onNew, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Outlined.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Create your first novel")
                    }
                    TextButton(onClick = onDemo) {
                        Text("Explore a sample project")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(16.dp))
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Your bookshelf", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${books.size} ${if(books.size==1) "story" else "stories"} · stored on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ActionIcon(
                    if (grid) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                    "Change bookshelf layout",
                    { grid = !grid },
                )
                FilledIconButton(onClick = onNew, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Outlined.Add, "New novel")
                }
            }
        }
        if (grid)
            items(books.chunked(2)) { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    pair.forEach { book ->
                        Column(
                            Modifier.weight(1f).clickable { onBook(book.id) },
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            BookCover(book, Modifier.fillMaxWidth().aspectRatio(.69f))
                            Text(
                                book.title.replace('\n', ' '),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val words =
                                entries
                                    .filter { it.bookId == book.id && it.kind == "Chapter" }
                                    .sumOf { it.words }
                            Text(
                                "${number(words)} / ${number(book.target)} words",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            LinearProgressIndicator(
                                progress = { (words.toFloat() / book.target).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(3.dp),
                                trackColor = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        else
            items(books, key = { it.id }) { book ->
                SurfaceCard(Modifier.fillMaxWidth().clickable { onBook(book.id) }) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BookCover(book, Modifier.width(60.dp).height(86.dp), true)
                        Column(Modifier.weight(1f)) {
                            Text(
                                book.title.replace('\n', ' '),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                book.genre,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(Icons.Outlined.ChevronRight, null)
                    }
                }
            }
        item {
            OutlinedButton(
                onClick = onImport,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Outlined.FileDownload, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Restore a project backup")
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    null,
                    Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Your words. Your device. Your world.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun BookDialog(
    book: Book?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Int, String) -> Unit,
) {
    var title by remember { mutableStateOf(book?.title ?: "") }
    var author by remember { mutableStateOf(book?.author ?: "") }
    var genre by remember { mutableStateOf(book?.genre ?: "Fantasy") }
    var cover by remember { mutableIntStateOf(book?.cover ?: 0) }
    var target by remember { mutableStateOf((book?.target ?: 80000).toString()) }
    var synopsis by remember { mutableStateOf(book?.synopsis ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (book == null) "Begin a new story" else "Your book, at a glance",
                fontFamily = FontFamily.Serif,
            )
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    "Make a little room for a whole new world.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    title,
                    { title = it.take(200) },
                    label = { Text("Book title") },
                    singleLine = true,
                )
                OutlinedTextField(
                    author,
                    { author = it.take(120) },
                    label = { Text("Author / pen name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    genre,
                    { genre = it.take(80) },
                    label = { Text("Genre") },
                    singleLine = true,
                )
                OutlinedTextField(
                    target,
                    { target = it.filter(Char::isDigit).take(8) },
                    label = { Text("Target word count") },
                    singleLine = true,
                    isError = target.toIntOrNull()?.let { it !in 1..10000000 } ?: true,
                )
                if (book != null)
                    OutlinedTextField(
                        synopsis,
                        { synopsis = it },
                        label = { Text("Synopsis") },
                        minLines = 3,
                    )
                Eyebrow("COVER EDITION")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { i ->
                        Box(
                            Modifier.weight(1f)
                                .border(
                                    if (i == cover) 2.dp else 0.dp,
                                    if (i == cover) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                    RoundedCornerShape(10.dp),
                                )
                                .padding(3.dp)
                                .clickable { cover = i }
                        ) {
                            BookCover(
                                Book(
                                    title = title.ifBlank { "Your story" },
                                    author = author,
                                    cover = i,
                                ),
                                Modifier.fillMaxWidth().height(80.dp),
                                true,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title.trim(),
                        author.trim(),
                        genre.trim(),
                        cover,
                        target.toInt(),
                        synopsis,
                    )
                },
                enabled =
                    title.isNotBlank() && target.toIntOrNull()?.let { it in 1..10000000 } == true,
            ) {
                Text(if (book == null) "Create novel" else "Save changes")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun EntryDialog(initialKind: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(initialKind) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("A new piece of your story", fontFamily = FontFamily.Serif) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    kinds.forEach {
                        FilterChip(
                            selected = kind == it,
                            onClick = { kind = it },
                            label = { Text(it) },
                        )
                    }
                }
                OutlinedTextField(
                    title,
                    { title = it.take(200) },
                    label = { Text("$kind title") },
                    singleLine = true,
                )
                Text(
                    if (kind == "Chapter")
                        "Start with a blank page. You can assign an arc from the manuscript outline."
                    else "A thoughtful template will help you get started.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title.trim(), kind) }, enabled = title.isNotBlank()) {
                Text("Create ${kind.lowercase()}")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
