package com.novelforge.app

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.novelforge.app.data.*
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            ForgeDb::class.java.canonicalName!!,
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun preservesExistingTextAndOrganizesIntoFolders() {
        helper.createDatabase("migration-test", 1).apply {
            execSQL(
                "INSERT INTO Book(id,title,author,genre,synopsis,cover,target,updated) VALUES ('old','Legacy','','Fantasy','',0,80000,0)"
            )
            execSQL(
                "INSERT INTO Entry(rowid,bookId,title,body,kind,status,parentId,position,words,updated,deleted) VALUES (7,'old','Dawn','An ocean remembers','Chapter','Draft',NULL,0,3,0,0)"
            )
            close()
        }
        val db = helper.runMigrationsAndValidate("migration-test", 2, true, MIGRATION_1_2)
        db.query(
                "SELECT e.body,f.title FROM Entry e JOIN Entry f ON e.folderId=f.rowid WHERE e.rowid=7"
            )
            .use {
                Assert.assertTrue(it.moveToFirst())
                Assert.assertEquals("An ocean remembers", it.getString(0))
                Assert.assertEquals("01_Chapters", it.getString(1))
            }
        db.query("SELECT rowid FROM entry_fts WHERE entry_fts MATCH 'ocean'").use {
            Assert.assertTrue(it.moveToFirst())
            Assert.assertEquals(7L, it.getLong(0))
        }
        db.close()
    }
}
