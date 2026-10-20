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
class LocalWorkspaceRepository @Inject constructor(private val db:ForgeDb):WorkspaceRepository {
 private val dao=Records(db.dao())
 override val books=dao.books()
 override val summaries=dao.summaries()
 override val trash=dao.trash()
 override suspend fun createBook(title:String,author:String,genre:String,cover:Int,target:Int,synopsis:String,folders:Set<String>):String {
        val book =
            Book(
                title = title.trim(),
                author = author.trim(),
                genre = genre,
                cover = cover,
                target = target,
                synopsis = synopsis,
            )
        db.withTransaction {
            dao.addBook(book)
            val ids =
                folders.associateWith { name ->
                    dao.addEntry(Entry(bookId = book.id, title = name, kind = "Folder"))
                }
            if ("01_Chapters" in folders)
                dao.addEntry(
                    Entry(bookId = book.id, title = "Chapter 1", folderId = ids["01_Chapters"])
                )
            if ("05_Metadata" in folders)
                dao.addEntry(
                    Entry(
                        bookId = book.id,
                        title = "Novel overview",
                        kind = "Note",
                        folderId = ids["05_Metadata"],
                        body =
                            "## Genre\n${book.genre}\n\n## Premise\n${book.synopsis}\n\n## Themes\n\n## Links & references\n",
                    )
                )
        }
        return book.id
    
 }
 override suspend fun editBook(book:Book) {
        dao.updateBook(book.copy(updated = System.currentTimeMillis()))
    
 }
 override suspend fun createEntry(bookId:String,title:String,kind:String,parentId:Long?,folderId:Long?):Long {
        require(title.isNotBlank()) { "A file name is required." }
        if (folderId != null)
            require(
                dao.entry(folderId)?.let {
                    it.bookId == bookId && it.kind == "Folder" && !it.deleted
                } == true
            ) {
                "The destination folder is unavailable."
            }
        val position =
            summaries.first().filter { it.bookId == bookId }.maxOfOrNull { it.position }?.plus(1) ?: 0
        val body = template(kind)
        val id =
            dao.addEntry(
                Entry(
                    bookId = bookId,
                    title = title.trim(),
                    kind = kind,
                    parentId = parentId,
                    folderId = folderId,
                    body = body,
                    position = position,
                    words = wordCount(body),
                )
            )
        dao.touch(bookId, System.currentTimeMillis())
        return id
    
 }
 override suspend fun createFolder(bookId:String,title:String,parent:Long?) {
        require(title.trim().isNotEmpty()) { "A folder name is required." }
        if (parent != null)
            require(
                dao.entry(parent)?.let {
                    it.bookId == bookId && it.kind == "Folder" && !it.deleted
                } == true
            )
        dao.addEntry(
            Entry(
                bookId = bookId,
                title = title.trim().take(200),
                kind = "Folder",
                folderId = parent,
            )
        )
        dao.touch(bookId, System.currentTimeMillis())
    
 }
 override suspend fun renameEntry(id:Long,title:String) {
        
        require(title.trim().isNotEmpty()) { "A name is required." }
        dao.entry(id)?.let { entry ->
            val changed =
                entry.copy(title = title.trim().take(200), updated = System.currentTimeMillis())
            db.withTransaction {
                dao.updateEntry(changed)
                dao.touch(changed.bookId, changed.updated)
            }

        }
    
 }
 override suspend fun relocate(id:Long,destination:Long?) {
        
        val e = dao.entry(id) ?: return
        val all = dao.entries(e.bookId)
        require(canRelocate(e, destination, all)) {
            "A folder cannot contain itself or its descendants."
        }
        val changed = e.copy(folderId = destination, updated = System.currentTimeMillis())
        db.withTransaction {
            dao.updateEntry(changed)
            dao.touch(changed.bookId, changed.updated)
        }

    
 }
 override suspend fun deleteEntry(id:Long) {
        
        val e = dao.entry(id) ?: return
        val all = dao.entries(e.bookId)
        val ids = descendantIds(id, all)
        db.withTransaction {
            ids.forEach { dao.setDeleted(it, true) }
            dao.touch(e.bookId, System.currentTimeMillis())
        }


    
 }
 override suspend fun restoreEntry(id:Long) {
        val e = dao.entry(id) ?: return
        val all = trash.first().filter { it.bookId == e.bookId }
        db.withTransaction {
            descendantIds(id, all).forEach { dao.setDeleted(it, false) }
            var parent = e.folderId
            val seen = mutableSetOf<Long>()
            while (parent != null && seen.add(parent)) {
                val folder = dao.entry(parent) ?: break
                dao.setDeleted(folder.id, false)
                parent = folder.folderId
            }
        }
    
 }
 override suspend fun moveEntry(id:Long,delta:Int) {
        val e = dao.entry(id) ?: return
        val entries =
            dao.entries(e.bookId)
                .filter {
                    it.folderId == e.folderId && (it.kind == "Folder") == (e.kind == "Folder")
                }
                .sortedWith(compareBy<Entry> { it.position }.thenBy { it.title })
        val index = entries.indexOfFirst { it.id == id }
        val target = index + delta
        if (target !in entries.indices) return
        val list = entries.toMutableList()
        val moved = list.removeAt(index)
        list.add(target, moved)
        db.withTransaction {
            list.forEachIndexed { i, item -> dao.updateEntry(item.copy(position = i)) }
        }
    
 }
 override suspend fun setBookStatus(book:Book,status:String) {
        require(status in bookStatuses)
        dao.updateBook(book.copy(status = status, updated = System.currentTimeMillis()))
    
 }
 override suspend fun loadDemo():String {
        val book =
            Book(
                title = "The Atlas of\nForgotten Things",
                author = "E. W. Arden",
                genre = "Literary fantasy",
                synopsis =
                    "In a city that forgets one thing every midnight, a young cartographer begins drawing a map of everything that has disappeared.",
                cover = 0,
                target = 70000,
            )
        db.withTransaction {
            dao.addBook(book)
            val arc =
                dao.addEntry(
                    Entry(
                        bookId = book.id,
                        title = "The city that forgets",
                        kind = "Arc",
                        body =
                            "## Central conflict\nMara must choose between preserving the past and protecting the people she loves.\n\n## Turning point\nThe atlas begins to remember her.",
                        position = 0,
                    )
                )
            dao.addEntry(
                Entry(
                    bookId = book.id,
                    title = "A map of the missing",
                    parentId = arc,
                    status = "In progress",
                    body = DEMO_TEXT,
                    words = wordCount(DEMO_TEXT),
                    position = 1,
                )
            )
            dao.addEntry(
                Entry(
                    bookId = book.id,
                    title = "The midnight collector",
                    parentId = arc,
                    body =
                        "The bell rang thirteen times.\n\nMara counted again, though the sound had already vanished into the rain. Somewhere in the lower city, a door that had never existed was opening.",
                    words = 31,
                    position = 2,
                )
            )
            dao.addEntry(
                Entry(
                    bookId = book.id,
                    title = "Mara Venn",
                    kind = "Character",
                    body =
                        "## Role in the story\nCartographer of forgotten places.\n\n## Desire & motivation\nTo find the street where her mother once lived.\n\n## Fear & flaw\nShe trusts maps more than people.\n\n## Character arc\nFrom collecting memories to making new ones.",
                    position = 3,
                )
            )
            dao.addEntry(
                Entry(
                    bookId = book.id,
                    title = "The paper district",
                    kind = "Location",
                    body =
                        "## Atmosphere\nRain-dark stone, warm windows, and the smell of ink.\n\n## History\nEvery street was once named after a story. Most have forgotten their names.",
                    position = 4,
                )
            )
            dao.addEntry(
                Entry(
                    bookId = book.id,
                    title = "The missing street",
                    kind = "Plot thread",
                    body =
                        "## Setup\nMara finds a blank space on a map.\n\n## Foreshadowing\nThe city's oldest residents avoid the western bridge.\n\n## Payoff\nThe street was hidden, not forgotten.",
                    position = 5,
                )
            )
            val existing = dao.entries(book.id)
            val folders =
                workspaceTemplates.associate {
                    it.name to
                        dao.addEntry(Entry(bookId = book.id, title = it.name, kind = "Folder"))
                }
            existing.forEach { e ->
                dao.updateEntry(e.copy(folderId = folders[defaultFolder(e.kind)]))
            }
        }
        return book.id
    
 }
}
val DEMO_TEXT =
    """The city had forgotten the sea.

Mara noticed it first in the maps. Where blue ink had once pooled against the western edge of the paper, there was now a pale, unfinished space. The cartographers had drawn small mountains over it. They looked apologetic.

She stood at the window of her workshop, watching rain gather on the sill. Below, the paper district was waking: shutters creaked open, a bicycle rattled over the stones, and someone was singing a song with no words.

On her desk lay the only map that still remembered.

It was no larger than her hand, folded twice and soft at the corners. Her mother had given it to her on a Thursday. Mara was certain about the Thursday, though she could no longer remember the year.

“Some things,” her mother had said, “need someone to keep looking.”

She unfolded it carefully.

A thin blue line shimmered across the paper. Not a river. Not a road. Something that moved when she wasn't looking directly at it.

For the first time in seven years, the workshop smelled of salt.

Mara reached for her pen.

Outside, the bells began to count down to midnight, though the morning sun had only just found its way between the roofs. The bicycle stopped. The singing stopped. Even the rain seemed to wait.

And in the blank space at the edge of the map, a small black door appeared."""
