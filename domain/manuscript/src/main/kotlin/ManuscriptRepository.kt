package com.novelforge.domain.manuscript

import com.novelforge.core.model.EntrySummary
import com.novelforge.core.model.Entry
import com.novelforge.core.model.manuscriptIncluded
import kotlinx.coroutines.flow.Flow

data class ManuscriptPlacement(val documentId: Long, val bookId: String, val position: Int, val included: Boolean = true)

interface ManuscriptRepository {
    val placements: Flow<List<ManuscriptPlacement>>
    suspend fun add(bookId: String, documentIds: List<Long>)
    suspend fun move(documentId: Long, delta: Int)
    suspend fun include(documentId: Long, included: Boolean)
}

/** Canonical chapter order, independent from the explorer's folder/sibling ordering. */
fun manuscriptChapters(entries: List<EntrySummary>, placements: List<ManuscriptPlacement>): List<EntrySummary> {
    val byId = placements.associateBy { it.documentId }
    return entries.filter { manuscriptIncluded(it.kind, byId[it.id]?.included) }
        .sortedWith(compareBy<EntrySummary> { byId[it.id]?.position ?: it.position }.thenBy { it.id })
}

/** Export and reading use the same inclusion and ordering projection. */
fun compileManuscript(entries: List<Entry>, placements: List<ManuscriptPlacement>): List<Entry> {
    val byId = entries.associateBy { it.id }
    val summaries = entries.filter { !it.deleted }.map { e ->
        EntrySummary(e.id, e.bookId, e.title, e.kind, e.status, e.parentId, e.position, e.words, e.updated, e.folderId)
    }
    return manuscriptChapters(summaries, placements).map { byId.getValue(it.id) }
}
