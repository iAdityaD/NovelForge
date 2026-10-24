package com.novelforge.data.local

import android.content.Context
import android.util.AtomicFile
import com.novelforge.core.model.Entry
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal data class JournalDraft(val entry: Entry, val baseFingerprint: String)

/** One atomically replaced recovery file per edited document, outside Android's cache. */
@Singleton
class LocalDraftJournal @Inject constructor(@ApplicationContext context: Context) {
    private val directory = File(context.filesDir, "writing-recovery")
    private fun file(id: Long) = AtomicFile(File(directory, "$id.json"))

    internal suspend fun write(entry: Entry, previous: Entry?) = withContext(Dispatchers.IO) {
        check(directory.exists() || directory.mkdirs()) { "Recovery storage is unavailable." }
        val json = JSONObject().apply {
            put("version", 1); put("id", entry.id); put("bookId", entry.bookId)
            put("title", entry.title); put("body", entry.body); put("kind", entry.kind); put("status", entry.status)
            put("base", previous?.let(::fingerprint) ?: "")
        }.toString().toByteArray(Charsets.UTF_8)
        val target = file(entry.id)
        val stream = target.startWrite()
        try { stream.write(json); target.finishWrite(stream) }
        catch (e: Exception) { target.failWrite(stream); throw e }
    }

    internal suspend fun read(saved: Entry): JournalDraft? = withContext(Dispatchers.IO) {
        val target = file(saved.id)
        if (!target.baseFile.exists() && !File(target.baseFile.path + ".bak").exists()) return@withContext null
        val json = JSONObject(target.readFully().toString(Charsets.UTF_8))
        require(json.getInt("version") == 1 && json.getLong("id") == saved.id && json.getString("bookId") == saved.bookId) { "Invalid recovery record." }
        JournalDraft(saved.copy(title = json.getString("title"), body = json.getString("body"), kind = json.getString("kind"), status = json.getString("status")), json.getString("base"))
    }

    internal suspend fun clear(id: Long) = withContext(Dispatchers.IO) { file(id).delete() }

    internal fun fingerprint(entry: Entry): String {
        val value = JSONObject().apply { put("title", entry.title); put("body", entry.body); put("kind", entry.kind); put("status", entry.status) }.toString()
        return MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
