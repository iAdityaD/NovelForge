package com.novelforge.app.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity
data class Book(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String = "",
    val genre: String = "Fantasy",
    val synopsis: String = "",
    val cover: Int = 0,
    val target: Int = 80000,
    @ColumnInfo(defaultValue = "'In progress'") val status: String = "In progress",
    val updated: Long = System.currentTimeMillis(),
)

@Entity(
    indices = [Index("bookId"), Index("parentId")],
    foreignKeys =
        [
            ForeignKey(
                entity = Book::class,
                parentColumns = ["id"],
                childColumns = ["bookId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
)
data class Entry(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Long = 0,
    val bookId: String,
    val title: String,
    val body: String = "",
    val kind: String = "Chapter",
    val status: String = "Draft",
    val parentId: Long? = null,
    val position: Int = 0,
    val words: Int = 0,
    val updated: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val folderId: Long? = null,
)

@Fts4(contentEntity = Entry::class)
@Entity(tableName = "entry_fts")
data class EntryFts(val title: String, val body: String)

data class EntrySummary(
    val id: Long,
    val bookId: String,
    val title: String,
    val kind: String,
    val status: String,
    val parentId: Long?,
    val position: Int,
    val words: Int,
    val updated: Long,
    val folderId: Long? = null,
)

@Entity(
    indices = [Index("entryId")],
    foreignKeys =
        [
            ForeignKey(
                entity = Entry::class,
                parentColumns = ["rowid"],
                childColumns = ["entryId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
)
data class Revision(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val title: String,
    val body: String,
    val created: Long = System.currentTimeMillis(),
)

@Entity(primaryKeys = ["bookId", "day"])
data class WritingDay(val bookId: String, val day: String, val words: Int = 0, val edits: Int = 0)

@Dao
interface ForgeDao {
    @Query("SELECT * FROM WritingDay ORDER BY day") fun writingDays(): Flow<List<WritingDay>>
    @Query("INSERT OR IGNORE INTO WritingDay(bookId, day, words, edits) VALUES (:bookId, :day, 0, 0)") suspend fun ensureDay(bookId: String, day: String)
    @Query("UPDATE WritingDay SET words = words + :words, edits = edits + 1 WHERE bookId = :bookId AND day = :day") suspend fun recordWriting(bookId: String, day: String, words: Int)

    @Query("SELECT * FROM Book ORDER BY updated DESC") fun books(): Flow<List<Book>>

    @Query(
        "SELECT rowid AS id, bookId, title, kind, status, parentId, position, words, updated, folderId FROM Entry WHERE deleted = 0 ORDER BY position, rowid"
    )
    fun summaries(): Flow<List<EntrySummary>>

    @Query("SELECT * FROM Entry WHERE rowid = :id") suspend fun entry(id: Long): Entry?

    @Query("SELECT * FROM Entry WHERE bookId = :bookId AND deleted = 0 ORDER BY position, rowid")
    suspend fun entries(bookId: String): List<Entry>

    @Query("SELECT * FROM Book WHERE id = :id") suspend fun book(id: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addBook(book: Book)

    @Update suspend fun updateBook(book: Book)

    @Insert suspend fun addEntry(entry: Entry): Long

    @Update suspend fun updateEntry(entry: Entry)

    @Query("UPDATE Book SET updated = :time WHERE id = :id")
    suspend fun touch(id: String, time: Long)

    @Query("UPDATE Entry SET deleted = :deleted WHERE rowid = :id")
    suspend fun setDeleted(id: Long, deleted: Boolean)

    @Query("SELECT * FROM Entry WHERE deleted = 1 ORDER BY updated DESC")
    fun trash(): Flow<List<Entry>>

    @Query(
        "SELECT rowid AS id, bookId, title, kind, status, parentId, position, words, updated, folderId FROM Entry WHERE deleted = 0 AND kind != 'Folder' AND rowid IN (SELECT rowid FROM entry_fts WHERE entry_fts MATCH :query) ORDER BY updated DESC LIMIT 80"
    )
    suspend fun search(query: String): List<EntrySummary>

    @Insert suspend fun addRevision(revision: Revision)

    @Query("SELECT * FROM Revision WHERE entryId = :id ORDER BY created DESC LIMIT 20")
    suspend fun revisions(id: Long): List<Revision>

    @Query(
        "DELETE FROM Revision WHERE entryId = :id AND id NOT IN (SELECT id FROM Revision WHERE entryId = :id ORDER BY created DESC LIMIT 20)"
    )
    suspend fun prune(id: Long)

    @Query("SELECT COUNT(*) FROM Book") suspend fun bookCount(): Int
}

@Database(
    entities = [Book::class, Entry::class, EntryFts::class, Revision::class, WritingDay::class],
    version = 2,
    exportSchema = true,
)
abstract class ForgeDb : RoomDatabase() {
    abstract fun dao(): ForgeDao
}

fun wordCount(text: String): Int =
    Regex("[\\p{L}\\p{M}\\p{N}]+(?:['’][\\p{L}\\p{M}\\p{N}]+)*").findAll(text).count()

fun searchExpression(text: String): String =
    Regex("[\\p{L}\\p{M}\\p{N}]+").findAll(text).take(12).joinToString(" AND ") {
        "\"${it.value}\"*"
    }

val kinds =
    listOf(
        "Chapter",
        "Arc",
        "Character",
        "Location",
        "Backstory",
        "Worldbuilding",
        "Plot thread",
        "Research",
        "Screenplay",
        "Timeline event",
        "Note",
    )
val statuses = listOf("Draft", "In progress", "Revised", "Final")

fun template(kind: String): String =
    when (kind) {
        "Character" ->
            "## Role in the story\n\n## Desire & motivation\n\n## Fear & flaw\n\n## Appearance & voice\n\n## Backstory\n\n## Relationships\n\n## Character arc\n"
        "Timeline event" ->
            "## When\n\n## What happens\n\n## People involved\n\n## Cause\n\n## Consequence\n"
        "Location" ->
            "## Atmosphere\n\n## Geography\n\n## History\n\n## People & culture\n\n## Story significance\n"
        "Arc" ->
            "## Central conflict\n\n## Beginning\n\n## Turning point\n\n## Climax & resolution\n"
        "Backstory" ->
            "## What happened?\n\n## Who was involved?\n\n## When did it happen?\n\n## Consequences in the present\n"
        "Plot thread" -> "## Setup\n\n## Foreshadowing\n\n## Development\n\n## Payoff\n"
        "Screenplay" ->
            "INT. LOCATION - DAY\n\nDescribe the action.\n\nCHARACTER\nDialogue begins here.\n"
        "Worldbuilding" -> "## Rules\n\n## Culture & history\n\n## Limitations\n\n## Connections\n"
        else -> ""
    }
