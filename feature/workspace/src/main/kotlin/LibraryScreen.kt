package com.novelforge.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.*
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*

@Composable
fun NovelDetailsScreen(book: Book, onDismiss: () -> Unit, onSave: (Book) -> Unit) {
    var title by rememberSaveable(book.id) { mutableStateOf(book.title) }
    var author by rememberSaveable(book.id) { mutableStateOf(book.author) }
    var genres by rememberSaveable(book.id) { mutableStateOf(book.genres) }
    var cover by rememberSaveable(book.id) { mutableIntStateOf(book.cover) }
    var target by rememberSaveable(book.id) { mutableStateOf(book.target.toString()) }
    var status by rememberSaveable(book.id) { mutableStateOf(book.status) }
    var discard by remember { mutableStateOf(false) }
    val dirty =
        title != book.title ||
            author != book.author ||
            genres != book.genres ||
            cover != book.cover ||
            target != book.target.toString() ||
            status != book.status
    fun close() {
        if (dirty) discard = true else onDismiss()
    }
    Box(Modifier.fillMaxSize()) {
        BackHandler { close() }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionIcon(Icons.Outlined.Close, "Close novel details", { close() })
                    Text(
                        "Novel details",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                }
                LazyColumn(
                    Modifier.weight(1f).testTag("novel-details-form"),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        OutlinedTextField(
                            title,
                            { title = it.take(200) },
                            label = { Text("Novel title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item {
                        OutlinedTextField(
                            author,
                            { author = it.take(120) },
                            label = { Text("Author") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item { GenreInput(genres, { genres = it }) }
                    item {
                        OutlinedTextField(
                            target,
                            { target = it.filter(Char::isDigit).take(8) },
                            label = { Text("Target word count") },
                            singleLine = true,
                            isError = target.toIntOrNull()?.let { it !in 1..10000000 } ?: true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item { ChoiceField("Writing status", status, bookStatuses) { status = it } }
                    item {
                        Text("Workspace color", style = MaterialTheme.typography.titleMedium)
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("Iris", "Ocean", "Ember", "Moss").forEachIndexed { i, name ->
                                FilterChip(
                                    selected = cover == i,
                                    onClick = { cover = i },
                                    label = { Text(name) },
                                )
                            }
                        }
                    }
                }
                HorizontalDivider()
                Button(
                    onClick = {
                        onSave(
                            book.copy(
                                title = title.trim(),
                                author = author.trim(),
                                genres = genres,
                                cover = cover,
                                target = target.toInt(),
                                status = status,
                            )
                        )
                    },
                    enabled =
                        title.isNotBlank() &&
                            target.toIntOrNull()?.let { it in 1..10000000 } == true,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                ) {
                    Text("Save changes")
                }
            }
        }
        if (discard)
            AlertDialog(
                onDismissRequest = { discard = false },
                title = { Text("Discard unsaved changes?") },
                text = { Text("Your saved novel details will be kept.") },
                confirmButton = { TextButton(onClick = onDismiss) { Text("Discard") } },
                dismissButton = {
                    TextButton(onClick = { discard = false }) { Text("Keep editing") }
                },
            )
    }
}

@Composable
fun EntryDialog(initialKind: String, onDismiss: () -> Unit, manuscriptOnly: Boolean = false, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(initialKind) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("A new piece of your story", fontFamily = FontFamily.SansSerif) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (if (manuscriptOnly) listOf("Document", "Chapter", "Screenplay") else kinds).forEach {
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
                        "A lightweight chapter template is ready. Planning and revision notes are optional."
                    else "Every template field is optional. Start with the details that help you write.",
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
