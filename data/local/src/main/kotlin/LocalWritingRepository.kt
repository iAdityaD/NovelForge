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
@Singleton
class LocalWritingRepository @Inject constructor(private val db:ForgeDb):WritingRepository {
 private val dao=Records(db.dao())
 override suspend fun entry(id:Long)=dao.entry(id)
 override suspend fun save(entry:Entry,previous:Entry?) = db.withTransaction {
  // Never resurrect a file trashed while the editor was open.
  val existing=dao.entry(entry.id) ?: error("This file is unavailable.")
  require(!existing.deleted) { "This file is in Recently deleted." }
  val saved=entry.copy(folderId=existing.folderId, position=existing.position, words=wordCount(entry.body),updated=System.currentTimeMillis())
  dao.updateEntry(saved);dao.touch(saved.bookId,saved.updated)
  if(entry.body!=previous?.body){val day=java.time.LocalDate.now().toString();dao.ensureDay(saved.bookId,day);dao.recordWriting(saved.bookId,day,(wordCount(entry.body)-wordCount(previous?.body ?: "")).coerceAtLeast(0))}
 }
 override suspend fun checkpoint(entry:Entry)=db.withTransaction {dao.addRevision(Revision(entryId=entry.id,title=entry.title,body=entry.body));dao.prune(entry.id)}
 override suspend fun revisions(id:Long)=dao.revisions(id)
 override suspend fun search(query:String):List<EntrySummary>{val expression=searchExpression(query);return if(expression.isBlank()) emptyList() else dao.search(expression)}
}
