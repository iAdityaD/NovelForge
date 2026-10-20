package com.novelforge.domain.workspace
import com.novelforge.core.model.*
import kotlinx.coroutines.flow.*
interface WorkspaceRepository {
 val books: Flow<List<Book>>
 val summaries: Flow<List<EntrySummary>>
 val trash: Flow<List<Entry>>
 suspend fun createBook(title:String, author:String, genre:String, cover:Int, target:Int, synopsis:String, folders:Set<String>):String
 suspend fun editBook(book:Book)
 suspend fun createEntry(bookId:String,title:String,kind:String,parentId:Long?,folderId:Long?):Long
 suspend fun createFolder(bookId:String,title:String,parent:Long?)
 suspend fun renameEntry(id:Long,title:String)
 suspend fun relocate(id:Long,destination:Long?)
 suspend fun deleteEntry(id:Long)
 suspend fun restoreEntry(id:Long)
 suspend fun moveEntry(id:Long,delta:Int)
 suspend fun setBookStatus(book:Book,status:String)
 suspend fun loadDemo():String
}
