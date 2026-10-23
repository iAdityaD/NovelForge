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
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class StudioUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun shot(name: String) {
        compose.waitForIdle()
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val file = File("build/screenshots/$name.png")
            file.parentFile?.mkdirs()
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test
    fun redesignedWorkspaceAndFourTabsRender() {
        compose.onNodeWithTag("new-workspace").performClick()
        compose.onNodeWithText("Novel title").assertExists()
        shot("00-creation")
        compose.onNodeWithContentDescription("Cancel workspace creation").performClick()
        compose.onNodeWithText("Open demo").performScrollTo().performClick()
        compose.waitUntil(20000) {
            compose.onAllNodesWithText("Your story, at a glance").fetchSemanticsNodes().isNotEmpty()
        }
        shot("10-overview")
        compose.onNodeWithText("Files").performClick()
        compose.onNodeWithContentDescription("Expand 01_Chapters").performClick()
        shot("02-workspace")
        compose.onNodeWithText("A map of the missing.md").performClick()
        compose.waitUntil(20000) {
            compose
                .onAllNodesWithContentDescription("Manuscript text")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        shot("03-editor")
        compose.onNodeWithContentDescription("Open file explorer").performClick()
        compose.onNodeWithContentDescription("Close file explorer").assertExists()
        shot("09-drawer")
        compose.onNodeWithContentDescription("Close file explorer").performClick()
        compose
            .onNodeWithContentDescription("Manuscript text")
            .performTextReplacement("A new world begins here.")
        compose.onNodeWithContentDescription("Read this page").performClick()
        compose.onNodeWithText("A new world begins here.").assertExists()
        shot("04-reader")
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNodeWithContentDescription("Back to library").performClick()
        shot("01-library")
        compose.onNodeWithText("Analytics").performClick()
        compose.onNodeWithText("Your writing portfolio").assertExists()
        shot("05-analytics")
        compose.onNodeWithText("Guide").performClick()
        compose.onNodeWithText("Link characters & worlds").assertExists()
        shot("06-guide")
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Data & backup").assertExists()
        shot("07-settings")
        compose.onNodeWithText("Editor").performClick()
        compose.onNodeWithText("Monospace editor").assertExists()
        shot("08-editor-settings")
    }
}
