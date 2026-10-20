package com.novelforge.presentation

import androidx.lifecycle.viewModelScope
import com.novelforge.core.model.*
import com.novelforge.core.presentation.FeatureViewModel
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*
import com.novelforge.domain.writing.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class WorkspaceUiState(
    val books: List<Book> = emptyList(),
    val entries: List<EntrySummary> = emptyList(),
    val trash: List<Entry> = emptyList(),
    val connections: List<Pair<Long, Long>> = emptyList(),
)

@HiltViewModel
class WorkspaceViewModel
@Inject
constructor(
    private val repository: WorkspaceRepository,
    private val story: StoryRepository,
    private val session: EditorSession,
) : FeatureViewModel() {
    private val links = MutableStateFlow<List<Pair<Long, Long>>>(emptyList())
    val state =
        combine(repository.books, repository.summaries, repository.trash, links) { b, e, t, l ->
                WorkspaceUiState(b, e, t, l)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkspaceUiState())

    fun createBook(
        title: String,
        author: String,
        genres: List<String>,
        cover: Int,
        target: Int,
        synopsis: String,
        folders: Set<String>,
        done: (String) -> Unit,
    ) = launchWork {
        done(repository.createBook(title, author, genres, cover, target, synopsis, folders))
    }

    fun editBook(book: Book, done: () -> Unit = {}) = launchWork {
        repository.editBook(book)
        done()
    }

    fun createEntry(
        bookId: String,
        title: String,
        kind: String,
        parentId: Long?,
        folderId: Long?,
        done: (Long) -> Unit,
    ) = launchWork { done(repository.createEntry(bookId, title, kind, parentId, folderId)) }

    fun loadDemo(done: (String) -> Unit) = launchWork { done(repository.loadDemo()) }

    fun loadConnections(id: String) = launchWork {
        if (session.flush()) links.value = story.connections(id)
    }

    private fun mutate(block: suspend () -> Unit) = launchWork {
        if (session.flush()) {
            block()
            session.reload()
        }
    }

    fun createFolder(bookId: String, title: String, parent: Long?) = mutate {
        repository.createFolder(bookId, title, parent)
    }

    fun renameEntry(id: Long, title: String) = mutate { repository.renameEntry(id, title) }

    fun relocate(id: Long, destination: Long?) = mutate { repository.relocate(id, destination) }

    fun deleteEntry(id: Long, done: () -> Unit = {}) = mutate {
        repository.deleteEntry(id)
        done()
    }

    fun restoreEntry(id: Long) = mutate { repository.restoreEntry(id) }

    fun moveEntry(id: Long, delta: Int) = mutate { repository.moveEntry(id, delta) }

    fun setBookStatus(book: Book, status: String) = launchWork {
        repository.setBookStatus(book, status)
    }
}
