package com.novelforge.data.local

import android.content.Context
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
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Singleton
class LocalPreferencesRepository @Inject constructor(@ApplicationContext context: Context) :
    PreferencesRepository {
    private val prefs = context.getSharedPreferences("preferences", 0)
    private val current =
        MutableStateFlow(
            AppPreferences(
                prefs.getBoolean("dark", true),
                prefs.getBoolean("editorMono", false),
                prefs.getBoolean("syntaxColors", true),
                prefs.getFloat("fontSize", 19f),
                prefs.getBoolean("serif", true),
                prefs.getBoolean("warm", true),
                prefs.getInt("synopsisLimit", 500),
                readStarterFolders(),
                prefs.getFloat("readerFontSize", prefs.getFloat("fontSize", 19f)),
                readTemplates(),
            )
        )
    private fun readStarterFolders(): List<StarterFolder> = runCatching {
        val raw = prefs.getString("starterFolders", null) ?: return defaultStarterFolders
        val array = org.json.JSONArray(raw)
        validateStarterFolders((0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            StarterFolder(item.getString("key"), item.getString("name"), item.optBoolean("enabled", true))
        })
    }.getOrDefault(defaultStarterFolders)

    private fun readTemplates(): List<SavedTemplate> = runCatching {
        val array = org.json.JSONArray(prefs.getString("savedTemplates", "[]"))
        (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            val nodes = item.getJSONArray("nodes")
            validateSavedTemplate(SavedTemplate(item.getString("id"), item.getString("name"), (0 until nodes.length()).map { n ->
                val node = nodes.getJSONObject(n)
                TemplateNode(node.getString("key"), if (node.isNull("parent")) null else node.getString("parent"), node.getString("title"), node.getString("kind"), node.getString("area"), node.getInt("position"))
            }, item.getInt("version"), FieldCodec.definitions(item.optJSONArray("fields")?.toString() ?: "[]")))
        }
    }.getOrDefault(emptyList())

    override val state = current.asStateFlow()

    override fun update(value: AppPreferences) {
        require(value.synopsisLimit in 50..10000)
        require(value.fontSize.isFinite() && value.fontSize in 14f..30f)
        require(value.readerFontSize.isFinite() && value.readerFontSize in 14f..30f)
        validateStarterFolders(value.starterFolders)
        require(value.savedTemplates.size <= 50)
        require(value.savedTemplates.map { it.id }.distinct().size == value.savedTemplates.size)
        value.savedTemplates.forEach(::validateSavedTemplate)
        current.value = value
        prefs
            .edit()
            .putBoolean("dark", value.dark)
            .putBoolean("editorMono", value.editorMono)
            .putBoolean("syntaxColors", value.syntaxColors)
            .putFloat("fontSize", value.fontSize)
            .putFloat("readerFontSize", value.readerFontSize)
            .putBoolean("serif", value.readerSerif)
            .putBoolean("warm", value.readerWarm)
            .putInt("synopsisLimit", value.synopsisLimit)
            .putString("savedTemplates", org.json.JSONArray().apply {
                value.savedTemplates.forEach { template -> put(org.json.JSONObject().apply {
                    put("fields", org.json.JSONArray(FieldCodec.encodeDefinitions(template.fields)))
                    put("id", template.id); put("name", template.name); put("version", template.version)
                    put("nodes", org.json.JSONArray().apply { template.nodes.forEach { node -> put(org.json.JSONObject().apply {
                        put("key", node.key); put("parent", node.parentKey ?: org.json.JSONObject.NULL); put("title", node.title); put("kind", node.kind); put("area", node.area); put("position", node.position)
                    }) } })
                }) }
            }.toString())
            .putString("starterFolders", org.json.JSONArray().apply {
                value.starterFolders.forEach { folder -> put(org.json.JSONObject().apply {
                    put("key", folder.key); put("name", folder.name); put("enabled", folder.enabled)
                }) }
            }.toString())
            .apply()
    }

    override fun lastBook() = prefs.getString("lastBook", "") ?: ""

    override fun lastEntry() = prefs.getLong("lastEntry", 0)

    override fun lastEntry(bookId: String) = prefs.getLong("last-entry-$bookId", 0)
    override fun editingPosition(bookId: String, id: Long) = prefs.getInt("cursor-$bookId-$id", 0) to prefs.getInt("editor-scroll-$bookId-$id", 0)
    override fun editingPosition(bookId: String, id: Long, cursor: Int, scroll: Int) {
        prefs.edit().putInt("cursor-$bookId-$id", cursor).putInt("editor-scroll-$bookId-$id", scroll).apply()
    }

    override fun rememberBook(id: String) {
        prefs.edit().putString("lastBook", id).apply()
    }

    override fun rememberEntry(entry: Entry) {
        prefs.edit().putLong("lastEntry", entry.id).putLong("last-entry-${entry.bookId}", entry.id).putString("lastBook", entry.bookId).apply()
    }

    override fun forgetWorkspace(bookId: String, entryIds: List<Long>) {
        prefs.edit().apply {
            remove("last-entry-$bookId")
            if (lastBook() == bookId) { remove("lastBook"); remove("lastEntry") }
            entryIds.forEach { id -> remove("bookmark-$id"); remove("read-$id"); remove("offset-$id") }
        }.apply()
    }

    override fun bookmark(id: Long) = prefs.getBoolean("bookmark-$id", false)

    override fun bookmark(id: Long, value: Boolean) {
        prefs.edit().putBoolean("bookmark-$id", value).apply()
    }

    override fun readingPosition(id: Long) =
        prefs.getInt("read-$id", 0) to prefs.getInt("offset-$id", 0)

    override fun readingPosition(id: Long, index: Int, offset: Int) {
        prefs.edit().putInt("read-$id", index).putInt("offset-$id", offset).apply()
    }
}
