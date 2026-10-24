package com.novelforge.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.data.local.*
import kotlinx.coroutines.runBlocking
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
}
