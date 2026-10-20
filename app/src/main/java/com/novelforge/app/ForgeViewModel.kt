package com.novelforge.app

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.novelforge.app.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ForgeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as ForgeApplication
    private val db = app.db
    private val dao = db.dao()
    val books =
        dao.books().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val summaries =
        dao.summaries().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val trash =
        dao.trash().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val writingDays =
        dao.writingDays().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val prefs = app.getSharedPreferences("preferences", 0)
    var dark by mutableStateOf(prefs.getBoolean("dark", true))
        private set

    var editorMono by mutableStateOf(prefs.getBoolean("editorMono", false))
        private set

    var syntaxColors by mutableStateOf(prefs.getBoolean("syntaxColors", true))
        private set

    fun updateEditorMono(value: Boolean) {
        editorMono = value
        prefs.edit().putBoolean("editorMono", value).apply()
    }

    fun updateSyntaxColors(value: Boolean) {
        syntaxColors = value
        prefs.edit().putBoolean("syntaxColors", value).apply()
    }

    var fontSize by mutableFloatStateOf(prefs.getFloat("fontSize", 19f))
        private set

    var readerSerif by mutableStateOf(prefs.getBoolean("serif", true))
        private set

    var readerWarm by mutableStateOf(prefs.getBoolean("warm", true))
        private set

    var editor by mutableStateOf<Entry?>(null)
        private set

    var saveStatus by mutableStateOf("Saved locally")
        private set

    var busy by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var results by mutableStateOf<List<EntrySummary>>(emptyList())
        private set

    var revisions by mutableStateOf<List<Revision>>(emptyList())
        private set

    var connections by mutableStateOf<List<Pair<Long, Long>>>(emptyList())
        private set

    var backlinks by mutableStateOf<List<EntrySummary>>(emptyList())
        private set

    fun loadConnections(bookId: String) = launchWork {
        if (!flush()) return@launchWork
        val pages = dao.entries(bookId)
        val ids = pages.map { it.id }.toSet()
        connections =
            pages
                .flatMap { page ->
                    Regex("\\[\\[(\\d+)\\|")
                        .findAll(page.body)
                        .mapNotNull { match ->
                            match.groupValues[1]
                                .toLongOrNull()
                                ?.takeIf { it in ids }
                                ?.let { page.id to it }
                        }
                        .toList()
                }
                .distinct()
        backlinks =
            summaries.value.filter { summary ->
                connections.any { it.first == summary.id && it.second == editor?.id }
            }
    }

    private var saveJob: Job? = null
    private var searchJob: Job? = null
    private val saveMutex = Mutex()
    private var persisted: Entry? = null
    private val undo = ArrayDeque<String>()
    private val redo = ArrayDeque<String>()
    private var undoTime = 0L
    val canUndo
        get() = undo.isNotEmpty()

    val canRedo
        get() = redo.isNotEmpty()

    fun clearError() {
        error = null
    }

    fun updateDark(value: Boolean) {
        dark = value
        prefs.edit().putBoolean("dark", value).apply()
    }

    fun setFont(value: Float) {
        fontSize = value
        prefs.edit().putFloat("fontSize", value).apply()
    }

    fun setSerif(value: Boolean) {
        readerSerif = value
        prefs.edit().putBoolean("serif", value).apply()
    }

    fun setWarm(value: Boolean) {
        readerWarm = value
        prefs.edit().putBoolean("warm", value).apply()
    }

    private fun launchWork(block: suspend () -> Unit) =
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = e.message ?: "Something went wrong. Please try again."
            }
        }

    fun createBook(
        title: String,
        author: String,
        genre: String,
        cover: Int,
        target: Int,
        synopsis: String,
        folders: Set<String> = workspaceTemplates.map { it.name }.toSet(),
        done: (String) -> Unit,
    ) = launchWork {
        val book =
            Book(
                title = title.trim(),
                author = author.trim(),
                genre = genre,
                cover = cover,
                target = target,
                synopsis = synopsis,
            )
        db.withTransaction {
            dao.addBook(book)
            val ids =
                folders.associateWith { name ->
                    dao.addEntry(Entry(bookId = book.id, title = name, kind = "Folder"))
                }
            if ("01_Chapters" in folders)
                dao.addEntry(
                    Entry(bookId = book.id, title = "Chapter 1", folderId = ids["01_Chapters"])
                )
            if ("05_Metadata" in folders)
                dao.addEntry(
                    Entry(
                        bookId = book.id,
                        title = "Novel overview",
                        kind = "Note",
                        folderId = ids["05_Metadata"],
                        body =
                            "## Genre\n${book.genre}\n\n## Premise\n${book.synopsis}\n\n## Themes\n\n## Links & references\n",
                    )
                )
        }
        done(book.id)
    }

    fun editBook(book: Book) = launchWork {
        dao.updateBook(book.copy(updated = System.currentTimeMillis()))
    }

    fun createEntry(
        bookId: String,
        title: String,
        kind: String,
        parentId: Long?,
        folderId: Long? = null,
        done: (Long) -> Unit,
    ) = launchWork {
        require(title.isNotBlank()) { "A file name is required." }
        if (folderId != null)
            require(
                dao.entry(folderId)?.let {
                    it.bookId == bookId && it.kind == "Folder" && !it.deleted
                } == true
            ) {
                "The destination folder is unavailable."
            }
        val position =
            summaries.value.filter { it.bookId == bookId }.maxOfOrNull { it.position }?.plus(1) ?: 0
        val body = template(kind)
        val id =
            dao.addEntry(
                Entry(
                    bookId = bookId,
                    title = title.trim(),
                    kind = kind,
                    parentId = parentId,
                    folderId = folderId,
                    body = body,
                    position = position,
                    words = wordCount(body),
                )
            )
        dao.touch(bookId, System.currentTimeMillis())
        done(id)
    }

    fun openEntry(id: Long, done: () -> Unit) = launchWork {
        if (!flush()) return@launchWork
        val e = dao.entry(id)
        if (e == null || e.deleted) {
            error = "This page is unavailable. Deleted pages can be restored from Settings."
            return@launchWork
        }
        editor = e
        persisted = e
        undo.clear()
        redo.clear()
        revisions = emptyList()
        saveStatus = "Saved locally"
        prefs.edit().putLong("lastEntry", id).putString("lastBook", e.bookId).apply()
        done()
    }

    fun updateBody(body: String) {
        val e = editor ?: return
        if (e.body == body) return
        val now = System.currentTimeMillis()
        if (now - undoTime > 700 || undo.isEmpty()) {
            undo.addLast(e.body)
            if (undo.size > 20) undo.removeFirst()
        }
        undoTime = now
        redo.clear()
        editor = e.copy(body = body, updated = now)
        scheduleSave()
    }

    fun undo() {
        if (undo.isNotEmpty()) {
            editor?.let {
                redo.addLast(it.body)
                editor = it.copy(body = undo.removeLast())
            }
            scheduleSave()
        }
    }

    fun redo() {
        if (redo.isNotEmpty()) {
            editor?.let {
                undo.addLast(it.body)
                editor = it.copy(body = redo.removeLast())
            }
            scheduleSave()
        }
    }

    fun updateTitle(title: String) {
        editor = editor?.copy(title = title)
        scheduleSave()
    }

    fun setStatus(status: String) {
        editor = editor?.copy(status = status)
        scheduleSave()
    }

    private fun scheduleSave() {
        saveStatus = "Saving…"
        saveJob?.cancel()
        saveJob =
            app.ioScope.launch {
                delay(400)
                persist()
            }
    }

    private suspend fun persist(): Boolean =
        saveMutex.withLock {
            val snapshot = editor ?: return@withLock true
            if (snapshot == persisted) {
                saveStatus = "Saved locally"
                return@withLock true
            }
            try {
                val saved =
                    snapshot.copy(
                        title = snapshot.title.ifBlank { "Untitled page" },
                        words = wordCount(snapshot.body),
                        updated = System.currentTimeMillis(),
                    )
                db.withTransaction {
                    dao.updateEntry(saved)
                    dao.touch(saved.bookId, saved.updated)
                    if (snapshot.body != persisted?.body) {
                        val day = java.time.LocalDate.now().toString()
                        dao.ensureDay(saved.bookId, day)
                        dao.recordWriting(
                            saved.bookId,
                            day,
                            (wordCount(snapshot.body) - wordCount(persisted?.body ?: ""))
                                .coerceAtLeast(0),
                        )
                    }
                }
                persisted = snapshot
                if (editor == snapshot) saveStatus = "Saved locally"
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saveStatus = "Save failed · tap to retry"
                error =
                    "Your latest edit could not be saved. Free some storage and retry before leaving."
                false
            }
        }

    suspend fun flush(): Boolean {
        saveJob?.cancel()
        return withContext(Dispatchers.IO) { persist() }
    }

    fun flushBackground() {
        saveJob?.cancel()
        app.ioScope.launch { persist() }
    }

    fun leave(done: () -> Unit) = launchWork { if (flush()) done() }

    fun checkpoint() = launchWork {
        if (!flush()) return@launchWork
        editor?.let {
            dao.addRevision(Revision(entryId = it.id, title = it.title, body = it.body))
            dao.prune(it.id)
            revisions = dao.revisions(it.id)
            saveStatus = "Checkpoint saved"
        }
    }

    fun loadRevisions() = launchWork { editor?.let { revisions = dao.revisions(it.id) } }

    fun restoreRevision(r: Revision) {
        updateBody(r.body)
        updateTitle(r.title)
    }

    fun deleteEntry(id: Long, done: () -> Unit) = launchWork {
        if (!flush()) return@launchWork
        val e = dao.entry(id) ?: return@launchWork
        val all = dao.entries(e.bookId)
        val ids = descendantIds(id, all)
        db.withTransaction {
            ids.forEach { dao.setDeleted(it, true) }
            dao.touch(e.bookId, System.currentTimeMillis())
        }
        if (editor?.id in ids) {
            editor = editor?.copy(deleted = true)
            persisted = editor
        }
        done()
    }

    fun restoreEntry(id: Long) = launchWork {
        val e = dao.entry(id) ?: return@launchWork
        val all = trash.value.filter { it.bookId == e.bookId }
        db.withTransaction {
            descendantIds(id, all).forEach { dao.setDeleted(it, false) }
            var parent = e.folderId
            val seen = mutableSetOf<Long>()
            while (parent != null && seen.add(parent)) {
                val folder = dao.entry(parent) ?: break
                dao.setDeleted(folder.id, false)
                parent = folder.folderId
            }
        }
    }

    fun renameEntry(id: Long, title: String) = launchWork {
        if (!flush()) return@launchWork
        require(title.trim().isNotEmpty()) { "A name is required." }
        dao.entry(id)?.let { entry ->
            val changed =
                entry.copy(title = title.trim().take(200), updated = System.currentTimeMillis())
            db.withTransaction {
                dao.updateEntry(changed)
                dao.touch(changed.bookId, changed.updated)
            }
            if (editor?.id == id) {
                editor = changed
                persisted = changed
            }
        }
    }

    fun createFolder(bookId: String, title: String, parent: Long?) = launchWork {
        require(title.trim().isNotEmpty()) { "A folder name is required." }
        if (parent != null)
            require(
                dao.entry(parent)?.let {
                    it.bookId == bookId && it.kind == "Folder" && !it.deleted
                } == true
            )
        dao.addEntry(
            Entry(
                bookId = bookId,
                title = title.trim().take(200),
                kind = "Folder",
                folderId = parent,
            )
        )
        dao.touch(bookId, System.currentTimeMillis())
    }

    fun relocate(id: Long, destination: Long?) = launchWork {
        if (!flush()) return@launchWork
        val e = dao.entry(id) ?: return@launchWork
        val all = dao.entries(e.bookId)
        require(canRelocate(e, destination, all)) {
            "A folder cannot contain itself or its descendants."
        }
        val changed = e.copy(folderId = destination, updated = System.currentTimeMillis())
        db.withTransaction {
            dao.updateEntry(changed)
            dao.touch(changed.bookId, changed.updated)
        }
        if (editor?.id == id) {
            editor = changed
            persisted = changed
        }
    }

    fun setBookStatus(book: Book, status: String) = launchWork {
        require(status in bookStatuses)
        dao.updateBook(book.copy(status = status, updated = System.currentTimeMillis()))
    }

    fun moveEntry(id: Long, delta: Int) = launchWork {
        val e = dao.entry(id) ?: return@launchWork
        val entries =
            dao.entries(e.bookId)
                .filter {
                    it.folderId == e.folderId && (it.kind == "Folder") == (e.kind == "Folder")
                }
                .sortedWith(compareBy<Entry> { it.position }.thenBy { it.title })
        val index = entries.indexOfFirst { it.id == id }
        val target = index + delta
        if (target !in entries.indices) return@launchWork
        val list = entries.toMutableList()
        val moved = list.removeAt(index)
        list.add(target, moved)
        db.withTransaction {
            list.forEachIndexed { i, item -> dao.updateEntry(item.copy(position = i)) }
        }
    }

    fun assignArc(id: Long, parentId: Long?) = launchWork {
        dao.entry(id)?.let { dao.updateEntry(it.copy(parentId = parentId)) }
    }

    fun boardStatus(id: Long, status: String) = launchWork {
        dao.entry(id)?.let { dao.updateEntry(it.copy(status = status)) }
    }

    fun search(query: String) {
        searchJob?.cancel()
        searchJob = launchWork {
            delay(200)
            val expression = searchExpression(query)
            results = if (expression.isEmpty()) emptyList() else dao.search(expression)
        }
    }

    fun export(uri: Uri, bookId: String, markdown: Boolean, done: () -> Unit) = launchWork {
        if (!flush()) return@launchWork
        busy = true
        try {
            withContext(Dispatchers.IO) {
                val book = requireNotNull(dao.book(bookId))
                val entries = dao.entries(bookId)
                val text =
                    if (markdown) BackupCodec.markdown(book, entries)
                    else BackupCodec.encode(book, entries)
                require(text.toByteArray().size <= BackupCodec.MAX_BYTES) {
                    "Project exceeds the current 16 MB backup limit."
                }
                val stream =
                    requireNotNull(app.contentResolver.openOutputStream(uri, "wt")) {
                        "Cannot open destination."
                    }
                stream.bufferedWriter(Charsets.UTF_8).use { it.write(text) }
            }
            done()
        } finally {
            busy = false
        }
    }

    fun importBackup(uri: Uri, done: (String) -> Unit) = launchWork {
        busy = true
        try {
            val id =
                withContext(Dispatchers.IO) {
                    val text =
                        requireNotNull(app.contentResolver.openInputStream(uri)).use { input ->
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
                            val bytes = out.toByteArray()
                            require(bytes.size <= BackupCodec.MAX_BYTES) { "Backup exceeds 16 MB." }
                            bytes.toString(Charsets.UTF_8)
                        }
                    val (book, entries) = BackupCodec.decode(text)
                    db.withTransaction {
                        dao.addBook(book)
                        val ids = mutableMapOf<Long, Long>()
                        entries.forEach {
                            ids[it.id] =
                                dao.addEntry(it.copy(id = 0, parentId = null, folderId = null))
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
            done(id)
        } finally {
            busy = false
        }
    }

    fun loadDemo(done: (String) -> Unit) = launchWork {
        val book =
            Book(
                title = "The Atlas of\nForgotten Things",
                author = "E. W. Arden",
                genre = "Literary fantasy",
                synopsis =
                    "In a city that forgets one thing every midnight, a young cartographer begins drawing a map of everything that has disappeared.",
                cover = 0,
                target = 70000,
            )
        db.withTransaction {
            dao.addBook(book)
            val arc =
                dao.addEntry(
                    Entry(
                        bookId = book.id,
                        title = "The city that forgets",
                        kind = "Arc",
                        body =
                            "## Central conflict\nMara must choose between preserving the past and protecting the people she loves.\n\n## Turning point\nThe atlas begins to remember her.",
                        position = 0,
                    )
                )
            dao.addEntry(
                Entry(
                    bookId = book.id,
                    title = "A map of the missing",
                    parentId = arc,
                    status = "In progress",
                    body = DEMO_TEXT,
                    words = wordCount(DEMO_TEXT),
                    position = 1,
                )
            )
            dao.addEntry(
                Entry(
                    bookId = book.id,
                    title = "The midnight collector",
                    parentId = arc,
                    body =
                        "The bell rang thirteen times.\n\nMara counted again, though the sound had already vanished into the rain. Somewhere in the lower city, a door that had never existed was opening.",
                    words = 31,
                    position = 2,
                )
            )
            dao.addEntry(
                Entry(
                    bookId = book.id,
                    title = "Mara Venn",
                    kind = "Character",
                    body =
                        "## Role in the story\nCartographer of forgotten places.\n\n## Desire & motivation\nTo find the street where her mother once lived.\n\n## Fear & flaw\nShe trusts maps more than people.\n\n## Character arc\nFrom collecting memories to making new ones.",
                    position = 3,
                )
            )
            dao.addEntry(
                Entry(
                    bookId = book.id,
                    title = "The paper district",
                    kind = "Location",
                    body =
                        "## Atmosphere\nRain-dark stone, warm windows, and the smell of ink.\n\n## History\nEvery street was once named after a story. Most have forgotten their names.",
                    position = 4,
                )
            )
            dao.addEntry(
                Entry(
                    bookId = book.id,
                    title = "The missing street",
                    kind = "Plot thread",
                    body =
                        "## Setup\nMara finds a blank space on a map.\n\n## Foreshadowing\nThe city's oldest residents avoid the western bridge.\n\n## Payoff\nThe street was hidden, not forgotten.",
                    position = 5,
                )
            )
            val existing = dao.entries(book.id)
            val folders =
                workspaceTemplates.associate {
                    it.name to
                        dao.addEntry(Entry(bookId = book.id, title = it.name, kind = "Folder"))
                }
            existing.forEach { e ->
                dao.updateEntry(e.copy(folderId = folders[defaultFolder(e.kind)]))
            }
        }
        done(book.id)
    }
}

val DEMO_TEXT =
    """The city had forgotten the sea.

Mara noticed it first in the maps. Where blue ink had once pooled against the western edge of the paper, there was now a pale, unfinished space. The cartographers had drawn small mountains over it. They looked apologetic.

She stood at the window of her workshop, watching rain gather on the sill. Below, the paper district was waking: shutters creaked open, a bicycle rattled over the stones, and someone was singing a song with no words.

On her desk lay the only map that still remembered.

It was no larger than her hand, folded twice and soft at the corners. Her mother had given it to her on a Thursday. Mara was certain about the Thursday, though she could no longer remember the year.

“Some things,” her mother had said, “need someone to keep looking.”

She unfolded it carefully.

A thin blue line shimmered across the paper. Not a river. Not a road. Something that moved when she wasn't looking directly at it.

For the first time in seven years, the workshop smelled of salt.

Mara reached for her pen.

Outside, the bells began to count down to midnight, though the morning sun had only just found its way between the roofs. The bicycle stopped. The singing stopped. Even the rain seemed to wait.

And in the blank space at the edge of the map, a small black door appeared."""
