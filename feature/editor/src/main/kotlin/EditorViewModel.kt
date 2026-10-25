package com.novelforge.presentation

import com.novelforge.core.model.*
import com.novelforge.core.presentation.FeatureViewModel
import com.novelforge.domain.preferences.*
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*
import com.novelforge.domain.writing.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class ReferencesState(
    val revisions: List<Revision> = emptyList(),
    val backlinks: List<EntrySummary> = emptyList(),
)

@HiltViewModel
class EditorViewModel
@Inject
constructor(
    private val session: EditorSession,
    private val repository: WritingRepository,
    private val story: StoryRepository,
    private val workspace: WorkspaceRepository,
    private val preferences: PreferencesRepository,
) : FeatureViewModel() {
    val state = session.state
    private val refs = MutableStateFlow(ReferencesState())
    val references = refs.asStateFlow()

    fun openEntry(id: Long, done: () -> Unit) = launchWork {
        if (session.open(id)) {
            refs.value = ReferencesState()
            state.value.entry?.let(preferences::rememberEntry)
            done()
        }
    }

    fun resume(bookId: String, done: (Boolean) -> Unit) = launchWork {
        val current = state.value.entry
        if (current?.bookId == bookId && !current.deleted && (current.id == 0L || repository.entry(current.id)?.deleted == false)) { done(true); return@launchWork }
        val draft = repository.untitled(bookId)
        val id = preferences.lastEntry(bookId)
        val saved = repository.entry(id)
        val opened = if (saved?.bookId == bookId && !saved.deleted) session.open(id)
            else if (draft != null) session.openUntitled(bookId) else false
        done(opened)
    }

    fun newDraft(bookId: String, done: () -> Unit) = launchWork {
        if (session.openUntitled(bookId)) {
            state.value.entry?.let(preferences::rememberEntry)
            done()
        }
    }
    private var committingDraft = false
    fun saveDraft(title: String, folderId: Long?, newFolder: String?, done: () -> Unit) = launchWork {
        if (committingDraft) return@launchWork
        committingDraft = true
        try {
        val draft = state.value.entry
        val position = draft?.let { preferences.editingPosition(it.bookId, it.id) } ?: (0 to 0)
        if (session.saveUntitled(title, folderId, newFolder)) {
            state.value.entry?.let { preferences.rememberEntry(it); preferences.editingPosition(it.bookId, it.id, position.first, position.second); preferences.editingPosition(it.bookId, 0, 0, 0) }
            done()
        }
        } finally { committingDraft = false }
    }
    fun discardDraft(done: () -> Unit) = launchWork { val bookId = state.value.entry?.bookId; session.discardUntitled(); bookId?.let { preferences.editingPosition(it, 0, 0, 0) }; done() }
    fun position(entry: Entry) = preferences.editingPosition(entry.bookId, entry.id)
    fun position(entry: Entry, cursor: Int, scroll: Int) = preferences.editingPosition(entry.bookId, entry.id, cursor, scroll)

    fun updateBody(text: String) = session.body(text)

    fun updateTitle(text: String) = session.title(text)

    fun setStatus(value: String) = session.status(value)

    fun metadata(kind: String, status: String) = launchWork { session.metadata(kind, status) }

    fun fields(values: Map<String, List<String>>) = session.fields(values)

    fun undo() = session.undo()

    fun redo() = session.redo()

    fun flushBackground() = session.flushBackground()

    fun leave(done: () -> Unit) = launchWork { if (session.flush()) done() }

    fun checkpoint() = launchWork {
        if (session.flush())
            state.value.entry?.let {
                repository.checkpoint(it)
                refs.update { r -> r.copy(revisions = repository.revisions(it.id)) }
            }
    }

    fun loadRevisions() = launchWork {
        state.value.entry?.let { e ->
            refs.update { it.copy(revisions = repository.revisions(e.id)) }
        }
    }

    fun restoreRevision(revision: Revision) {
        updateBody(revision.body)
        updateTitle(revision.title)
    }

    fun loadConnections(bookId: String) = launchWork {
        if (session.flush()) {
            val links = story.connections(bookId)
            val id = state.value.entry?.id
            val entries = workspace.summaries.first()
            refs.update {
                it.copy(
                    backlinks =
                        entries.filter { e -> links.any { it.first == e.id && it.second == id } }
                )
            }
        }
    }

    fun clearSessionError() = session.clearError()
}
