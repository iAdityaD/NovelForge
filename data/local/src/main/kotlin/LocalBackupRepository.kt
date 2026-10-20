package com.novelforge.data.local

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.domain.analytics.*
import com.novelforge.domain.backup.*
import com.novelforge.domain.preferences.*
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*
import com.novelforge.domain.writing.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class LocalBackupRepository
@Inject
constructor(@ApplicationContext private val app: Context, private val db: ForgeDb) :
    BackupRepository {
    private val dao = Records(db.dao())

    override suspend fun export(destination: String, bookId: String, markdown: Boolean) =
        withContext(Dispatchers.IO) {
            val book = requireNotNull(dao.book(bookId))
            val entries = dao.entries(bookId)
            val text =
                if (markdown) BackupCodec.markdown(book, entries)
                else BackupCodec.encode(book, entries)
            require(text.toByteArray().size <= BackupCodec.MAX_BYTES) {
                "Project exceeds the 16 MB backup limit."
            }
            requireNotNull(app.contentResolver.openOutputStream(Uri.parse(destination), "wt"))
                .bufferedWriter(Charsets.UTF_8)
                .use { it.write(text) }
        }

    override suspend fun restore(source: String): String =
        withContext(Dispatchers.IO) {
            val text =
                requireNotNull(app.contentResolver.openInputStream(Uri.parse(source))).use { input
                    ->
                    val out = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        require(out.size() + count <= BackupCodec.MAX_BYTES) {
                            "Backup exceeds 16 MB."
                        }
                        out.write(buffer, 0, count)
                    }
                    out.toByteArray().toString(Charsets.UTF_8)
                }
            val (book, entries) = BackupCodec.decode(text)
            db.withTransaction {
                dao.addBook(book)
                val ids = mutableMapOf<Long, Long>()
                entries.forEach {
                    ids[it.id] = dao.addEntry(it.copy(id = 0, parentId = null, folderId = null))
                }
                entries.forEach { e ->
                    val body = remapLinks(e.body, ids)
                    dao.updateEntry(
                        e.copy(
                            id = ids.getValue(e.id),
                            parentId = ids[e.parentId],
                            folderId = ids[e.folderId],
                            body = body,
                            words = wordCount(body),
                        )
                    )
                }
            }
            book.id
        }
}
