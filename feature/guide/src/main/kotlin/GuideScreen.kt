package com.novelforge.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GuideScreen() {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val selected = guideArticles.find { it.id == selectedId }
    val visible = guideArticles.filter { article ->
        query.isBlank() || listOf(article.title, article.category).plus(article.sections.flatMap { listOf(it.first, it.second) })
            .any { it.contains(query.trim(), ignoreCase = true) }
    }
    BackHandler(selected != null) { selectedId = null }
    // Independent list state prevents an article from opening at the index's scroll offset.
    key(selectedId) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (selected == null) {
                item { Text("Guide", style = MaterialTheme.typography.headlineLarge) }
                item {
                    OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search guides") }, singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Search, null) },
                        trailingIcon = { if (query.isNotEmpty()) ActionIcon(Icons.Outlined.Close, "Clear guide search", { query = "" }) })
                }
                if (visible.isEmpty()) item { Text("No guides found", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                visible.groupBy { it.category }.forEach { (category, articles) ->
                    item(key = category) { Text(category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
                    items(articles, key = { it.id }) { article ->
                        Row(Modifier.fillMaxWidth().clickable { selectedId = article.id }.heightIn(min = 56.dp).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(article.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            Icon(Icons.Outlined.ChevronRight, null)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            } else {
                item {
                    TextButton(onClick = { selectedId = null }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                        Spacer(Modifier.width(8.dp)); Text("All guides")
                    }
                }
                item { Text(selected.title, style = MaterialTheme.typography.headlineMedium) }
                items(selected.sections) { (title, body) ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
