package com.novelforge.app

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class StudioUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun shot(name: String) {
        compose.waitForIdle()
        val file = File("build/screenshots/$name.png")
        file.parentFile.mkdirs()
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test
    fun libraryWorkspaceEditorAndReaderRenderAndNavigate() {
        compose.onNodeWithText("A home for your\nnext great story.").assertExists()
        shot("01-library")
        compose.onNodeWithText("Explore a sample project").performScrollTo().performClick()
        compose.waitUntil(20000) {
            compose
                .onAllNodesWithText("Write", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        shot("02-workspace")
        compose.onNodeWithText("Write").performScrollTo().performClick()
        compose.waitUntil(20000) {
            compose
                .onAllNodesWithContentDescription("Manuscript text")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        shot("03-editor")
        compose
            .onNodeWithContentDescription("Manuscript text")
            .performTextReplacement(
                "The city remembered the sea.\n\nA sentence saved before reading."
            )
        compose.onNodeWithContentDescription("Read this page").performClick()
        compose.waitUntil(20000) {
            compose.onAllNodesWithText("READING ROOM").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("The city remembered the sea.").assertExists()
        compose.onNodeWithContentDescription("Bookmark chapter").performClick()
        compose.onNodeWithContentDescription("Remove bookmark").assertExists()
        shot("04-reader")
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNodeWithText("Story bible").performClick()
        compose.onNodeWithText("Mara Venn").assertExists()
        shot("05-story-bible")
        compose.onNodeWithText("Timeline").performScrollTo().performClick()
        compose.onNodeWithText("Every story begins somewhere").assertExists()
        compose.onAllNodesWithText("Add event")[0].performClick()
        compose.onNodeWithText("Timeline event title").performTextInput("The first midnight")
        compose.onNodeWithText("Create timeline event").performClick()
        compose.waitUntil(20000) {
            compose
                .onAllNodesWithContentDescription("Manuscript text")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose
            .onNodeWithContentDescription("Manuscript text")
            .assertTextContains("## When", substring = true)
        compose.onNodeWithContentDescription("Back to manuscript").performClick()
        compose.onNodeWithText("Timeline").performScrollTo().performClick()
        compose.onNodeWithText("The first midnight").assertExists()
        shot("06-timeline")
        compose.onNodeWithText("Library").performClick()
        shot("07-bookshelf")
    }
}
