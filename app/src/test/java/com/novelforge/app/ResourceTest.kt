package com.novelforge.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.novelforge.app.data.*
import com.novelforge.data.local.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ResourceTest {
    @Test fun scratchResourcesRequireCategoriesAndPreserveIdentity() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        try {
            val repo = LocalWorkspaceRepository(db, LocalPreferencesRepository(context))
            val book = repo.createBook("Scratch resources", "", emptyList(), 0, 0, false)
            assertTrue(db.dao().entries(book.bookId).isEmpty())
            suspend fun rejected(folder: Long?, category: String?, url: String?) {
                try { repo.createResource(book.bookId, "Bad", folder, category, url, ""); fail("Expected rejection") }
                catch (_: IllegalArgumentException) { }
            }
            rejected(null, null, null)
            rejected(null, "Articles", "javascript:alert(1)")
            assertTrue(db.dao().entries(book.bookId).isEmpty())
            val id = repo.createResource(book.bookId, "A source", null, "Articles", "https://example.com/article", "My notes")
            val entry = db.dao().entry(id)!!
            assertNotNull(entry.folderId)
            assertEquals("External source", entry.kind)
            assertTrue(entry.body.contains("https://example.com/article"))
            rejected(null, "articles", null)
            val folderId = entry.folderId!!
            repo.renameEntry(folderId, "Research")
            val noteId = repo.createResource(book.bookId, "Another idea", folderId, null, null, "Details")
            assertEquals(folderId, db.dao().entry(noteId)!!.folderId)
            assertEquals(folderId, db.dao().entry(id)!!.folderId)
            val encoded = BackupCodec.encode(com.novelforge.core.model.Book(id = book.bookId, title = "Scratch resources"), db.dao().entries(book.bookId).map { it.toModel() })
            assertEquals(3, BackupCodec.decode(encoded).second.size)
            repo.deleteEntry(folderId)
            rejected(folderId, null, null)
            repo.restoreEntry(folderId)
            assertEquals(entry.body, db.dao().entry(id)!!.body)
        } finally { db.close() }
    }
}
