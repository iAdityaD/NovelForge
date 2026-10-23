package com.novelforge.presentation

import androidx.lifecycle.viewModelScope
import com.novelforge.core.model.*
import com.novelforge.core.presentation.FeatureViewModel
import com.novelforge.domain.story.*
import com.novelforge.domain.manuscript.*
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
    val placements: List<ManuscriptPlacement> = emptyList(),
    val connections: List<Pair<Long, Long>> = emptyList(),
)

@HiltViewModel
class WorkspaceViewModel
@Inject
constructor(
    private val repository: WorkspaceRepository,
    private val story: StoryRepository,
    private val session: EditorSession,
    private val manuscript: ManuscriptRepository,
) : FeatureViewModel() {
    private val links = MutableStateFlow<List<Pair<Long, Long>>>(emptyList())
    val state =
        combine(repository.books, repository.summaries, repository.trash, links, manuscript.placements) { b, e, t, l, p ->
                WorkspaceUiState(b, e, t, p, l)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkspaceUiState())

    fun createBook(
        title: String,
        author: String,
        genres: List<String>,
        cover: Int,
        target: Int,
        standard: Boolean,
        done: (CreatedWorkspace) -> Unit,
    ) = launchWork {
        done(repository.createBook(title, author, genres, cover, target, standard))
    }

    fun deleteBook(bookId: String, done: () -> Unit) = launchWork {
        if (session.closeWorkspace(bookId)) {
            repository.deleteBook(bookId)
            done()
        }
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

    fun moveChapter(id: Long, delta: Int) = mutate { manuscript.move(id, delta) }

    fun includeChapter(id: Long, included: Boolean) = mutate { manuscript.include(id, included) }

    fun moveEntry(id: Long, delta: Int) = mutate { repository.moveEntry(id, delta) }

    fun setBookStatus(book: Book, status: String) = launchWork {
        repository.setBookStatus(book, status)
    }
}
