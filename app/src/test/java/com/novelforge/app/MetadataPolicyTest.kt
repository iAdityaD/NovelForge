package com.novelforge.app

import com.novelforge.app.data.BackupCodec
import com.novelforge.core.model.*
import com.novelforge.domain.workspace.*
import org.junit.Assert.*
import org.junit.Test

class MetadataPolicyTest {
    @Test
    fun synopsisRejectsOversizedPasteButPreservesLegacyText() {
        val legacy = "word ".repeat(600)
        assertFalse(MetadataPolicy.acceptsSynopsis("", legacy, 500))
        assertTrue(MetadataPolicy.acceptsSynopsis(legacy, legacy, 500))
        assertTrue(MetadataPolicy.acceptsSynopsis(legacy, "word ".repeat(550), 500))
        assertFalse(MetadataPolicy.acceptsSynopsis(legacy, legacy + "extra", 500))
    }

    @Test
    fun genresAreDistinctValuesAndBackupsPreserveLongText() {
        val genres =
            MetadataPolicy.genres(listOf(" Fantasy ", "fantasy", "Historical fiction", "Drama"))
        assertEquals(listOf("Fantasy", "Historical fiction", "Drama"), genres)
        val book = Book(title = "Test", genres = genres, synopsis = "word ".repeat(600))
        val restored = BackupCodec.decode(BackupCodec.encode(book, emptyList())).first
        assertEquals(genres, restored.genres)
        assertEquals(book.synopsis, restored.synopsis)
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateFoldersAreRejected() {
        MetadataPolicy.folders(listOf("Worlds", " worlds "))
    }

    @Test(expected = IllegalArgumentException::class)
    fun folderPathsAreRejected() {
        MetadataPolicy.folder("Worlds/North")
    }

    @Test
    fun legacyBackupGenreIsPreserved() {
        val json =
            """{"format":"novelforge","version":2,"book":{"title":"Old","genre":"Historical, literary"},"entries":[]}"""
        assertEquals(listOf("Historical, literary"), BackupCodec.decode(json).first.genres)
    }
}
