package com.novelforge.data.local
import com.novelforge.core.model.*
import com.novelforge.app.data.*
import com.novelforge.domain.workspace.*
import com.novelforge.domain.writing.*
import com.novelforge.domain.story.*
import com.novelforge.domain.analytics.*
import com.novelforge.domain.backup.*
import com.novelforge.domain.preferences.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton
class LocalStoryRepository @Inject constructor(db:ForgeDb):StoryRepository {
 private val dao=Records(db.dao())
 override suspend fun connections(bookId:String):List<Pair<Long,Long>> {
 val pages=dao.entries(bookId); val ids=pages.map{it.id}.toSet()
 return pages.flatMap { page -> Regex("\\[\\[(\\d+)\\|").findAll(page.body).mapNotNull { m -> m.groupValues[1].toLongOrNull()?.takeIf{it in ids}?.let{page.id to it} }.toList() }.distinct()
 }
}
class LocalAnalyticsRepository @Inject constructor(db:ForgeDb):AnalyticsRepository {override val writingDays=Records(db.dao()).writingDays()}
