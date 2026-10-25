package com.novelforge.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.data.local.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class CoverAndUnifiedWorkspaceTest {
    @Test fun coverIsLocalPortableAndInvalidReplacementKeepsOriginal() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        val file = java.io.File(context.cacheDir, "cover.png")
        val bitmap = Bitmap.createBitmap(60, 90, Bitmap.Config.ARGB_8888)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
        val repo = LocalWorkspaceRepository(db, LocalPreferencesRepository(context), covers = CoverImages(context))
        try {
            val id = repo.createBook("Cover", "", emptyList(), 0, 0, false).bookId
            repo.setCover(id, Uri.fromFile(file).toString())
            val before = db.dao().bookRecord(id)!!.toModel()
            assertTrue(before.coverImage.isNotEmpty())
            file.writeText("invalid image")
            assertTrue(runCatching { repo.setCover(id, Uri.fromFile(file).toString()) }.isFailure)
            assertEquals(before.coverImage, db.dao().bookRecord(id)!!.toModel().coverImage)
            val backup = BackupCodec.encode(before, emptyList())
            assertEquals(before.coverImage, BackupCodec.decode(backup).first.coverImage)
            repo.setCover(id, null)
            assertEquals("", db.dao().book(id)!!.coverImage)
        } finally { db.close(); file.delete() }
    }
    @Test fun movesPreserveRolesLinksAndIdentityAndRoleChangesAreExplicit() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ForgeDb::class.java).build()
        try {
            val repo = LocalWorkspaceRepository(db, LocalPreferencesRepository(context))
            val book = repo.createBook("Mixed", "", emptyList(), 0, 0, false).bookId
            val folder = repo.createNode(book, "Research", true, PLAN_AREA, null)
            val draft = repo.createNode(book, "Draft", false, MANUSCRIPT_AREA, null)
            val note = repo.createNode(book, "Note", false, PLAN_AREA, folder)
            db.dao().updateEntry(db.dao().entry(draft)!!.copy(body = "[[${note}|Note]]"))
            repo.relocate(draft, folder)
            assertEquals(MANUSCRIPT_AREA, db.dao().entry(draft)!!.area)
            repo.placeBefore(note, draft, true)
            assertEquals(PLAN_AREA, db.dao().entry(note)!!.area)
            assertEquals("[[${note}|Note]]", db.dao().entry(draft)!!.body)
            assertTrue(runCatching { repo.relocate(folder, folder) }.isFailure)
            repo.setArea(folder, MANUSCRIPT_AREA)
            assertEquals(MANUSCRIPT_AREA, db.dao().entry(note)!!.area)
            val copy = repo.duplicate(folder)
            val copiedChildren = db.dao().entries(book).filter { it.folderId == copy }
            val copiedDraft = copiedChildren.single { it.title == "Draft" }
            val copiedNote = copiedChildren.single { it.title == "Note" }
            assertEquals("[[${copiedNote.id}|Note]]", copiedDraft.body)
            assertNotEquals(draft, copiedDraft.id)
            repo.relocate(draft, null)
            assertNull(db.dao().entry(draft)!!.folderId)
        } finally { db.close() }
    }
}
