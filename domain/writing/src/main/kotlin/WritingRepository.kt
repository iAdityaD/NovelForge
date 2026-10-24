package com.novelforge.domain.writing

import com.novelforge.core.model.*
import kotlinx.coroutines.flow.*

interface WritingRepository {
    suspend fun entry(id: Long): Entry?

    suspend fun save(entry: Entry, previous: Entry?)

    /** Persist a recoverable draft before the debounced document commit. */
    suspend fun stageDraft(entry: Entry, previous: Entry?) {}

    /** May return a separate recovered copy if the saved document has diverged. */
    suspend fun recoverDraft(entry: Entry): Entry? = null

    suspend fun checkpoint(entry: Entry)

    suspend fun revisions(id: Long): List<Revision>

    suspend fun search(query: String): List<EntrySummary>
}
