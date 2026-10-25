package com.novelforge.presentation

import androidx.lifecycle.viewModelScope
import com.novelforge.core.model.*
import com.novelforge.core.presentation.FeatureViewModel
import com.novelforge.domain.preferences.*
import com.novelforge.domain.workspace.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    private val preferences: PreferencesRepository,
    private val workspace: WorkspaceRepository,
) : FeatureViewModel() {
    val state = preferences.state
    val trash =
        workspace.trash.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateDark(value: Boolean) = preferences.update(state.value.copy(dark = value))

    fun updateEditorMono(value: Boolean) = preferences.update(state.value.copy(editorMono = value))

    fun updateSyntaxColors(value: Boolean) =
        preferences.update(state.value.copy(syntaxColors = value))

    fun setFont(value: Float) = preferences.update(state.value.copy(fontSize = value))

    fun setReaderFont(value: Float) = preferences.update(state.value.copy(readerFontSize = value))

    fun setSerif(value: Boolean) = preferences.update(state.value.copy(readerSerif = value))

    fun setWarm(value: Boolean) = preferences.update(state.value.copy(readerWarm = value))

    fun setSynopsisLimit(value: Int) = launchWork {
        preferences.update(state.value.copy(synopsisLimit = value))
    }

    fun setStarterFolders(folders: List<StarterFolder>) = launchWork {
        preferences.update(state.value.copy(starterFolders = validateStarterFolders(folders)))
    }

    fun saveTemplate(bookId: String, name: String, includeDocuments: Boolean, done: () -> Unit) = launchWork {
        val entries = workspace.summaries.first().filter { it.bookId == bookId }
        require(workspace.books.first().any { it.id == bookId }) { "Project is unavailable." }
        val template = templateFromStructure("custom-" + java.util.UUID.randomUUID(), name, entries, includeDocuments).copy(fields = workspace.books.first().first { it.id == bookId }.fields)
        preferences.update(state.value.copy(savedTemplates = state.value.savedTemplates + template))
        done()
    }

    fun saveFields(bookId: String, fields: List<FieldDefinition>, done: () -> Unit) = launchWork {
        val book = workspace.books.first().first { it.id == bookId }
        validateFields(fields)
        workspace.editBook(book.copy(fields = fields))
        done()
    }

    fun removeTemplate(id: String) = launchWork {
        preferences.update(state.value.copy(savedTemplates = state.value.savedTemplates.filterNot { it.id == id }))
    }

    fun restoreEntry(id: Long) = launchWork { workspace.restoreEntry(id) }
}
