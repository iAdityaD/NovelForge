package com.novelforge.app

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.novelforge.app.data.*
import com.novelforge.data.local.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecoveryBackupTest {
    @Test fun recoveryRestoresTrashRevisionsLinksAndActivityIntoFreshDatabase() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val source = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        val target = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        val file = java.io.File(context.cacheDir, "recovery-test.json")
        try {
            val repo = LocalWorkspaceRepository(source, LocalPreferencesRepository(context))
            val book = repo.createBook("Recovery", "", emptyList(), 0, 80000, true)
            val chapters = source.dao().entries(book.bookId).filter { it.kind == "Chapter" }
            val first = chapters[0]; val second = chapters[1]
            val text = "ప్రపంచం [[${second.id}|Second chapter]]"
            source.dao().updateEntry(first.copy(body = text, updated = 123456L))
            source.dao().addRevision(RevisionEntity(entryId = first.id, title = "Earlier draft", body = text, created = 1000L))
            source.dao().addWritingDays(listOf(WritingDayEntity(book.bookId, "2026-10-23", 123, 7)))
            repo.deleteEntry(second.id)
            LocalBackupRepository(context, source).export(Uri.fromFile(file).toString(), book.bookId, false)
            assertEquals(7, JSONObject(file.readText()).getInt("version"))
            val restoredId = LocalBackupRepository(context, target).restore(Uri.fromFile(file).toString())
            val restored = target.dao().allEntries(restoredId)
            val restoredFirst = restored.single { it.title == first.title }
            val restoredSecond = restored.single { it.title == second.title }
            assertTrue(restoredSecond.deleted)
            assertFalse(target.dao().entries(restoredId).any { it.id == restoredSecond.id })
            assertEquals(123456L, restoredFirst.updated)
            assertEquals("ప్రపంచం [[${restoredSecond.id}|Second chapter]]", restoredFirst.body)
            val revision = target.dao().revisions(restoredFirst.id).single()
            assertEquals(restoredFirst.body, revision.body)
            assertEquals(1000L, revision.created)
            assertEquals(WritingDayEntity(restoredId, "2026-10-23", 123, 7), target.dao().workspaceDays(restoredId).single())
            assertEquals(2, target.dao().manuscriptPlacements(restoredId).size)
            LocalWorkspaceRepository(target, LocalPreferencesRepository(context)).restoreEntry(restoredSecond.id)
            assertFalse(target.dao().entry(restoredSecond.id)!!.deleted)
            assertTrue(source.dao().entry(second.id)!!.deleted)
        } finally { source.close(); target.close(); file.delete() }
    }

    @Test fun invalidHistoryFailsBeforeCreatingAnyProject() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        val file = java.io.File(context.cacheDir, "bad-recovery-test.json")
        try {
            val book = com.novelforge.core.model.Book(title = "Bad")
            val text = BackupCodec.encode(book, emptyList(), recovery = RecoveryHistory(
                revisions = listOf(com.novelforge.core.model.Revision(entryId = 999, title = "Missing", body = "Do not partially restore"))))
            file.writeText(text)
            try { LocalBackupRepository(context, db).restore(Uri.fromFile(file).toString()); fail("Expected invalid reference rejection") }
            catch (_: IllegalArgumentException) { }
            assertEquals(0, db.dao().bookCount())
        } finally { db.close(); file.delete() }
    }
}
