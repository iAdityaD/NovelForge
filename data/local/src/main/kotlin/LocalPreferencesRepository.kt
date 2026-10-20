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
            )
        )
    override val state = current.asStateFlow()

    override fun update(value: AppPreferences) {
        require(value.synopsisLimit in 50..10000)
        current.value = value
        prefs
            .edit()
            .putBoolean("dark", value.dark)
            .putBoolean("editorMono", value.editorMono)
            .putBoolean("syntaxColors", value.syntaxColors)
            .putFloat("fontSize", value.fontSize)
            .putBoolean("serif", value.readerSerif)
            .putBoolean("warm", value.readerWarm)
            .putInt("synopsisLimit", value.synopsisLimit)
            .apply()
    }

    override fun lastBook() = prefs.getString("lastBook", "") ?: ""

    override fun lastEntry() = prefs.getLong("lastEntry", 0)

    override fun rememberBook(id: String) {
        prefs.edit().putString("lastBook", id).apply()
    }

    override fun rememberEntry(entry: Entry) {
        prefs.edit().putLong("lastEntry", entry.id).putString("lastBook", entry.bookId).apply()
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
