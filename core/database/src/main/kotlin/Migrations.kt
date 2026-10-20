package com.novelforge.app.data
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.novelforge.domain.workspace.*
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE Book ADD COLUMN status TEXT NOT NULL DEFAULT 'In progress'")
            db.execSQL("ALTER TABLE Entry ADD COLUMN folderId INTEGER")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS WritingDay (bookId TEXT NOT NULL, day TEXT NOT NULL, words INTEGER NOT NULL, edits INTEGER NOT NULL, PRIMARY KEY(bookId,day))"
            )
            val books = mutableListOf<String>()
            db.query("SELECT id FROM Book").use {
                while (it.moveToNext()) books.add(it.getString(0))
            }
            books.forEach { book ->
                val ids = mutableMapOf<String, Long>()
                workspaceTemplates.forEachIndexed { i, t ->
                    db.execSQL(
                        "INSERT INTO Entry(bookId,title,body,kind,status,parentId,position,words,updated,deleted,folderId) VALUES (?,?,'','Folder','Draft',NULL,?,0,0,0,NULL)",
                        arrayOf(book, t.name, i),
                    )
                    db.query("SELECT last_insert_rowid()").use {
                        it.moveToFirst()
                        ids[t.name] = it.getLong(0)
                    }
                }
                val entries = mutableListOf<Pair<Long, String>>()
                db.query(
                        "SELECT rowid,kind FROM Entry WHERE bookId=? AND kind!='Folder'",
                        arrayOf(book),
                    )
                    .use { while (it.moveToNext()) entries.add(it.getLong(0) to it.getString(1)) }
                entries.forEach { (id, kind) ->
                    db.execSQL(
                        "UPDATE Entry SET folderId=? WHERE rowid=?",
                        arrayOf(ids.getValue(defaultFolder(kind)), id),
                    )
                }
            }
        }
    }
