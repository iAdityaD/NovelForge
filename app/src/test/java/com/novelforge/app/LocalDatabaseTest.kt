package com.novelforge.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.domain.analytics.*
import com.novelforge.domain.workspace.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalDatabaseTest {
    @Test
    fun ftsTracksEditsAndTrashWithoutDuplicatingPages() = runBlocking {
        val db =
            Room.inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    ForgeDb::class.java,
                )
                .build()
        try {
            val dao = db.dao()
            val book = BookEntity(title = "Test")
            dao.addBook(book)
            val id =
                dao.addEntry(
                    EntryEntity(bookId = book.id, title = "Dawn", body = "A forgotten ocean")
                )
            assertEquals(id, dao.search(searchExpression("ocean")).single().id)
            val entry = dao.entry(id)!!
            dao.updateEntry(entry.copy(body = "A remembered forest"))
            assertTrue(dao.search(searchExpression("ocean")).isEmpty())
            assertEquals(id, dao.search(searchExpression("forest")).single().id)
            dao.setDeleted(id, true)
            assertTrue(dao.search(searchExpression("forest")).isEmpty())
            dao.setDeleted(id, false)
            assertEquals(id, dao.search(searchExpression("forest")).single().id)
        } finally {
            db.close()
        }
    }
}
