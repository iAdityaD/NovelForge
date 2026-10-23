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
}
