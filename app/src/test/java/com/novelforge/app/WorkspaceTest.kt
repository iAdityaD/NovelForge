package com.novelforge.app

import com.novelforge.app.data.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class WorkspaceTest {
    @Test
    fun hierarchyRejectsCyclesAndCrossWorkspaceMoves() {
        val a = Entry(id = 1, bookId = "a", title = "Worlds", kind = "Folder")
        val b = Entry(id = 2, bookId = "a", title = "North", kind = "Folder", folderId = 1)
        val c = Entry(id = 3, bookId = "a", title = "Lore", kind = "Note", folderId = 2)
        val foreign = Entry(id = 4, bookId = "b", title = "Other", kind = "Folder")
        val all = listOf(a, b, c, foreign)
        assertFalse(canRelocate(a, 2, all))
        assertFalse(canRelocate(c, 4, all))
        assertFalse(canRelocate(c, 3, all))
        assertTrue(canRelocate(c, 1, all))
        assertEquals(setOf(1L, 2L, 3L), descendantIds(1, all))
    }

    @Test
    fun folderBackupRoundtripAndLegacyImport() {
        val b = Book(title = "World", status = "Completed")
        val files =
            listOf(
                Entry(id = 1, bookId = b.id, title = "Worlds", kind = "Folder"),
                Entry(id = 2, bookId = b.id, title = "Chapter", folderId = 1, body = "Story"),
            )
        val (restored, items) = BackupCodec.decode(BackupCodec.encode(b, files))
        assertEquals("Completed", restored.status)
        assertEquals(1L, items[1].folderId)
        val legacy =
            BackupCodec.encode(b, listOf(files[1].copy(folderId = null)))
                .replace("\"version\": 2", "\"version\": 1")
        assertNull(BackupCodec.decode(legacy).second.single().folderId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun malformedFolderCycleRejectedBeforeRestore() {
        val b = Book(title = "Broken")
        BackupCodec.decode(
            BackupCodec.encode(
                b,
                listOf(
                    Entry(id = 1, bookId = b.id, title = "A", kind = "Folder", folderId = 2),
                    Entry(id = 2, bookId = b.id, title = "B", kind = "Folder", folderId = 1),
                ),
            )
        )
    }

    @Test
    fun streakAllowsTodayNotYetWrittenButStopsAtGap() {
        val now = LocalDate.of(2026, 10, 20)
        val days =
            listOf(
                WritingDay("a", "2026-10-19", 20, 1),
                WritingDay("b", "2026-10-18", 5, 1),
                WritingDay("a", "2026-10-16", 20, 1),
            )
        assertEquals(2, writingStreak(days, now))
        assertEquals(0, writingStreak(days, now.plusDays(1)))
    }
}
