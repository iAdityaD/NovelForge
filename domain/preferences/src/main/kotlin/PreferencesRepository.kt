package com.novelforge.domain.preferences

import com.novelforge.core.model.*
import kotlinx.coroutines.flow.*

data class AppPreferences(
    val dark: Boolean = true,
    val editorMono: Boolean = false,
    val syntaxColors: Boolean = true,
    val fontSize: Float = 19f,
    val readerSerif: Boolean = true,
    val readerWarm: Boolean = true,
    val synopsisLimit: Int = 500,
    val starterFolders: List<StarterFolder> = defaultStarterFolders,
    val readerFontSize: Float = fontSize,
    val savedTemplates: List<SavedTemplate> = emptyList(),
)

interface PreferencesRepository {
    val state: StateFlow<AppPreferences>

    fun update(value: AppPreferences)

    fun lastBook(): String

    fun lastEntry(): Long
    fun lastEntry(bookId: String): Long = lastEntry()
    fun editingPosition(bookId: String, id: Long): Pair<Int, Int> = 0 to 0
    fun editingPosition(bookId: String, id: Long, cursor: Int, scroll: Int) {}

    fun rememberBook(id: String)

    fun rememberEntry(entry: Entry)

    fun forgetWorkspace(bookId: String, entryIds: List<Long>)

    fun bookmark(id: Long): Boolean

    fun bookmark(id: Long, value: Boolean)

    fun readingPosition(id: Long): Pair<Int, Int>

    fun readingPosition(id: Long, index: Int, offset: Int)
}
