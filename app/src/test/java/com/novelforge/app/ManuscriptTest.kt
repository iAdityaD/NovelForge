package com.novelforge.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.data.local.*
import com.novelforge.domain.manuscript.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ManuscriptTest {
    @Test fun pickerAddsOnlyExistingChaptersAtomicallyWithoutDuplicates() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("arrange-picker-test")
        var db = Room.databaseBuilder(context, ForgeDb::class.java, "arrange-picker-test").build()
        try {
            val repo = LocalWorkspaceRepository(db, LocalPreferencesRepository(context))
            val book = repo.createBook("Picker", "", emptyList(), 0, 0, false)
            val first = repo.createEntry(book.bookId, "Same title", "Chapter", null, null)
            val second = repo.createEntry(book.bookId, "Same title", "Chapter", null, null)
            val note = repo.createEntry(book.bookId, "Reference", "Note", null, null)
            val foreign = repo.createBook("Other", "", emptyList(), 0, 0, false)
            val foreignChapter = repo.createEntry(foreign.bookId, "Foreign", "Chapter", null, null)
            val manuscript = LocalManuscriptRepository(db)
            assertTrue(manuscriptChapters(db.dao().summaries().first().filter { it.bookId == book.bookId }, manuscript.placements.first()).isEmpty())
            for (bad in listOf(note, foreignChapter)) {
                try { manuscript.add(book.bookId, listOf(first, bad)); fail("Expected rejection") }
                catch (_: IllegalArgumentException) { }
                assertFalse(manuscript.placements.first().first { it.documentId == first }.included)
            }
            manuscript.add(book.bookId, listOf(second, first, second))
            manuscript.add(book.bookId, listOf(first))
            fun order(rows: List<com.novelforge.core.model.EntrySummary>, positions: List<ManuscriptPlacement>) = manuscriptChapters(rows.filter { it.bookId == book.bookId }, positions).map { it.id }
            assertEquals(listOf(second, first), order(db.dao().summaries().first(), manuscript.placements.first()))
            val old = db.dao().entry(second)!!
            manuscript.include(second, false)
            assertEquals(old.body, db.dao().entry(second)!!.body)
            assertEquals(old.folderId, db.dao().entry(second)!!.folderId)
            manuscript.add(book.bookId, listOf(second))
            assertEquals(listOf(first, second), order(db.dao().summaries().first(), manuscript.placements.first()))
            db.close()
            db = Room.databaseBuilder(context, ForgeDb::class.java, "arrange-picker-test").build()
            assertEquals(listOf(first, second), order(db.dao().summaries().first(), LocalManuscriptRepository(db).placements.first()))
        } finally { db.close(); context.deleteDatabase("arrange-picker-test") }
    }

    @Test fun manuscriptOrderSurvivesFolderMovesReopenAndUnplacing() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("manuscript-test")
        context.getSharedPreferences("preferences", 0).edit().clear().commit()
        var db = Room.databaseBuilder(context, ForgeDb::class.java, "manuscript-test").build()
        val repo = LocalWorkspaceRepository(db, LocalPreferencesRepository(context))
        val book = repo.createBook("Order", "", emptyList(), 0, 80000, true)
        val chapters = db.dao().entries(book.bookId).filter { it.kind == "Chapter" }
        val first = chapters[0]; val second = chapters[1]
        val manuscript = LocalManuscriptRepository(db)
        manuscript.move(second.id, -1)
        repo.relocate(second.id, null)
        repo.renameEntry(second.id, "Renamed chapter")
        repo.moveEntry(first.id, 1)
        assertEquals(listOf(second.id, first.id), manuscriptChapters(db.dao().summaries().first(), manuscript.placements.first()).map { it.id })
        manuscript.include(second.id, false)
        assertEquals(listOf(first.id), manuscriptChapters(db.dao().summaries().first(), manuscript.placements.first()).map { it.id })
        assertEquals(second.body, db.dao().entry(second.id)!!.body)
        db.close()
        db = Room.databaseBuilder(context, ForgeDb::class.java, "manuscript-test").build()
        try {
            val reopened = LocalManuscriptRepository(db)
            assertFalse(reopened.placements.first().first { it.documentId == second.id }.included)
            reopened.include(second.id, true)
            assertEquals(listOf(first.id, second.id), manuscriptChapters(db.dao().summaries().first(), reopened.placements.first()).map { it.id })
            val newWorkspace = LocalWorkspaceRepository(db, LocalPreferencesRepository(context))
            newWorkspace.deleteEntry(first.id)
            assertEquals(listOf(second.id), manuscriptChapters(db.dao().summaries().first(), reopened.placements.first()).map { it.id })
            newWorkspace.restoreEntry(first.id)
            assertEquals(listOf(first.id, second.id), manuscriptChapters(db.dao().summaries().first(), reopened.placements.first()).map { it.id })
        } finally { db.close(); context.deleteDatabase("manuscript-test") }
    }

    @Test fun backupPreservesPlacementAndExportOmitsUnplacedMaterial() {
        val book = Book(title = "Order")
        val entries = listOf(Entry(id = 1, bookId = book.id, title = "First", body = "Keep"), Entry(id = 2, bookId = book.id, title = "Second", body = "Not included"))
        val positions = mapOf(1L to (2 to true), 2L to (0 to false))
        val encoded = BackupCodec.encode(book, entries, positions)
        assertEquals(positions, BackupCodec.manuscript(encoded))
        assertEquals(entries.map { it.body }, BackupCodec.decode(encoded).second.map { it.body })
        val legacy = org.json.JSONObject(encoded).put("version", 3).toString()
        assertTrue(BackupCodec.manuscript(legacy).isEmpty())
    }
    @Test fun portableRestoreRemapsOverviewAndManuscriptWithoutLosingExcludedChapters() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("preferences", 0).edit().clear().commit()
        val db = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        val file = java.io.File(context.cacheDir, "manuscript-backup.json")
        val markdown = java.io.File(context.cacheDir, "manuscript.md")
        try {
            val workspace = LocalWorkspaceRepository(db, LocalPreferencesRepository(context))
            val original = workspace.createBook("Original", "", emptyList(), 0, 80000, true)
            val chapters = db.dao().entries(original.bookId).filter { it.kind == "Chapter" }
            db.dao().updateEntry(chapters[0].copy(body = "FIRST_CONTENT"))
            db.dao().updateEntry(chapters[1].copy(body = "EXCLUDED_CONTENT"))
            LocalManuscriptRepository(db).include(chapters[1].id, false)
            val backup = LocalBackupRepository(context, db)
            backup.export(android.net.Uri.fromFile(file).toString(), original.bookId, false)
            val restored = backup.restore(android.net.Uri.fromFile(file).toString())
            val restoredBook = db.dao().book(restored)!!
            assertNotEquals(original.overviewId, restoredBook.overviewId)
            assertEquals(restored, db.dao().entry(restoredBook.overviewId!!)!!.bookId)
            assertEquals(2, db.dao().manuscriptPlacements(restored).size)
            assertEquals(1, db.dao().manuscriptPlacements(restored).count { !it.included })
            backup.export(android.net.Uri.fromFile(markdown).toString(), restored, true)
            assertTrue(markdown.readText().contains("FIRST_CONTENT"))
            assertFalse(markdown.readText().contains("EXCLUDED_CONTENT"))
            assertTrue(db.dao().entries(restored).any { it.body == "EXCLUDED_CONTENT" })
        } finally { db.close(); file.delete(); markdown.delete() }
    }

}
