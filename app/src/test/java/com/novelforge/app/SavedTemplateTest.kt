package com.novelforge.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.data.local.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SavedTemplateTest {
    @Test fun nestedTemplatePersistsAndCreatesIndependentEmptyDocuments() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("preferences", 0).edit().clear().commit()
        val db = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        try {
            val prefs = LocalPreferencesRepository(context)
            val workspace = LocalWorkspaceRepository(db, prefs)
            val source = workspace.createBook("Source", "", emptyList(), 0, 0, false).bookId
            val folder = workspace.createNode(source, "Custom part", true, MANUSCRIPT_AREA, null)
            val nested = workspace.createNode(source, "Nested", true, MANUSCRIPT_AREA, folder)
            val document = workspace.createNode(source, "Opening", false, MANUSCRIPT_AREA, nested, "My scene")
            db.dao().updateEntry(db.dao().entry(document)!!.copy(body = "PRIVATE PROSE"))
            val template = templateFromStructure("custom-test", "My structure", db.dao().summaries().first(), true).copy(fields = listOf(FieldDefinition("mood", "Mood", FieldType.TEXT)))
            prefs.update(prefs.state.value.copy(savedTemplates = listOf(template)))
            val reopened = LocalPreferencesRepository(context)
            assertEquals(template, reopened.state.value.savedTemplates.single())
            val restoredWorkspace = LocalWorkspaceRepository(db, reopened)
            val newProject = restoredWorkspace.createBook("New project", "", emptyList(), 0, 0, true, template.id).bookId
            assertEquals(template.fields, db.dao().bookRecord(newProject)!!.toModel().fields)
            val entries = db.dao().entries(newProject)
            val copy = entries.first { it.title == "Opening" }
            assertNotEquals(document, copy.id)
            assertEquals("", copy.body)
            assertEquals("My scene", copy.kind)
            assertEquals(MANUSCRIPT_AREA, copy.area)
            assertEquals("Nested", entries.first { it.id == copy.folderId }.title)
            assertEquals("PRIVATE PROSE", db.dao().entry(document)!!.body)
            reopened.update(reopened.state.value.copy(savedTemplates = emptyList()))
            assertEquals(3, db.dao().entries(newProject).size)
        } finally { db.close() }
    }

    @Test(expected = IllegalArgumentException::class) fun rejectsCyclicTemplateBeforePersistence() {
        validateSavedTemplate(SavedTemplate("bad", "Bad", listOf(TemplateNode("a", "b", "A", "Folder", PLAN_AREA, 0), TemplateNode("b", "a", "B", "Folder", PLAN_AREA, 1))))
    }
}
