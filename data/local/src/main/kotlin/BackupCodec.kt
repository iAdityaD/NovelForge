package com.novelforge.app.data

import com.novelforge.core.model.*
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*
import org.json.JSONArray
import org.json.JSONObject

/** Versioned portable JSON. Restore always creates a new project; IDs are remapped. */
data class RecoveryHistory(val revisions: List<Revision> = emptyList(), val days: List<WritingDay> = emptyList())

object BackupCodec {
    const val MAX_BYTES = 16 * 1024 * 1024

    fun encode(book: Book, entries: List<Entry>, manuscript: Map<Long, Pair<Int, Boolean>> = emptyMap(), recovery: RecoveryHistory? = null): String =
        JSONObject()
            .apply {
                put("format", "novelforge")
                put("version", if (recovery == null) 4 else 5)
                put(
                    "book",
                    JSONObject().apply {
                        put("title", book.title)
                        put("author", book.author)
                        put("genres", JSONArray(book.genres))
                        put("synopsis", book.synopsis)
                        put("cover", book.cover)
                        put("target", book.target)
                        put("status", book.status)
                        put("overviewId", book.overviewId?.takeIf { id -> entries.any { it.id == id } } ?: JSONObject.NULL)
                    },
                )
                put(
                    "entries",
                    JSONArray().apply {
                        entries.forEach { e ->
                            put(
                                JSONObject().apply {
                                    put("id", e.id)
                                    if (recovery != null) { put("deleted", e.deleted); put("updated", e.updated) }
                                    put("title", e.title)
                                    put("body", e.body)
                                    put("kind", e.kind)
                                    put("status", e.status)
                                    put(
                                        "parentId",
                                        e.parentId?.takeIf { id ->
                                            entries.any { it.id == id && it.kind == "Arc" }
                                        } ?: JSONObject.NULL,
                                    )
                                    put("position", e.position)
                                    manuscript[e.id]?.let { (order, included) ->
                                        put("manuscriptPosition", order)
                                        put("manuscriptIncluded", included)
                                    }
                                    put(
                                        "folderId",
                                        e.folderId?.takeIf { id ->
                                            entries.any { it.id == id && it.kind == "Folder" }
                                        } ?: JSONObject.NULL,
                                    )
                                }
                            )
                        }
                    },
                )
                if (recovery != null) {
                    put("recovery", JSONObject().apply {
                        put("revisions", JSONArray().apply {
                            recovery.revisions.forEach { revision -> put(JSONObject().apply {
                                put("entryId", revision.entryId); put("title", revision.title)
                                put("body", revision.body); put("created", revision.created)
                            }) }
                        })
                        put("days", JSONArray().apply {
                            recovery.days.forEach { day -> put(JSONObject().apply {
                                put("day", day.day); put("words", day.words); put("edits", day.edits)
                            }) }
                        })
                    })
                }
            }
            .toString(2)

