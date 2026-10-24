package com.novelforge.domain.manuscript

import com.novelforge.core.model.EntrySummary
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
    return entries.filter { it.kind == "Chapter" && byId[it.id]?.included != false }
        .sortedWith(compareBy<EntrySummary> { byId[it.id]?.position ?: it.position }.thenBy { it.id })
}
