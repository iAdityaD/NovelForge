package com.novelforge.core.model

import java.util.UUID

data class Book(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String = "",
    val genres: List<String> = listOf("Fantasy"),
    val synopsis: String = "",
    val cover: Int = 0,
    val target: Int = 80000,
    val status: String = "In progress",
    val updated: Long = System.currentTimeMillis(),
) {
    val genre: String
        get() = genres.joinToString(" · ")
}

data class Entry(
    val id: Long = 0,
    val bookId: String,
    val title: String,
    val body: String = "",
    val kind: String = "Chapter",
    val status: String = "Draft",
    val parentId: Long? = null,
    val position: Int = 0,
    val words: Int = 0,
    val updated: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val folderId: Long? = null,
)

data class EntryFts(val title: String, val body: String)

data class EntrySummary(
    val id: Long,
    val bookId: String,
    val title: String,
    val kind: String,
    val status: String,
    val parentId: Long?,
    val position: Int,
    val words: Int,
    val updated: Long,
    val folderId: Long? = null,
)

data class Revision(
    val id: Long = 0,
    val entryId: Long,
    val title: String,
    val body: String,
    val created: Long = System.currentTimeMillis(),
)

data class WritingDay(val bookId: String, val day: String, val words: Int = 0, val edits: Int = 0)

fun wordCount(text: String): Int =
    Regex("[\\p{L}\\p{M}\\p{N}]+(?:['’][\\p{L}\\p{M}\\p{N}]+)*").findAll(text).count()

fun searchExpression(text: String): String =
    Regex("[\\p{L}\\p{M}\\p{N}]+").findAll(text).take(12).joinToString(" AND ") {
        "\"${it.value}\"*"
    }
