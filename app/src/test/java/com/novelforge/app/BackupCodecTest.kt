package com.novelforge.app

import com.novelforge.app.data.*
import org.junit.Assert.*
import org.junit.Test

class BackupCodecTest {
    @Test
    fun backupRoundTripPreservesStoryStructureAndUnicode() {
        val book =
            Book(
                title = "A world of words",
                author = "ఆదిత్య",
                synopsis = "A memory",
                cover = 2,
                target = 90000,
            )
        val entries =
            listOf(
                Entry(id = 7, bookId = book.id, title = "Act I", kind = "Arc"),
                Entry(
                    id = 8,
                    bookId = book.id,
                    title = "Dawn",
                    body = "Hello, ప్రపంచం. [[7|Act I]]",
                    parentId = 7,
                    status = "Revised",
                    position = 1,
                ),
            )
        val (restored, pages) = BackupCodec.decode(BackupCodec.encode(book, entries))
        assertNotEquals(book.id, restored.id)
        assertEquals(book.author, restored.author)
        assertEquals(book.synopsis, restored.synopsis)
        assertEquals(7L, pages[1].parentId)
        assertEquals(entries[1].body, pages[1].body)
        assertEquals("Revised", pages[1].status)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnknownVersion() {
        BackupCodec.decode(
            BackupCodec.encode(Book(title = "A"), emptyList())
                .replace("\"version\": 2", "\"version\": 99")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDuplicateIds() {
        val b = Book(title = "A")
        val e = Entry(id = 1, bookId = b.id, title = "A")
        BackupCodec.decode(BackupCodec.encode(b, listOf(e, e)))
    }

    @Test
    fun exportDoesNotIncludeResearchInManuscript() {
        val b = Book(title = "A")
        val output =
            BackupCodec.markdown(
                b,
                listOf(
                    Entry(bookId = b.id, title = "Chapter", body = "story"),
                    Entry(bookId = b.id, title = "Secret", kind = "Research", body = "private note"),
                ),
            )
        assertTrue(output.contains("story"))
        assertFalse(output.contains("private note"))
    }

    @Test
    fun remapsInternalLinksOnImport() {
        assertEquals(
            "See [[44|Mara]] and Lost.",
            remapLinks("See [[4|Mara]] and [[8|Lost]].", mapOf(4L to 44L)),
        )
    }

    @Test
    fun deletedArcDoesNotCreateInvalidBackup() {
        val b = Book(title = "A")
        val (_, pages) =
            BackupCodec.decode(
                BackupCodec.encode(
                    b,
                    listOf(Entry(id = 2, bookId = b.id, title = "Dawn", parentId = 1)),
                )
            )
        assertNull(pages[0].parentId)
    }

    @Test
    fun searchEscapesOperatorsAndQuotes() {
        assertEquals("\"Mara\"* AND \"OR\"* AND \"sea\"*", searchExpression("Mara \" OR sea*"))
        assertEquals("", searchExpression("*\"-()"))
    }

    @Test
    fun countsWordsWithoutCountingMarkdownPunctuation() {
        assertEquals(4, wordCount("## Hello, world! It's dawn."))
        assertEquals(0, wordCount("  \n** --"))
    }
}
