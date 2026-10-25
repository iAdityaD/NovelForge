package com.novelforge.data.local

import androidx.room.withTransaction
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.domain.analytics.*
import com.novelforge.domain.backup.*
import com.novelforge.domain.preferences.*
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*
import com.novelforge.domain.writing.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Singleton
class LocalWorkspaceRepository
@Inject
constructor(private val db: ForgeDb, private val preferences: PreferencesRepository, private val journal: LocalDraftJournal? = null) :
    WorkspaceRepository {
    private val dao = Records(db.dao())
    override val books = dao.books()
    override val summaries = dao.summaries()
    override val trash = dao.trash()

    override suspend fun createBook(
        title: String, author: String, genres: List<String>, cover: Int, target: Int,
        standard: Boolean, templateId: String,
    ): CreatedWorkspace {
        require(title.isNotBlank() && title.trim().length <= 200) { "Use a project title of 1–200 characters." }
        require(target in 0..10000000) { "Use a word goal from 0 to 10,000,000." }
        val custom = preferences.state.value.savedTemplates.firstOrNull { it.id == templateId }?.let(::validateSavedTemplate)
        val recipe = if (custom == null) projectRecipe(templateId, preferences.state.value.starterFolders) else null
        val folders = if (standard) recipe?.folders.orEmpty().filter { it.enabled } else emptyList()
        val book = Book(title = title.trim(), author = author.trim(), genres = MetadataPolicy.genres(genres), cover = cover, target = target, fields = if (standard) custom?.fields.orEmpty() else emptyList())
        var overviewId: Long? = null
        db.withTransaction {
            dao.addBook(book)
            folders.forEachIndexed { index, folder ->
                val folderId = dao.addEntry(Entry(bookId = book.id, title = folder.name, kind = "Folder", position = index, area = if (folder.key == "manuscript") MANUSCRIPT_AREA else PLAN_AREA))
                val body = folderGuide(folder, book)
                val guideId = dao.addEntry(Entry(bookId = book.id, title = "README", kind = "Guide", folderId = folderId, body = body, words = wordCount(body)))
                if (folder.key == "overview") overviewId = guideId
                if (folder.key == "manuscript") recipe!!.draftTitles.forEachIndexed { n, draftTitle ->
                    val body = template(recipe.draftKind).replace("# Chapter title", "# $draftTitle")
                    val id = dao.addEntry(Entry(bookId = book.id, title = draftTitle, kind = recipe.draftKind,
                        folderId = folderId, body = body, words = wordCount(body), position = n + 1))
                    db.dao().place(ManuscriptPlacementEntity(id, book.id, n, true))
                }
            }
            if (standard && custom != null) {
                val ids = mutableMapOf<String, Long>()
                custom.nodes.forEach { node -> ids[node.key] = dao.addEntry(Entry(bookId = book.id, title = node.title, kind = node.kind, area = node.area, position = node.position)) }
                custom.nodes.forEach { node ->
                    val entry = requireNotNull(dao.entry(ids.getValue(node.key)))
                    dao.updateEntry(entry.copy(folderId = node.parentKey?.let(ids::getValue)))
                }
            }
            dao.updateBook(book.copy(overviewId = overviewId))
        }
        return CreatedWorkspace(book.id, overviewId)
    }

    override suspend fun createNode(bookId: String, title: String, folder: Boolean, area: String, parent: Long?, typeLabel: String, sourceUrl: String?, categoryName: String?): Long = db.withTransaction {
        require(area == PLAN_AREA || area == MANUSCRIPT_AREA) { "Invalid workspace area." }
        require(title.trim().isNotEmpty() && title.trim().length <= 200) { "Use a name of 1–200 characters." }
        require(dao.book(bookId) != null) { "Workspace is unavailable." }
        if (parent != null) require(dao.entry(parent)?.let { it.bookId == bookId && it.kind == "Folder" && !it.deleted } == true) { "Folder is unavailable." }
        val label = typeLabel.trim().ifBlank { if (area == MANUSCRIPT_AREA) "Document" else "Resource" }
        require(label.length <= 80 && label != "Folder") { "Choose a type label of at most 80 characters." }
        if (folder) {
            MetadataPolicy.folder(title)
            require(dao.entries(bookId).none { it.kind == "Folder" && it.folderId == parent && it.title.equals(title.trim(), true) }) { "A folder with this name already exists here." }
        }
        val url = sourceUrl?.trim()?.takeIf { it.isNotEmpty() }
        if (url != null) {
            val parsed = runCatching { java.net.URI(url) }.getOrNull()
            require(parsed != null && parsed.scheme?.lowercase() in listOf("https", "http") && !parsed.host.isNullOrBlank() && parsed.userInfo == null) { "Enter a valid http or https address." }
        }
        val body = if (url == null) "" else "## Source\n\n$url\n\n## Notes\n\n"
        val destination = if (categoryName != null) {
            require(!folder && area == PLAN_AREA)
            val name = MetadataPolicy.folder(categoryName)
            require(dao.entries(bookId).none { it.kind == "Folder" && it.folderId == parent && it.title.equals(name, true) }) { "A category with this name already exists here." }
            dao.addEntry(Entry(bookId = bookId, title = name, kind = "Folder", area = PLAN_AREA, folderId = parent))
        } else parent
        val position = dao.entries(bookId).filter { it.folderId == destination }.maxOfOrNull { it.position }?.plus(1) ?: 0
        val id = dao.addEntry(Entry(bookId = bookId, title = title.trim(), kind = if (folder) "Folder" else label, folderId = destination,
            area = area, body = if (folder) "" else body, words = if (folder) 0 else wordCount(body), position = position))
        dao.touch(bookId, System.currentTimeMillis())
        id
    }

    override suspend fun deleteBook(bookId: String) {
        val ids = db.dao().workspaceEntryIds(bookId)
        db.withTransaction {
            db.dao().deleteWritingDays(bookId)
            db.dao().deleteBook(bookId)
        }
        ids.forEach { journal?.clear(it) }
        preferences.forgetWorkspace(bookId, ids)
    }

    override suspend fun editBook(book: Book) {
        db.withTransaction {
            val previous = dao.book(book.id) ?: error("Workspace is unavailable.")
            validateFields(book.fields)
            book.fields.forEach { field ->
                previous.fields.find { it.id == field.id }?.let { require(it.type == field.type) { "Create a new field to change its type." } }
                dao.allEntries(book.id).forEach { validateFieldValue(field, it.metadata[field.id].orEmpty()) }
            }
            require(book.status in bookStatuses) { "Choose a valid writing status." }
            MetadataPolicy.synopsis(
                previous.synopsis,
                book.synopsis,
                preferences.state.value.synopsisLimit,
            )
            dao.updateBook(
                book.copy(
                    genres = MetadataPolicy.genres(book.genres),
                    updated = System.currentTimeMillis(),
                )
            )
        }
    }

    override suspend fun createEntry(
        bookId: String,
        title: String,
        kind: String,
        parentId: Long?,
        folderId: Long?,
    ): Long = db.withTransaction {
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
            summaries.first().filter { it.bookId == bookId }.maxOfOrNull { it.position }?.plus(1)
                ?: 0
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
        if (isManuscriptKind(kind)) db.dao().place(ManuscriptPlacementEntity(id, bookId, position, false))
        if (kind == "Novel Profile") dao.book(bookId)?.let { book ->
            if (book.overviewId?.let { dao.entry(it) }?.deleted != false) dao.updateBook(book.copy(overviewId = id))
        }
        dao.touch(bookId, System.currentTimeMillis())
        id
    }

    override suspend fun createResource(bookId: String, title: String, folderId: Long?, newCategory: String?, sourceUrl: String?, notes: String): Long = db.withTransaction {
        require(title.isNotBlank() && title.trim().length <= 200) { "Use a resource title of 1–200 characters." }
        require((folderId != null) != (newCategory != null)) { "Choose an existing category or create a new one." }
        require(dao.book(bookId) != null) { "Workspace is unavailable." }
        val url = sourceUrl?.trim()?.also { value ->
            val uri = runCatching { java.net.URI(value) }.getOrNull()
            require(uri != null && uri.scheme?.lowercase() in listOf("http", "https") && !uri.host.isNullOrBlank() && uri.userInfo == null) { "Enter a valid http or https website address." }
        }
        val category = if (newCategory != null) {
            val name = MetadataPolicy.folder(newCategory)
            require(dao.entries(bookId).none { it.kind == "Folder" && it.folderId == null && it.title.equals(name, true) }) { "This category already exists. Select it from the list." }
            dao.addEntry(Entry(bookId = bookId, title = name, kind = "Folder"))
        } else {
            require(dao.entry(folderId!!)?.let { it.bookId == bookId && it.kind == "Folder" && !it.deleted } == true) { "The selected category is unavailable." }
            folderId
        }
        val body = if (url == null) notes else "## Source\n\n$url\n\n## Notes\n\n$notes"
        val id = dao.addEntry(Entry(bookId = bookId, title = title.trim(), kind = if (url == null) "Resource" else "External source", folderId = category, body = body, words = wordCount(body)))
        dao.touch(bookId, System.currentTimeMillis())
        id
    }

    override suspend fun createFolder(bookId: String, title: String, parent: Long?) {
        val validName = MetadataPolicy.folder(title)
        require(
            dao.entries(bookId).none {
                it.kind == "Folder" && it.folderId == parent && it.title.equals(validName, true)
            }
        ) {
            "A folder with this name already exists here."
        }
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

    override suspend fun renameEntry(id: Long, title: String) {

        require(title.trim().isNotEmpty()) { "A name is required." }
        dao.entry(id)?.let { entry ->
            if (entry.kind == "Folder") {
                MetadataPolicy.folder(title)
                require(
                    dao.entries(entry.bookId).none {
                        it.id != id &&
                            it.kind == "Folder" &&
                            it.folderId == entry.folderId &&
                            it.title.equals(title.trim(), true)
                    }
                ) {
                    "A folder with this name already exists here."
                }
            }
            val changed =
                entry.copy(title = title.trim().take(200), updated = System.currentTimeMillis())
            db.withTransaction {
                dao.updateEntry(changed)
                dao.touch(changed.bookId, changed.updated)
            }
        }
    }

    override suspend fun relocate(id: Long, destination: Long?) {

        val e = dao.entry(id) ?: return
        val all = dao.entries(e.bookId)
        require(canRelocate(e, destination, all)) {
            "A folder cannot contain itself or its descendants."
        }
        if (e.kind == "Folder")
            require(
                all.none {
                    it.id != id &&
                        it.kind == "Folder" &&
                        it.folderId == destination &&
                        it.title.equals(e.title, true)
                }
            ) {
                "A folder with this name already exists at the destination."
            }
        val changed = e.copy(folderId = destination, updated = System.currentTimeMillis())
        db.withTransaction {
            dao.updateEntry(changed)
            dao.touch(changed.bookId, changed.updated)
        }
    }

    override suspend fun deleteEntry(id: Long) {

        val e = dao.entry(id) ?: return
        val all = dao.entries(e.bookId)
        val ids = descendantIds(id, all)
        db.withTransaction {
            ids.forEach { dao.setDeleted(it, true) }
            dao.touch(e.bookId, System.currentTimeMillis())
        }
    }

    override suspend fun restoreEntry(id: Long) {
        val e = dao.entry(id) ?: return
        val all = trash.first().filter { it.bookId == e.bookId }
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

    override suspend fun placeBefore(id: Long, targetId: Long, after: Boolean) = db.withTransaction {
        if (id == targetId) return@withTransaction
        val entry = requireNotNull(dao.entry(id)) { "Document is unavailable." }
        val target = requireNotNull(dao.entry(targetId)) { "Destination is unavailable." }
        require(!entry.deleted && !target.deleted && entry.bookId == target.bookId && entry.area == target.area) { "Choose a destination in the same workspace area." }
        val all = dao.entries(entry.bookId)
        require(canRelocate(entry, target.folderId, all)) { "A folder cannot contain itself or its descendants." }
        if (entry.kind == "Folder") require(all.none { it.id != id && it.kind == "Folder" && it.folderId == target.folderId && it.title.equals(entry.title, true) }) { "A folder with this name already exists here." }
        val siblings = all.filter { it.folderId == target.folderId && it.id != id }.sortedWith(compareBy<Entry> { it.position }.thenBy { it.id }).toMutableList()
        val index = siblings.indexOfFirst { it.id == targetId }
        require(index >= 0)
        siblings.add(index + if (after) 1 else 0, entry.copy(folderId = target.folderId))
        siblings.forEachIndexed { position, node -> dao.updateEntry(node.copy(position = position)) }
        dao.touch(entry.bookId, System.currentTimeMillis())
    }

    override suspend fun moveEntry(id: Long, delta: Int) {
        val e = dao.entry(id) ?: return
        val entries =
            dao.entries(e.bookId)
                .filter {
                    it.folderId == e.folderId
                }
                .sortedWith(compareBy<Entry> { it.position }.thenBy { it.id })
        val index = entries.indexOfFirst { it.id == id }
        val target = index + delta
        if (target !in entries.indices) return
        val list = entries.toMutableList()
        val moved = list.removeAt(index)
        list.add(target, moved)
        db.withTransaction {
            list.forEachIndexed { i, item -> dao.updateEntry(item.copy(position = i)) }
        }
    }

    override suspend fun setBookStatus(book: Book, status: String) {
        require(status in bookStatuses)
        db.withTransaction {
            dao.updateBook(book.copy(status = status, updated = System.currentTimeMillis()))
        }
    }

    override suspend fun loadDemo(): String {
        val book =
            Book(
                title = "The Atlas of\nForgotten Things",
                author = "E. W. Arden",
                genres = listOf("Literary fantasy"),
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
        return book.id
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
