package com.novelforge.app

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.domain.analytics.*
import com.novelforge.domain.workspace.*
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
        val db =
            helper.runMigrationsAndValidate("migration-test", 3, true, MIGRATION_1_2, MIGRATION_2_3)
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
        db.query("SELECT name FROM BookGenre WHERE bookId='old'").use {
            Assert.assertTrue(it.moveToFirst())
            Assert.assertEquals("Fantasy", it.getString(0))
        }
        db.close()
    }

    @Test
    fun preservesV2SynopsisAndLegacyGenre() {
        val synopsis = "A very long legacy synopsis. ".repeat(200)
        helper.createDatabase("migration-v2", 2).apply {
            execSQL(
                "INSERT INTO Book(id,title,author,genre,synopsis,cover,target,status,updated) VALUES ('legacy','Old','','Historical, literary',?,0,80000,'In progress',0)",
                arrayOf(synopsis),
            )
            close()
        }
        val db = helper.runMigrationsAndValidate("migration-v2", 3, true, MIGRATION_2_3)
        db.query("SELECT synopsis FROM Book WHERE id='legacy'").use {
            Assert.assertTrue(it.moveToFirst())
            Assert.assertEquals(synopsis, it.getString(0))
        }
        db.query("SELECT name FROM BookGenre WHERE bookId='legacy'").use {
            Assert.assertTrue(it.moveToFirst())
            Assert.assertEquals("Historical, literary", it.getString(0))
        }
        db.close()
    }
    @Test
    fun v3ChaptersAndOverviewKeepStableIdentity() = kotlinx.coroutines.runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        helper.createDatabase("migration-v3", 3).apply {
            execSQL("INSERT INTO Book(id,title,author,genre,synopsis,cover,target,status,updated) VALUES ('v3','Existing','','Fantasy','Kept',0,80000,'In progress',0)")
            execSQL("INSERT INTO Entry(rowid,bookId,title,body,kind,status,parentId,position,words,updated,deleted,folderId) VALUES (17,'v3','Later','Words','Chapter','Draft',NULL,4,1,0,0,NULL)")
            execSQL("INSERT INTO Entry(rowid,bookId,title,body,kind,status,parentId,position,words,updated,deleted,folderId) VALUES (18,'v3','Earlier','More words','Chapter','Draft',NULL,2,2,0,0,NULL)")
            execSQL("INSERT INTO Entry(rowid,bookId,title,body,kind,status,parentId,position,words,updated,deleted,folderId) VALUES (19,'v3','Renamed reference','## Novel profile','Guide','Draft',NULL,0,2,0,0,NULL)")
            close()
        }
        val db = androidx.room.Room.databaseBuilder(context, ForgeDb::class.java, "migration-v3").addMigrations(MIGRATION_3_4, MIGRATION_4_5).build()
        try {
            Assert.assertEquals(listOf(18L,17L), db.dao().manuscriptPlacements("v3").map { it.documentId })
            Assert.assertEquals(19L, db.dao().book("v3")!!.overviewId)
            Assert.assertEquals("Words", db.dao().entry(17)!!.body)
            Assert.assertEquals("Kept", db.dao().book("v3")!!.synopsis)
        } finally { db.close() }
    }

}
