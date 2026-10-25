package com.novelforge.domain.workspace

import com.novelforge.core.model.*
import kotlinx.coroutines.flow.*

interface WorkspaceRepository {
    val books: Flow<List<Book>>
    val summaries: Flow<List<EntrySummary>>
    val trash: Flow<List<Entry>>

    suspend fun createBook(
        title: String,
        author: String,
        genres: List<String>,
        cover: Int,
        target: Int,
        standard: Boolean,
        templateId: String = "novel",
    ): CreatedWorkspace

    suspend fun createNode(bookId: String, title: String, folder: Boolean, area: String, parent: Long?, typeLabel: String = "", sourceUrl: String? = null, categoryName: String? = null): Long

    suspend fun deleteBook(bookId: String)

    suspend fun editBook(book: Book)

    suspend fun createEntry(
        bookId: String,
        title: String,
        kind: String,
        parentId: Long?,
        folderId: Long?,
    ): Long

    /** A resource always belongs to an existing or atomically created category folder. */
    suspend fun createResource(bookId: String, title: String, folderId: Long?, newCategory: String?, sourceUrl: String?, notes: String): Long

    suspend fun createFolder(bookId: String, title: String, parent: Long?)

    suspend fun renameEntry(id: Long, title: String)

    suspend fun setArea(id: Long, area: String)

    suspend fun relocate(id: Long, destination: Long?)

    suspend fun deleteEntry(id: Long)

    suspend fun restoreEntry(id: Long)

    suspend fun placeBefore(id: Long, targetId: Long, after: Boolean = false)

    suspend fun moveEntry(id: Long, delta: Int)

    suspend fun setBookStatus(book: Book, status: String)

    suspend fun loadDemo(): String
}
