package com.novelforge.app.data

import org.json.JSONArray
import org.json.JSONObject

/** Versioned portable JSON. Restore always creates a new project; IDs are remapped. */
object BackupCodec {
    const val MAX_BYTES = 16 * 1024 * 1024
    fun encode(book: Book, entries: List<Entry>): String = JSONObject().apply {
        put("format", "novelforge"); put("version", 1)
        put("book", JSONObject().apply { put("title", book.title); put("author", book.author); put("genre", book.genre); put("synopsis", book.synopsis); put("cover", book.cover); put("target", book.target) })
        put("entries", JSONArray().apply { entries.forEach { e -> put(JSONObject().apply {
            put("id", e.id); put("title", e.title); put("body", e.body); put("kind", e.kind); put("status", e.status); put("parentId", e.parentId ?: JSONObject.NULL); put("position", e.position)
        }) } })
    }.toString(2)
    fun decode(text: String): Pair<Book, List<Entry>> {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) { "Backup exceeds 16 MB." }
        val root = JSONObject(text)
        require(root.getString("format") == "novelforge" && root.getInt("version") == 1) { "Unsupported backup format." }
        val b = root.getJSONObject("book")
        val title = b.getString("title").trim(); require(title.isNotBlank() && title.length <= 200)
        val book = Book(title = "$title (restored)", author = b.optString("author"), genre = b.optString("genre"), synopsis = b.optString("synopsis"), cover = b.optInt("cover").coerceIn(0, 3), target = b.optInt("target", 80000).coerceIn(1, 10000000))
        val array = root.getJSONArray("entries"); require(array.length() <= 10000) { "Too many entries." }
        val entries = (0 until array.length()).map { i ->
            val e = array.getJSONObject(i); val body = e.getString("body"); val kind = e.getString("kind")
            require(kind in kinds); require(e.getString("title").isNotBlank())
            require(body.length <= 2000000) { "An entry is too large." }
            Entry(id = e.getLong("id"), bookId = book.id, title = e.getString("title"), body = body, kind = kind, status = e.optString("status", "Draft").takeIf { it in statuses } ?: "Draft", parentId = if (e.isNull("parentId")) null else e.getLong("parentId"), position = e.optInt("position"), words = wordCount(body))
        }
        require(entries.map { it.id }.distinct().size == entries.size) { "Duplicate entry IDs." }
        val arcs = entries.filter { it.kind == "Arc" }.map { it.id }.toSet()
        require(entries.all { it.parentId == null || it.parentId in arcs && it.parentId != it.id }) { "Invalid arc reference." }
        return book to entries
    }
    fun markdown(book: Book, entries: List<Entry>): String = buildString {
        append("# ${book.title}\n\n${book.author}\n\n")
        entries.filter { it.kind == "Chapter" }.forEach { append("## ${it.title}\n\n${it.body}\n\n---\n\n") }
    }
}
