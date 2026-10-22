package com.novelforge.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.data.local.*
import com.novelforge.domain.workspace.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StarterWorkspaceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun prefs(): LocalPreferencesRepository {
        context.getSharedPreferences("preferences", 0).edit().clear().commit()
        return LocalPreferencesRepository(context)
    }
    private fun db() = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()

    @Test fun standardGuidesChaptersAndIdentitySurviveDatabaseReopen() = runBlocking {
        val preferences = prefs()
        context.deleteDatabase("starter-persistence")
        var database = Room.databaseBuilder(context, ForgeDb::class.java, "starter-persistence").build()
        val repo = LocalWorkspaceRepository(database, preferences)
        val created = repo.createBook("New world", "", listOf("Fantasy", "Drama"), 0, 80000, true)
        val files = database.dao().entries(created.bookId)
        assertEquals(defaultStarterFolders.map { it.name }, files.filter { it.kind == "Folder" }.map { it.title })
        assertEquals(8, files.count { it.kind == "Guide" && it.title == "README" })
        assertEquals(2, files.count { it.kind == "Chapter" })
        assertTrue(files.filter { it.kind == "Guide" }.all { it.body.contains("All planning is optional") })
        val guide = database.dao().entry(created.overviewId!!)!!
        val chapter = files.first { it.kind == "Chapter" }
        val linkBody = "See [[${guide.id}|Overview]]"
        database.dao().updateEntry(chapter.copy(body = linkBody))
        repo.renameEntry(guide.id, "Story reference")
        repo.relocate(guide.id, null)
        database.close()
        database = Room.databaseBuilder(context, ForgeDb::class.java, "starter-persistence").build()
        try {
            assertEquals("Story reference", database.dao().entry(guide.id)!!.title)
            assertNull(database.dao().entry(guide.id)!!.folderId)
            assertEquals(linkBody, database.dao().entry(chapter.id)!!.body)
            assertEquals(2, database.dao().bookRecord(created.bookId)!!.genres.size)
        } finally { database.close(); context.deleteDatabase("starter-persistence") }
    }

    @Test fun scratchIsEmptyAndCustomizedKeysRetainTheirPurpose() = runBlocking {
        val preferences = prefs()
        val custom = listOf(StarterFolder("overview", "Home"), StarterFolder("manuscript", "Draft pages"), StarterFolder("ideas", "Unused", false), StarterFolder("custom-1", "Research desk"))
        preferences.update(preferences.state.value.copy(starterFolders = custom))
        assertEquals(custom, LocalPreferencesRepository(context).state.value.starterFolders)
        val database = db()
        try {
            val repo = LocalWorkspaceRepository(database, preferences)
            val scratch = repo.createBook("Empty", "", emptyList(), 0, 80000, false)
            assertNull(scratch.overviewId)
            assertTrue(database.dao().entries(scratch.bookId).isEmpty())
            val standard = repo.createBook("Custom", "", emptyList(), 0, 80000, true)
            val files = database.dao().entries(standard.bookId)
            assertEquals(listOf("Home", "Draft pages", "Research desk"), files.filter { it.kind == "Folder" }.map { it.title })
            val home = files.first { it.title == "Home" }
            assertEquals(home.id, database.dao().entry(standard.overviewId!!)!!.folderId)
            assertEquals(2, files.count { it.kind == "Chapter" })
            assertEquals(3, files.count { it.kind == "Guide" })
            assertTrue(database.dao().entries(scratch.bookId).isEmpty())
        } finally { database.close() }
    }

    @Test fun deletingWorkspaceCleansDependentDataAndCannotResurrectItsEditor() = runBlocking {
        val preferences = prefs()
        val database = db()
        try {
            val repo = LocalWorkspaceRepository(database, preferences)
            val removed = repo.createBook("Remove", "", listOf("Drama"), 0, 80000, true)
            val kept = repo.createBook("Keep", "", listOf("Fantasy"), 0, 80000, true)
            val entry = database.dao().entries(removed.bookId).first { it.kind == "Chapter" }
            database.dao().updateEntry(entry.copy(body = "uniquedeletiontoken"))
            database.dao().addRevision(RevisionEntity(entryId = entry.id, title = "Earlier", body = "Earlier text"))
            database.dao().ensureDay(removed.bookId, "2026-10-22")
            database.dao().recordWriting(removed.bookId, "2026-10-22", 12)
            preferences.rememberBook(removed.bookId)
            preferences.bookmark(entry.id, true)
            repo.deleteEntry(entry.id)
            repo.deleteBook(removed.bookId)
            assertNull(database.dao().bookRecord(removed.bookId))
            assertTrue(database.dao().workspaceEntryIds(removed.bookId).isEmpty())
            assertTrue(database.dao().revisions(entry.id).isEmpty())
            assertTrue(database.dao().writingDays().first().isEmpty())
            assertTrue(database.dao().search(searchExpression("uniquedeletiontoken")).isEmpty())
            assertFalse(preferences.bookmark(entry.id))
            assertEquals("", preferences.lastBook())
            assertEquals(18, database.dao().entries(kept.bookId).size)
            val writer = LocalWritingRepository(database)
            try {
                writer.save(Entry(id = entry.id, bookId = removed.bookId, title = entry.title, body = "late autosave"), null)
                fail("A deleted workspace must not be resurrected")
            } catch (_: IllegalStateException) { }
        } finally { database.close() }
    }

    @Test fun guidesAndNovelProfilesRoundTripThroughBackup() {
        val book = Book(title = "A story")
        val file = Entry(id = 1, bookId = book.id, title = "README", kind = "Guide", body = folderGuide(defaultStarterFolders.first(), book))
        assertEquals(file.body, BackupCodec.decode(BackupCodec.encode(book, listOf(file))).second.single().body)
        val profile = file.copy(kind = "Novel Profile")
        assertEquals("Novel Profile", BackupCodec.decode(BackupCodec.encode(book, listOf(profile))).second.single().kind)
    }

    @Test(expected = IllegalArgumentException::class)
    fun customizedFoldersCannotDuplicateDisplayNames() {
        validateStarterFolders(listOf(StarterFolder("a", "World"), StarterFolder("b", " world ")))
    }
}
