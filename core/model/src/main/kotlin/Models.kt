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
    val overviewId: Long? = null,
    val fields: List<FieldDefinition> = emptyList(),
    val coverImage: String = "",
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
    val area: String = if (isManuscriptKind(kind)) "manuscript" else "plan",
    val metadata: Map<String, List<String>> = emptyMap(),
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
    val area: String = if (isManuscriptKind(kind)) "manuscript" else "plan",
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

val statuses = listOf("Draft", "In progress", "Revised", "Final")
