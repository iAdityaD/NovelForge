package com.novelforge.app

import android.content.Context
import android.net.Uri
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
class CustomFieldsTest {
    @Test fun fieldsSurviveRecoveryBackupAndReferenceRemapping() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        val backup = java.io.File(context.cacheDir, "custom-fields-test.json")
        try {
            val repo = LocalWorkspaceRepository(db, LocalPreferencesRepository(context))
            val id = repo.createBook("Fields", "", emptyList(), 0, 0, false).bookId
            val target = repo.createNode(id, "Source", false, PLAN_AREA, null)
            val document = repo.createNode(id, "Draft", false, MANUSCRIPT_AREA, null)
            val fields = listOf(FieldDefinition("source", "Source", FieldType.REFERENCE), FieldDefinition("mood", "Mood", FieldType.MULTI_CHOICE, listOf("Hope", "Fear")))
            repo.editBook(db.dao().bookRecord(id)!!.toModel().copy(fields = fields))
            val writer = LocalWritingRepository(db, LocalDraftJournal(context))
            val original = writer.entry(document)!!
            val draft = original.copy(metadata = mapOf("source" to listOf(target.toString()), "mood" to listOf("Hope", "Fear")))
            writer.stageDraft(draft, original)
            val recovered = writer.recoverDraft(original)!!
            assertEquals(draft.metadata, recovered.metadata)
            writer.save(recovered, original)
            val service = LocalBackupRepository(context, db)
            service.export(Uri.fromFile(backup).toString(), id, false)
            val restored = service.restore(Uri.fromFile(backup).toString())
            val copies = db.dao().entries(restored).map { it.toModel() }
            assertEquals(listOf(copies.single { it.title == "Source" }.id.toString()), copies.single { it.title == "Draft" }.metadata["source"])
            assertEquals(fields, db.dao().bookRecord(restored)!!.toModel().fields)
            assertTrue(runCatching { repo.editBook(db.dao().bookRecord(id)!!.toModel().copy(fields = fields.map { if (it.id == "mood") it.copy(options = listOf("Hope")) else it })) }.isFailure)
        } finally { db.close(); backup.delete() }
    }
    @Test fun typedOptionalValuesRejectInvalidInput() {
        validateFieldValue(FieldDefinition("date", "Date", FieldType.DATE), emptyList())
        assertTrue(runCatching { validateFieldValue(FieldDefinition("date", "Date", FieldType.DATE), listOf("2026-02-30")) }.isFailure)
        assertTrue(runCatching { validateFieldValue(FieldDefinition("url", "URL", FieldType.URL), listOf("javascript:alert(1)")) }.isFailure)
        validateFieldValue(FieldDefinition("number", "Number", FieldType.NUMBER), listOf("-1.25"))
    }
}
