package com.novelforge.app.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
                listOf("01_Chapters", "02_Characters", "03_Lore", "04_Plot", "05_Metadata")
                    .forEachIndexed { i, name ->
                        db.execSQL(
                            "INSERT INTO Entry(bookId,title,body,kind,status,parentId,position,words,updated,deleted,folderId) VALUES (?,?,'','Folder','Draft',NULL,?,0,0,0,NULL)",
                            arrayOf(book, name, i),
                        )
                        db.query("SELECT last_insert_rowid()").use {
                            it.moveToFirst()
                            ids[name] = it.getLong(0)
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
                        arrayOf(ids.getValue(legacyFolder(kind)), id),
                    )
                }
            }
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS BookGenre (bookId TEXT NOT NULL, name TEXT NOT NULL, position INTEGER NOT NULL, PRIMARY KEY(bookId,name), FOREIGN KEY(bookId) REFERENCES Book(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
            )
            // Preserve legacy text as one genre; commas may be intentional parts of a custom genre.
            db.execSQL(
                "INSERT INTO BookGenre(bookId,name,position) SELECT id,trim(genre),0 FROM Book WHERE trim(genre)!=''"
            )
        }
    }

private fun legacyFolder(kind: String) =
    when (kind) {
        "Chapter",
        "Screenplay" -> "01_Chapters"
        "Character",
        "Backstory" -> "02_Characters"
        "Location",
        "Worldbuilding",
        "Research" -> "03_Lore"
        "Arc",
        "Plot thread",
        "Timeline event" -> "04_Plot"
        else -> "05_Metadata"
    }

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Book ADD COLUMN overviewId INTEGER")
        // Recover the original overview guide by its generated profile marker, even in a renamed folder.
        db.execSQL("UPDATE Book SET overviewId=(SELECT MIN(rowid) FROM Entry WHERE Entry.bookId=Book.id AND deleted=0 AND (kind='Novel Profile' OR (kind='Guide' AND body LIKE '%## Novel profile%')))")
        db.execSQL("CREATE TABLE IF NOT EXISTS ManuscriptPlacement (documentId INTEGER NOT NULL, bookId TEXT NOT NULL, position INTEGER NOT NULL, included INTEGER NOT NULL, PRIMARY KEY(documentId), FOREIGN KEY(documentId) REFERENCES Entry(rowid) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(bookId) REFERENCES Book(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_ManuscriptPlacement_bookId ON ManuscriptPlacement(bookId)")
        db.execSQL("INSERT INTO ManuscriptPlacement(documentId,bookId,position,included) SELECT rowid,bookId,position,1 FROM Entry WHERE kind='Chapter'")
    }
}
