package com.novelforge.app

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class StudioUiTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun shot(name: String) {
        compose.waitForIdle()
        val file=File("build/screenshots/$name.png");file.parentFile.mkdirs()
        file.outputStream().use {compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG,100,it)}
    }
    @Test fun libraryWorkspaceEditorAndReaderRenderAndNavigate() {
        compose.onNodeWithText("A home for your\nnext great story.").assertExists()
        shot("01-library")
        compose.onNodeWithText("Explore a sample project").performScrollTo().performClick()
        compose.waitUntil(20000) {compose.onAllNodesWithText("Write",useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}
        shot("02-workspace")
        compose.onNodeWithText("Write").performScrollTo().performClick()
        compose.waitUntil(20000) {compose.onAllNodesWithContentDescription("Manuscript text").fetchSemanticsNodes().isNotEmpty()}
        shot("03-editor")
        compose.onNodeWithContentDescription("Manuscript text").performTextReplacement("The city remembered the sea.\n\nA sentence saved before reading.")
        compose.onNodeWithContentDescription("Read this page").performClick()
        compose.waitUntil(20000) {compose.onAllNodesWithText("READING ROOM").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText("The city remembered the sea.").assertExists()
        shot("04-reader")
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNodeWithText("Story bible").performClick()
        compose.onNodeWithText("Mara Venn").assertExists()
        shot("05-story-bible")
    }
}
