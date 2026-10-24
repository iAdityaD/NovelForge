package com.novelforge.data.local

import androidx.room.withTransaction
import com.novelforge.app.data.*
import com.novelforge.domain.manuscript.*
import javax.inject.Inject
import kotlinx.coroutines.flow.map

class LocalManuscriptRepository @Inject constructor(private val db: ForgeDb) : ManuscriptRepository {
    override val placements = db.dao().manuscriptPlacements().map { rows -> rows.map { ManuscriptPlacement(it.documentId, it.bookId, it.position, it.included) } }
    override suspend fun add(bookId: String, documentIds: List<Long>) = db.withTransaction {
        val selected = documentIds.distinct().map { id ->
            requireNotNull(db.dao().entry(id)) { "A selected chapter is unavailable." }.also {
                require(it.bookId == bookId && it.kind == "Chapter" && !it.deleted) { "Select active chapters from this workspace." }
            }
        }
        val rows = db.dao().manuscriptPlacements(bookId).associateBy { it.documentId }
        val active = db.dao().entries(bookId).filter { it.kind == "Chapter" }
        var end = active.filter { rows[it.id]?.included != false }.maxOfOrNull { rows[it.id]?.position ?: it.position } ?: -1
        selected.filter { rows[it.id]?.included == false }.forEach { entry ->
            db.dao().place(ManuscriptPlacementEntity(entry.id, bookId, ++end, true))
        }
        if (selected.isNotEmpty()) db.dao().touch(bookId, System.currentTimeMillis())
    }

    override suspend fun move(documentId: Long, delta: Int) = db.withTransaction {
        require(delta == -1 || delta == 1)
        val entry = requireNotNull(db.dao().entry(documentId))
        require(!entry.deleted && entry.kind == "Chapter")
        val rows = db.dao().manuscriptPlacements(entry.bookId).associateBy { it.documentId }
        val chapters = db.dao().entries(entry.bookId).filter { it.kind == "Chapter" && rows[it.id]?.included != false }
            .sortedWith(compareBy<EntryEntity> { rows[it.id]?.position ?: it.position }.thenBy { it.id }).toMutableList()
        val from = chapters.indexOfFirst { it.id == documentId }
        val to = from + delta
        if (from >= 0 && to in chapters.indices) {
            chapters.add(to, chapters.removeAt(from))
            chapters.forEachIndexed { i, e -> db.dao().place(ManuscriptPlacementEntity(e.id, e.bookId, i)) }
            db.dao().touch(entry.bookId, System.currentTimeMillis())
        }
    }
    override suspend fun include(documentId: Long, included: Boolean) = db.withTransaction {
        val entry = requireNotNull(db.dao().entry(documentId))
        require(!entry.deleted && entry.kind == "Chapter")
        val rows = db.dao().manuscriptPlacements(entry.bookId)
        val previous = rows.find { it.documentId == documentId }
        if (included && previous?.included != false) return@withTransaction
        val end = maxOf(rows.maxOfOrNull { it.position } ?: -1, db.dao().entries(entry.bookId).maxOfOrNull { it.position } ?: -1) + 1
        db.dao().place(ManuscriptPlacementEntity(documentId, entry.bookId, if (included) end else previous?.position ?: entry.position, included))
        db.dao().touch(entry.bookId, System.currentTimeMillis())
    }
}
