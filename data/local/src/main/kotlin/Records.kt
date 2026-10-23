package com.novelforge.data.local

import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.domain.analytics.*
import com.novelforge.domain.backup.*
import com.novelforge.domain.preferences.*
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*
import com.novelforge.domain.writing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class Records(private val dao: ForgeDao) {
    private suspend fun saveGenres(book: Book) {
        dao.clearGenres(book.id)
        dao.addGenres(book.genres.mapIndexed { i, g -> BookGenreEntity(book.id, g, i) })
    }

    fun books() = dao.bookRecords().map { list -> list.map { it.toModel() } }

    fun summaries() = dao.summaries()

    fun trash() = dao.trash().map { list -> list.map { it.toModel() } }

    fun writingDays() = dao.writingDays().map { list -> list.map { it.toModel() } }

    suspend fun book(id: String) = dao.bookRecord(id)?.toModel()

    suspend fun entry(id: Long) = dao.entry(id)?.toModel()

    suspend fun entries(id: String) = dao.entries(id).map { it.toModel() }

    suspend fun addBook(book: Book) {
        dao.addBook(book.toEntity())
        saveGenres(book)
    }

    suspend fun updateBook(book: Book) {
        dao.updateBook(book.toEntity())
        saveGenres(book)
    }

    suspend fun addEntry(entry: Entry) = dao.addDocument(entry.toEntity())

    suspend fun updateEntry(entry: Entry) = dao.updateEntry(entry.toEntity())

    suspend fun setDeleted(id: Long, value: Boolean) = dao.setDeleted(id, value)

    suspend fun touch(id: String, time: Long) = dao.touch(id, time)

    suspend fun addRevision(revision: Revision) = dao.addRevision(revision.toEntity())

    suspend fun revisions(id: Long) = dao.revisions(id).map { it.toModel() }

    suspend fun prune(id: Long) = dao.prune(id)

    suspend fun search(query: String) = dao.search(query)

    suspend fun ensureDay(id: String, day: String) = dao.ensureDay(id, day)

    suspend fun recordWriting(id: String, day: String, words: Int) =
        dao.recordWriting(id, day, words)
}
