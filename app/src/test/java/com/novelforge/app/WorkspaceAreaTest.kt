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
class WorkspaceAreaTest {
    @Test fun customTypesNestedOrderAndPortableRolesSurviveRestart() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("area-test")
        var db = Room.databaseBuilder(context, ForgeDb::class.java, "area-test").build()
        val preferences = LocalPreferencesRepository(context)
        val file = java.io.File(context.cacheDir, "roles.json")
        val output = java.io.File(context.cacheDir, "roles.md")
        try {
            val repo = LocalWorkspaceRepository(db, preferences)
            val project = repo.createBook("Blank", "", emptyList(), 0, 0, false).bookId
            val group = repo.createNode(project, "Volume A", true, MANUSCRIPT_AREA, null, "", null)
            val nested = repo.createNode(project, "Movement", true, MANUSCRIPT_AREA, group, "", null)
            val first = repo.createNode(project, "Opening", false, MANUSCRIPT_AREA, nested, "Verse", null)
            val second = repo.createNode(project, "Ending", false, MANUSCRIPT_AREA, nested, "Reflection", null)
            val notes = repo.createNode(project, "Sources", true, PLAN_AREA, null, "", null)
            val source = repo.createNode(project, "Reference", false, PLAN_AREA, notes, "My evidence", "https://example.org/article")
            db.dao().updateEntry(db.dao().entry(first)!!.copy(body = "FIRST [[${source}|Reference]]", status = "My review"))
            db.dao().place(ManuscriptPlacementEntity(first, project, 0, false)) // Historical placement survives custom labels.
            db.dao().updateEntry(db.dao().entry(second)!!.copy(body = "SECOND"))
            fun order(rows: List<EntrySummary>) = orderedManuscript(rows.filter { it.bookId == project }).map { it.id }
            assertEquals(listOf(first, second), order(db.dao().summaries().first()))
            repo.placeBefore(second, first)
            assertEquals(listOf(second, first), order(db.dao().summaries().first()))
            repo.placeBefore(second, first, true)
            assertEquals(listOf(first, second), order(db.dao().summaries().first()))
            assertTrue(runCatching { repo.placeBefore(group, first) }.isFailure)
            repo.placeBefore(source, first)
            assertEquals(PLAN_AREA, db.dao().entry(source)!!.area)
            repo.renameEntry(group, "My own structure")
            repo.renameEntry(first, "Renamed")
            val before = db.dao().entry(first)!!.body
            db.close()
            db = Room.databaseBuilder(context, ForgeDb::class.java, "area-test").build()
            assertEquals(listOf(first, second), order(db.dao().summaries().first()))
            assertEquals(before, db.dao().entry(first)!!.body)
            val backups = LocalBackupRepository(context, db)
            backups.export(android.net.Uri.fromFile(file).toString(), project, false)
            val restored = backups.restore(android.net.Uri.fromFile(file).toString())
            val restoredEntries = db.dao().entries(restored)
            assertEquals("Verse", restoredEntries.first { it.title == "Renamed" }.kind)
            assertEquals("My review", restoredEntries.first { it.title == "Renamed" }.status)
            assertEquals(PLAN_AREA, restoredEntries.first { it.title == "Reference" }.area)
            backups.export(android.net.Uri.fromFile(output).toString(), restored, true)
            assertTrue(output.readText().indexOf("FIRST") < output.readText().indexOf("SECOND"))
            assertFalse(output.readText().contains("https://example.org"))
            assertEquals(1, areaEntries(db.dao().summaries().first().filter { it.bookId == restored }, PLAN_AREA).count { it.kind != "Folder" })
        } finally { db.close(); context.deleteDatabase("area-test"); file.delete(); output.delete() }
    }

    @Test fun creationRecipesUseSameDocumentModelAndBlankStaysEmpty() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        try {
            val repo = LocalWorkspaceRepository(db, LocalPreferencesRepository(context))
            for (recipe in com.novelforge.domain.workspace.projectRecipes) {
                val id = repo.createBook(recipe.name, "", emptyList(), 0, 0, true, recipe.id).bookId
                assertEquals(recipe.draftTitles.size, orderedManuscript(db.dao().summaries().first().filter { it.bookId == id }).size)
            }
            val blank = repo.createBook("Blank", "", emptyList(), 0, 0, false, "screenplay").bookId
            assertTrue(db.dao().entries(blank).isEmpty())
        } finally { db.close() }
    }
}
