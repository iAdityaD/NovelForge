package com.novelforge.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.novelforge.app.data.*
import java.text.NumberFormat

fun number(value: Int): String = NumberFormat.getIntegerInstance().format(value)

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
