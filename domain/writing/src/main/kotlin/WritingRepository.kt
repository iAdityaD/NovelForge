package com.novelforge.domain.writing
import com.novelforge.core.model.*
import kotlinx.coroutines.flow.*
interface WritingRepository {
 suspend fun entry(id:Long):Entry?
 suspend fun save(entry:Entry, previous:Entry?)
 suspend fun checkpoint(entry:Entry)
 suspend fun revisions(id:Long):List<Revision>
 suspend fun search(query:String):List<EntrySummary>
}
