package com.novelforge.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelforge.core.model.Book
import com.novelforge.presentation.LibraryViewModel

@Composable
fun SearchScreen(vm: LibraryViewModel, books: List<Book>, onOpen: (Long) -> Unit) {
    val ui by vm.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(query) { vm.search(query) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Eyebrow("A THREAD THROUGH EVERY STORY")
            Spacer(Modifier.height(8.dp))
            Text("Find a thought.", style = MaterialTheme.typography.headlineLarge)
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search chapters, characters, notes…") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty())
                        ActionIcon(Icons.Outlined.Close, "Clear search", { query = "" })
                },
                singleLine = true,
            )
        }
        if (query.isBlank())
            item {
                EmptyState(
                    "Nothing gets lost here",
                    "Search the words inside every chapter and note, across all your novels. Everything stays on this device.",
                    Icons.Outlined.TravelExplore,
                )
            }
        else if (ui.results.isEmpty())
            item {
                EmptyState(
                    "No matching pages",
                    "Try another name, place, or phrase.",
                    Icons.Outlined.SearchOff,
                )
            }
        else {
            item { Eyebrow("${ui.results.size} MATCHING PAGES") }
            items(ui.results, key = { it.id }) { e ->
                Column {
                    Text(
                        books.find { it.id == e.bookId }?.title?.replace('\n', ' ') ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    EntryRow(e, onClick = { onOpen(e.id) })
                }
            }
        }
    }
}
