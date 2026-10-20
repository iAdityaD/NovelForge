package com.novelforge.app.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate

val bookStatuses = listOf("In progress", "Completed", "On hold")
data class WorkspaceTemplate(val name: String, val description: String)
val workspaceTemplates = listOf(
    WorkspaceTemplate("01_Chapters", "Acts, chapters and scenes"),
    WorkspaceTemplate("02_Characters", "Profiles, backstories and relationships"),
    WorkspaceTemplate("03_Lore", "Worlds, magic, geography and history"),
    WorkspaceTemplate("04_Plot", "Outlines, timelines and subplots"),
    WorkspaceTemplate("05_Metadata", "Premise, genre, themes and references"),
)
fun defaultFolder(kind: String) = when(kind) {
    "Chapter", "Screenplay" -> "01_Chapters"
    "Character", "Backstory" -> "02_Characters"
    "Location", "Worldbuilding", "Research" -> "03_Lore"
    "Arc", "Plot thread", "Timeline event" -> "04_Plot"
    else -> "05_Metadata"
}
fun descendantIds(id: Long, entries: List<Entry>): Set<Long> {
    val children=entries.groupBy {it.folderId}
    val found=mutableSetOf<Long>(); val queue=ArrayDeque<Long>();queue.add(id)
    while(queue.isNotEmpty()) {val next=queue.removeFirst();if(found.add(next)) children[next].orEmpty().forEach {queue.add(it.id)} }
    return found
}
fun canRelocate(entry: Entry, destination: Long?, entries: List<Entry>): Boolean {
    if(destination==null) return true
    val folder=entries.find {it.id==destination} ?: return false
    return folder.kind=="Folder" && folder.bookId==entry.bookId && !folder.deleted && destination !in descendantIds(entry.id,entries)
}
fun writingStreak(days: List<WritingDay>, today: LocalDate = LocalDate.now()): Int {
    val active=days.filter {it.edits>0}.map {it.day}.toSet()
    var day=if(today.toString() in active) today else today.minusDays(1)
    var count=0
    while(day.toString() in active) {count++;day=day.minusDays(1)}
    return count
}

val MIGRATION_1_2 = object: Migration(1,2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Book ADD COLUMN status TEXT NOT NULL DEFAULT 'In progress'")
        db.execSQL("ALTER TABLE Entry ADD COLUMN folderId INTEGER")
        db.execSQL("CREATE TABLE IF NOT EXISTS WritingDay (bookId TEXT NOT NULL, day TEXT NOT NULL, words INTEGER NOT NULL, edits INTEGER NOT NULL, PRIMARY KEY(bookId,day))")
        val books=mutableListOf<String>()
        db.query("SELECT id FROM Book").use { while(it.moveToNext()) books.add(it.getString(0)) }
        books.forEach { book ->
            val ids=mutableMapOf<String,Long>()
            workspaceTemplates.forEachIndexed { i,t ->
                db.execSQL("INSERT INTO Entry(bookId,title,body,kind,status,parentId,position,words,updated,deleted,folderId) VALUES (?,?,'','Folder','Draft',NULL,?,0,0,0,NULL)", arrayOf(book,t.name,i))
                db.query("SELECT last_insert_rowid()").use {it.moveToFirst();ids[t.name]=it.getLong(0)}
            }
            val entries=mutableListOf<Pair<Long,String>>()
            db.query("SELECT rowid,kind FROM Entry WHERE bookId=? AND kind!='Folder'",arrayOf(book)).use {while(it.moveToNext()) entries.add(it.getLong(0) to it.getString(1))}
            entries.forEach {(id,kind)->db.execSQL("UPDATE Entry SET folderId=? WHERE rowid=?",arrayOf(ids.getValue(defaultFolder(kind)),id))}
        }
    }
}
