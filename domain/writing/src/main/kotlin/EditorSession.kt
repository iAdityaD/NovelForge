package com.novelforge.domain.writing

import com.novelforge.core.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class EditorState(
    val entry: Entry? = null,
    val saveStatus: String = "Saved locally",
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val error: String? = null,
)

/** One editing session shared by explorer mutations and the editor. No Android dependency. */
class EditorSession(private val repository: WritingRepository, private val scope: CoroutineScope) {
    private val mutable = MutableStateFlow(EditorState())
    val state = mutable.asStateFlow()
    private var persisted: Entry? = null
    private var saveJob: Job? = null
    private val mutex = Mutex()
    private val undo = ArrayDeque<String>()
    private val redo = ArrayDeque<String>()
    private var undoTime = 0L

    suspend fun open(id: Long): Boolean {
        if (!flush()) return false
        val entry = repository.entry(id)
        if (entry == null || entry.deleted) {
            mutable.update { it.copy(error = "This page is unavailable. Check Recently deleted.") }
            return false
        }
        persisted = entry
        undo.clear()
        redo.clear()
        mutable.value = EditorState(entry)
        return true
    }

    fun body(text: String) {
        val entry = state.value.entry ?: return
        if (entry.body == text) return
        val now = System.currentTimeMillis()
        if (now - undoTime > 700 || undo.isEmpty()) {
            undo.addLast(entry.body)
            if (undo.size > 20) undo.removeFirst()
        }
        undoTime = now
        redo.clear()
        change(entry.copy(body = text))
    }

    fun title(text: String) {
        state.value.entry?.let { change(it.copy(title = text)) }
    }

    fun status(value: String) {
        state.value.entry?.let { change(it.copy(status = value)) }
    }

    fun metadata(kind: String, status: String) {
        require(kind.trim().isNotEmpty() && kind.length <= 80 && kind.trim() != "Folder") { "Use a type label of 1–80 characters." }
        require(status.trim().isNotEmpty() && status.length <= 80) { "Use a status of 1–80 characters." }
        state.value.entry?.let { change(it.copy(kind = kind.trim(), status = status.trim())) }
    }

    fun undo() {
        val e = state.value.entry ?: return
        if (undo.isEmpty()) return
        redo.addLast(e.body)
        change(e.copy(body = undo.removeLast()))
    }

    fun redo() {
        val e = state.value.entry ?: return
        if (redo.isEmpty()) return
        undo.addLast(e.body)
        change(e.copy(body = redo.removeLast()))
    }

    private fun change(entry: Entry) {
        mutable.update {
            it.copy(
                entry = entry,
                saveStatus = "Saving…",
                canUndo = undo.isNotEmpty(),
                canRedo = redo.isNotEmpty(),
            )
        }
        saveJob?.cancel()
        saveJob =
            scope.launch {
                delay(400)
                persist()
            }
    }

    private suspend fun persist(): Boolean =
        mutex.withLock {
            val snapshot = state.value.entry ?: return@withLock true
            if (snapshot == persisted) {
                mutable.update { it.copy(saveStatus = "Saved locally") }
                return@withLock true
            }
            try {
                repository.save(snapshot, persisted)
                persisted = snapshot
                mutable.update {
                    if (it.entry == snapshot) it.copy(saveStatus = "Saved locally") else it
                }
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutable.update {
                    it.copy(
                        saveStatus = "Save failed · tap to retry",
                        error = "Your latest edit could not be saved. Retry before leaving.",
                    )
                }
                false
            }
        }

    suspend fun flush(): Boolean {
        saveJob?.cancel()
        return persist()
    }

    fun flushBackground() {
        saveJob?.cancel()
        saveJob = scope.launch { persist() }
    }

    suspend fun reload() {
        val id = state.value.entry?.id ?: return
        val e = repository.entry(id)
        persisted = e
        mutable.update { it.copy(entry = e) }
    }

    suspend fun closeWorkspace(bookId: String): Boolean {
        if (state.value.entry?.bookId != bookId) return true
        if (!flush()) return false
        mutex.withLock {
            persisted = null
            undo.clear()
            redo.clear()
            mutable.value = EditorState()
        }
        return true
    }

    fun clearError() {
        mutable.update { it.copy(error = null) }
    }
}
