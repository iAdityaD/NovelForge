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

fun BookEntity.toModel() =
    Book(
        id,
        title,
        author,
        listOf(genre).filter { it.isNotBlank() },
        synopsis,
        cover,
        target,
        status,
        updated,
        overviewId,
    )

fun Book.toEntity() = BookEntity(id, title, author, genre, synopsis, cover, target, status, updated, overviewId)

fun EntryEntity.toModel() =
    Entry(
        id,
        bookId,
        title,
        body,
        kind,
        status,
        parentId,
        position,
        words,
        updated,
        deleted,
        folderId,
        area,
    )

fun Entry.toEntity() =
    EntryEntity(
        id,
        bookId,
        title,
        body,
        kind,
        status,
        parentId,
        position,
        words,
        updated,
        deleted,
        folderId,
        area,
    )

fun RevisionEntity.toModel() = Revision(id, entryId, title, body, created)

fun Revision.toEntity() = RevisionEntity(id, entryId, title, body, created)

fun WritingDayEntity.toModel() = WritingDay(bookId, day, words, edits)

fun WritingDay.toEntity() = WritingDayEntity(bookId, day, words, edits)

fun BookRecord.toModel() =
    book.toModel().copy(genres = genres.sortedBy { it.position }.map { it.name })
