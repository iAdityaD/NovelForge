package com.novelforge.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.data.local.*
import kotlinx.coroutines.*
import com.novelforge.domain.writing.EditorSession
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DraftRecoveryTest {
    @Test fun interruptedDraftSurvivesDatabaseAndRepositoryRestart() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("recovery-journal-test")
        java.io.File(context.filesDir, "writing-recovery").deleteRecursively()
        var db = Room.databaseBuilder(context, ForgeDb::class.java, "recovery-journal-test").build()
        try {
            val workspace = LocalWorkspaceRepository(db, LocalPreferencesRepository(context))
            val project = workspace.createBook("Recovery", "", emptyList(), 0, 0, false).bookId
            val id = workspace.createNode(project, "Draft", false, MANUSCRIPT_AREA, null)
            val saved = db.dao().entry(id)!!.toModel()
            val draft = saved.copy(body = "An interrupted sentence ప్రపంచం", title = "Changed title", kind = "My type", status = "Needs research")
            LocalWritingRepository(db, LocalDraftJournal(context)).stageDraft(draft, saved)
            assertEquals("", db.dao().entry(id)!!.body)
            db.close()
            db = Room.databaseBuilder(context, ForgeDb::class.java, "recovery-journal-test").build()
            val writer = LocalWritingRepository(db, LocalDraftJournal(context))
            val recovered = writer.recoverDraft(db.dao().entry(id)!!.toModel())!!
            assertEquals(draft.body, recovered.body)
            assertEquals("Changed title", recovered.title)
            assertEquals("My type", recovered.kind)
            assertEquals("Needs research", recovered.status)
            writer.save(recovered, saved)
            assertNull(writer.recoverDraft(db.dao().entry(id)!!.toModel()))
            assertFalse(java.io.File(context.filesDir, "writing-recovery/$id.json").exists())
        } finally { db.close(); context.deleteDatabase("recovery-journal-test") }
    }

    @Test fun conflictingSavedTextIsNeverOverwrittenAndRecoveryDoesNotDuplicate() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        java.io.File(context.filesDir, "writing-recovery").deleteRecursively()
        val db = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        try {
            val workspace = LocalWorkspaceRepository(db, LocalPreferencesRepository(context))
            val project = workspace.createBook("Conflict", "", emptyList(), 0, 0, false).bookId
            val id = workspace.createNode(project, "Original", false, PLAN_AREA, null)
            val saved = db.dao().entry(id)!!.toModel()
            val writer = LocalWritingRepository(db, LocalDraftJournal(context))
            writer.stageDraft(saved.copy(body = "Interrupted text"), saved)
            db.dao().updateEntry(db.dao().entry(id)!!.copy(body = "Newer saved text"))
            val recovered = writer.recoverDraft(db.dao().entry(id)!!.toModel())!!
            assertNotEquals(id, recovered.id)
            assertEquals("Interrupted text", recovered.body)
            assertEquals(PLAN_AREA, recovered.area)
            assertEquals("Newer saved text", db.dao().entry(id)!!.body)
            assertNull(writer.recoverDraft(db.dao().entry(id)!!.toModel()))
            assertEquals(2, db.dao().entries(project).size)
        } finally { db.close() }
    }
    @Test fun untitledDraftIsPrivateRecoverableAndCommittedOnlyOnce() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        java.io.File(context.filesDir, "writing-recovery").deleteRecursively()
        val name = "untitled-restart"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context, ForgeDb::class.java, name).build()
        try {
            val book = LocalWorkspaceRepository(db, LocalPreferencesRepository(context)).createBook("Blank", "", emptyList(), 0, 0, false).bookId
            val draft = Entry(bookId = book, title = "Untitled", body = "An unsaved world", kind = "Document", metadata = mapOf("_draftToken" to listOf("test-token")))
            LocalWritingRepository(db, LocalDraftJournal(context)).stageUntitled(draft)
            assertTrue(db.dao().entries(book).isEmpty())
            db.close()
            db = Room.databaseBuilder(context, ForgeDb::class.java, name).build()
            val repository = LocalWritingRepository(db, LocalDraftJournal(context))
            assertEquals(draft.body, repository.untitled(book)!!.body)
            try { repository.commitUntitled(draft, "", null, "Parts"); fail("Empty title accepted") } catch (_: IllegalArgumentException) { }
            assertTrue(db.dao().entries(book).isEmpty())
            val saved = repository.commitUntitled(draft, "First chapter", null, "Parts")
            assertTrue(saved.id > 0)
            assertNotNull(saved.folderId)
            assertEquals(draft.body, saved.body)
            assertNull(repository.untitled(book))
            assertEquals(saved.id, repository.commitUntitled(draft, "First chapter", null, "Parts").id)
            assertEquals(2, db.dao().entries(book).size)
            repository.stageUntitled(draft.copy(metadata = mapOf("_draftToken" to listOf("discard-me"))))
            repository.discardUntitled(book)
            assertNull(repository.untitled(book))
            assertEquals(draft.body, repository.entry(saved.id)!!.body)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun editorSessionKeepsDraftUntilExplicitSaveAndPreservesFailedSave() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        java.io.File(context.filesDir, "writing-recovery").deleteRecursively()
        val db = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val book = LocalWorkspaceRepository(db, LocalPreferencesRepository(context)).createBook("Session", "", emptyList(), 0, 0, false).bookId
            val repository = LocalWritingRepository(db, LocalDraftJournal(context))
            val session = EditorSession(repository, scope)
            assertTrue(session.openUntitled(book))
            session.body("The owl returned.")
            assertTrue(session.flush())
            assertTrue(db.dao().entries(book).isEmpty())
            assertEquals("The owl returned.", repository.untitled(book)!!.body)
            try { session.saveUntitled("Chapter", 99999L, null); fail("Missing folder accepted") } catch (_: IllegalArgumentException) { }
            assertEquals(0L, session.state.value.entry!!.id)
            assertFalse(session.state.value.savingAs)
            assertEquals("The owl returned.", session.state.value.entry!!.body)
            assertTrue(session.saveUntitled("Chapter", null, null))
            val id = session.state.value.entry!!.id
            assertTrue(id > 0)
            session.body("The owl returned at dawn.")
            assertTrue(session.flush())
            assertEquals("The owl returned at dawn.", repository.entry(id)!!.body)
            assertTrue(session.openUntitled(book))
            session.body("Discard only this draft")
            session.discardUntitled()
            assertNull(repository.untitled(book))
            assertEquals("The owl returned at dawn.", repository.entry(id)!!.body)
        } finally { scope.cancel(); db.close() }
    }

}
