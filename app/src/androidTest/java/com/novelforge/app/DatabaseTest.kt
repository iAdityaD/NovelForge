package com.novelforge.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.novelforge.app.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
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
            val book = Book(title = "Test")
            dao.addBook(book)
            val id =
                dao.addEntry(Entry(bookId = book.id, title = "Dawn", body = "A forgotten ocean"))
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
