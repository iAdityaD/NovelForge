package com.novelforge.app.data

import androidx.room.*
import java.util.UUID
import com.novelforge.core.model.EntrySummary
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "Book")
data class BookEntity(
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
    tableName = "Entry",
    indices = [Index("bookId"), Index("parentId")],
    foreignKeys =
        [
            ForeignKey(
                entity = BookEntity::class,
                parentColumns = ["id"],
                childColumns = ["bookId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
)
data class EntryEntity(
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

@Fts4(contentEntity = EntryEntity::class)
@Entity(tableName = "entry_fts")
data class EntryFts(val title: String, val body: String)


@Entity(
    tableName = "Revision",
    indices = [Index("entryId")],
    foreignKeys =
        [
            ForeignKey(
                entity = EntryEntity::class,
                parentColumns = ["rowid"],
                childColumns = ["entryId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
)
data class RevisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val title: String,
    val body: String,
    val created: Long = System.currentTimeMillis(),
)

@Entity(tableName = "WritingDay", primaryKeys = ["bookId", "day"])
data class WritingDayEntity(val bookId: String, val day: String, val words: Int = 0, val edits: Int = 0)

@Dao
interface ForgeDao {
    @Query("SELECT * FROM WritingDay ORDER BY day") fun writingDays(): Flow<List<WritingDayEntity>>

    @Query(
        "INSERT OR IGNORE INTO WritingDay(bookId, day, words, edits) VALUES (:bookId, :day, 0, 0)"
    )
    suspend fun ensureDay(bookId: String, day: String)

    @Query(
        "UPDATE WritingDay SET words = words + :words, edits = edits + 1 WHERE bookId = :bookId AND day = :day"
    )
    suspend fun recordWriting(bookId: String, day: String, words: Int)

    @Query("SELECT * FROM Book ORDER BY updated DESC") fun books(): Flow<List<BookEntity>>

    @Query(
        "SELECT rowid AS id, bookId, title, kind, status, parentId, position, words, updated, folderId FROM Entry WHERE deleted = 0 ORDER BY position, rowid"
    )
    fun summaries(): Flow<List<EntrySummary>>

    @Query("SELECT * FROM Entry WHERE rowid = :id") suspend fun entry(id: Long): EntryEntity?

    @Query("SELECT * FROM Entry WHERE bookId = :bookId AND deleted = 0 ORDER BY position, rowid")
    suspend fun entries(bookId: String): List<EntryEntity>

    @Query("SELECT * FROM Book WHERE id = :id") suspend fun book(id: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addBook(book: BookEntity)

    @Update suspend fun updateBook(book: BookEntity)

    @Insert suspend fun addEntry(entry: EntryEntity): Long

    @Update suspend fun updateEntry(entry: EntryEntity)

    @Query("UPDATE Book SET updated = :time WHERE id = :id")
    suspend fun touch(id: String, time: Long)

    @Query("UPDATE Entry SET deleted = :deleted WHERE rowid = :id")
    suspend fun setDeleted(id: Long, deleted: Boolean)

    @Query("SELECT * FROM Entry WHERE deleted = 1 ORDER BY updated DESC")
    fun trash(): Flow<List<EntryEntity>>

    @Query(
        "SELECT rowid AS id, bookId, title, kind, status, parentId, position, words, updated, folderId FROM Entry WHERE deleted = 0 AND kind != 'Folder' AND rowid IN (SELECT rowid FROM entry_fts WHERE entry_fts MATCH :query) ORDER BY updated DESC LIMIT 80"
    )
    suspend fun search(query: String): List<EntrySummary>

    @Insert suspend fun addRevision(revision: RevisionEntity)

    @Query("SELECT * FROM Revision WHERE entryId = :id ORDER BY created DESC LIMIT 20")
    suspend fun revisions(id: Long): List<RevisionEntity>

    @Query(
        "DELETE FROM Revision WHERE entryId = :id AND id NOT IN (SELECT id FROM Revision WHERE entryId = :id ORDER BY created DESC LIMIT 20)"
    )
    suspend fun prune(id: Long)

    @Query("SELECT COUNT(*) FROM Book") suspend fun bookCount(): Int
}

@Database(
    entities = [BookEntity::class, EntryEntity::class, EntryFts::class, RevisionEntity::class, WritingDayEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class ForgeDb : RoomDatabase() {
    abstract fun dao(): ForgeDao
}

