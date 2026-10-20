package com.novelforge.domain.story

import com.novelforge.core.model.*
import kotlinx.coroutines.flow.*

interface StoryRepository {
    suspend fun connections(bookId: String): List<Pair<Long, Long>>
}