    fun decode(text: String): Pair<Book, List<Entry>> {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) { "Backup exceeds 16 MB." }
        val root = JSONObject(text)
        require(root.getString("format") == "novelforge" && root.getInt("version") in 1..5) {
            "Unsupported backup format."
        }
        val b = root.getJSONObject("book")
        val title = b.getString("title").trim()
        require(title.isNotBlank() && title.length <= 200)
        val book =
            Book(
                title = "$title (restored)",
                overviewId = if (b.isNull("overviewId")) null else b.getLong("overviewId"),
                author = b.optString("author"),
                genres =
                    MetadataPolicy.genres(
                        if (root.getInt("version") >= 3) {
                            val values = b.getJSONArray("genres")
                            (0 until values.length()).map { values.getString(it) }
                        } else listOf(b.optString("genre"))
                    ),
                synopsis = b.optString("synopsis"),
                status =
                    b.optString("status", "In progress").takeIf { it in bookStatuses }
                        ?: "In progress",
                cover = b.optInt("cover").coerceIn(0, 3),
                target = b.optInt("target", 80000).coerceIn(1, 10000000),
            )
        val array = root.getJSONArray("entries")
        require(array.length() <= 10000) { "Too many entries." }
        val entries =
            (0 until array.length()).map { i ->
                val e = array.getJSONObject(i)
                val body = e.getString("body")
                val kind = e.getString("kind")
                require(kind in kinds || kind == "Folder")
                require(e.getString("title").isNotBlank())
                require(body.length <= 2000000) { "An entry is too large." }
                Entry(
                    id = e.getLong("id"),
                    bookId = book.id,
                    deleted = if (root.getInt("version") >= 5) e.getBoolean("deleted") else false,
                    updated = if (root.getInt("version") >= 5) e.getLong("updated") else System.currentTimeMillis(),
                    title = e.getString("title"),
                    body = body,
                    kind = kind,
                    status = e.optString("status", "Draft").takeIf { it in statuses } ?: "Draft",
                    parentId = if (e.isNull("parentId")) null else e.getLong("parentId"),
                    position = e.optInt("position"),
                    words = if (kind == "Folder") 0 else wordCount(body),
                    folderId = if (e.isNull("folderId")) null else e.getLong("folderId"),
                )
            }
        require(entries.map { it.id }.distinct().size == entries.size) { "Duplicate entry IDs." }
        val arcs = entries.filter { it.kind == "Arc" }.map { it.id }.toSet()
        require(
            entries.all { it.parentId == null || it.parentId in arcs && it.parentId != it.id }
        ) {
            "Invalid arc reference."
        }
        val folders = entries.filter { it.kind == "Folder" }.associateBy { it.id }
        entries.forEach { e ->
            val visited = mutableSetOf(e.id)
            var parent = e.folderId
            while (parent != null) {
                require(visited.add(parent) && parent in folders) {
                    "Invalid or cyclic folder hierarchy."
                }
                parent = folders.getValue(parent).folderId
            }
        }
        require(book.overviewId == null || entries.any { it.id == book.overviewId && it.kind != "Folder" }) { "Invalid overview reference." }
        return book to entries
    }

    fun recovery(text: String, book: Book, entries: List<Entry>): RecoveryHistory {
        val root = JSONObject(text)
        if (root.getInt("version") < 5) return RecoveryHistory()
        val history = root.getJSONObject("recovery")
        val revisions = history.getJSONArray("revisions")
        val days = history.getJSONArray("days")
        require(revisions.length() <= 200000 && days.length() <= 50000) { "Recovery history is too large." }
        val entryIds = entries.map { it.id }.toSet()
        val restoredRevisions = (0 until revisions.length()).map { index ->
            val r = revisions.getJSONObject(index)
            val id = r.getLong("entryId")
            val body = r.getString("body")
            require(id in entryIds && body.length <= 2000000 && r.getLong("created") >= 0) { "Invalid revision checkpoint." }
            Revision(entryId = id, title = r.getString("title"), body = body, created = r.getLong("created"))
        }
        require(restoredRevisions.groupingBy { it.entryId }.eachCount().values.all { it <= 20 }) { "Too many checkpoints for a document." }
        val restoredDays = (0 until days.length()).map { index ->
            val d = days.getJSONObject(index)
            val date = d.getString("day")
            require(java.time.LocalDate.parse(date).toString() == date && d.getInt("words") >= 0 && d.getInt("edits") >= 0) { "Invalid writing history." }
            WritingDay(book.id, date, d.getInt("words"), d.getInt("edits"))
        }
        require(restoredDays.map { it.day }.distinct().size == restoredDays.size) { "Duplicate writing history dates." }
        return RecoveryHistory(restoredRevisions, restoredDays)
    }

    fun manuscript(text: String): Map<Long, Pair<Int, Boolean>> {
        val root = JSONObject(text)
        if (root.getInt("version") < 4) return emptyMap()
        val entries = root.getJSONArray("entries")
        return buildMap {
            for (i in 0 until entries.length()) {
                val entry = entries.getJSONObject(i)
                if (entry.has("manuscriptPosition")) {
                    require(entry.getString("kind") == "Chapter") { "Only chapters can be placed in a manuscript." }
                    put(entry.getLong("id"), entry.getInt("manuscriptPosition") to entry.getBoolean("manuscriptIncluded"))
                }
            }
        }
    }

    fun markdown(book: Book, entries: List<Entry>): String = buildString {
        append("# ${book.title}\n\n${book.author}\n\n")
        entries
            .filter { it.kind == "Chapter" }
            .forEach { append("## ${it.title}\n\n${it.body}\n\n---\n\n") }
    }
}

fun remapLinks(body: String, ids: Map<Long, Long>): String =
    Regex("\\[\\[(\\d+)\\|([^]]+)]]").replace(body) { match ->
        ids[match.groupValues[1].toLong()]?.let { "[[$it|${match.groupValues[2]}]]" }
            ?: match.groupValues[2]
    }
