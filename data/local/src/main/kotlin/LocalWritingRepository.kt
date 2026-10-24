package com.novelforge.data.local

import androidx.room.withTransaction
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.domain.analytics.*
import com.novelforge.domain.backup.*
import com.novelforge.domain.preferences.*
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*
import com.novelforge.domain.writing.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Singleton
class LocalWritingRepository @Inject constructor(private val db: ForgeDb, private val journal: LocalDraftJournal? = null) : WritingRepository {
    private val dao = Records(db.dao())

    override suspend fun entry(id: Long) = dao.entry(id)

    override suspend fun stageDraft(entry: Entry, previous: Entry?) {
        journal?.write(entry, previous)
    }

    override suspend fun recoverDraft(entry: Entry): Entry? {
        val storage = journal ?: return null
        val pending = storage.read(entry) ?: return null
        if (storage.fingerprint(entry) == storage.fingerprint(pending.entry)) {
            storage.clear(entry.id)
            return null
        }
        if (storage.fingerprint(entry) == pending.baseFingerprint) return pending.entry
        // Never overwrite a newer saved edit. Preserve the interrupted text as a separate document.
        val recovered = db.withTransaction {
            val title = pending.entry.title.take(180) + " (recovered)"
            dao.entries(entry.bookId).firstOrNull { it.title == title && it.body == pending.entry.body && it.kind == pending.entry.kind }
                ?: pending.entry.copy(id = 0, title = title, words = wordCount(pending.entry.body)).let { copy ->
                    copy.copy(id = dao.addEntry(copy))
                }
        }
        storage.clear(entry.id)
        return recovered
    }

    override suspend fun save(entry: Entry, previous: Entry?) {
        db.withTransaction {
            // Never resurrect a file trashed while the editor was open.
            val existing = dao.entry(entry.id) ?: error("This file is unavailable.")
            require(!existing.deleted) { "This file is in Recently deleted." }
            val saved =
                entry.copy(
                    folderId = existing.folderId,
                    area = existing.area,
                    position = existing.position,
                    words = wordCount(entry.body),
                    updated = System.currentTimeMillis(),
                )
            dao.updateEntry(saved)
            dao.touch(saved.bookId, saved.updated)
            if (entry.body != previous?.body) {
                val day = java.time.LocalDate.now().toString()
                dao.ensureDay(saved.bookId, day)
                dao.recordWriting(
                    saved.bookId,
                    day,
                    (wordCount(entry.body) - wordCount(previous?.body ?: "")).coerceAtLeast(0),
                )
            }
        }
        journal?.clear(entry.id)
    }

    override suspend fun checkpoint(entry: Entry) =
        db.withTransaction {
            dao.addRevision(Revision(entryId = entry.id, title = entry.title, body = entry.body))
            dao.prune(entry.id)
        }

    override suspend fun revisions(id: Long) = dao.revisions(id)

    override suspend fun search(query: String): List<EntrySummary> {
        val expression = searchExpression(query)
        return if (expression.isBlank()) emptyList() else dao.search(expression)
    }
}
