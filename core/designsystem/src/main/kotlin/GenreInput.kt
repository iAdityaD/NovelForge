package com.novelforge.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreInput(values: List<String>, onChange: (List<String>) -> Unit) {
    var custom by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Genres", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { value ->
                InputChip(
                    selected = true,
                    onClick = { onChange(values - value) },
                    label = { Text(value) },
                    trailingIcon = {
                        Icon(Icons.Outlined.Close, "Remove genre $value", Modifier.size(16.dp))
                    },
                )
            }
            AssistChip(
                onClick = { expanded = !expanded },
                label = { Text("Add genre") },
                leadingIcon = { Icon(Icons.Outlined.Add, null, Modifier.size(18.dp)) },
            )
        }
        if (expanded) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                        "Fantasy",
                        "Romance",
                        "Historical fiction",
                        "Mystery",
                        "Science fiction",
                        "Thriller",
                        "Drama",
                    )
                    .filter { candidate -> values.none { it.equals(candidate, true) } }
                    .forEach { genre ->
                        SuggestionChip(
                            onClick = { if (values.size < 20) onChange(values + genre) },
                            label = { Text(genre) },
                        )
                    }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    custom,
                    { custom = it.take(80) },
                    label = { Text("Custom genre") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = {
                        onChange(values + custom.trim())
                        custom = ""
                    },
                    enabled =
                        custom.isNotBlank() &&
                            values.size < 20 &&
                            values.none { it.equals(custom.trim(), true) },
                ) {
                    Text("Add")
                }
            }
            Text(
                "Choose several genres or add your own.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
