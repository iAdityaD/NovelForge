package com.novelforge.app

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetadataFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val folder = File(compose.activity.getExternalFilesDir(null), "screenshots")
        folder.mkdirs()
        File(folder, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }

    @Test
    fun limitsGenresAndCustomStarterFoldersPersist() {
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Writing").performScrollTo().performClick()
        compose.onNodeWithText("Content limits").performClick()
        compose.onNodeWithText("Maximum synopsis words").performTextReplacement("50")
        compose.onNodeWithText("Save limit").performClick()
        screenshot("limits")
        compose.onNodeWithText("Library").performClick()
        compose.onAllNodesWithText("New workspace").onLast().performClick()
        compose.onNodeWithText("Novel title").performTextInput("The Two Worlds")
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Add genre"))
        compose.onNodeWithText("Add genre").performClick()
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Custom genre"))
        compose.onNodeWithText("Custom genre").performTextInput("Historical fiction")
        compose.onNodeWithText("Add", substring = false).performClick()
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Add folder"))
        compose.onNodeWithText("Add folder").performClick()
        compose.onNodeWithText("Folder name").performTextInput("World maps")
        compose.onNodeWithText("Save folder").performClick()
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("World maps"))
        screenshot("custom-folders")
        compose.onNodeWithText("Create workspace").performClick()
        compose.waitUntil(10000) {
            compose
                .onAllNodesWithContentDescription("Workspace actions")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithTag("explorer-tree").performScrollToNode(hasText("World maps"))
        compose.onNodeWithText("World maps").assertExists()
        screenshot("workspace")
        compose.onNodeWithContentDescription("Workspace actions").performClick()
        compose.onNodeWithText("Edit workspace details").performClick()
        compose.onNodeWithText("Fantasy").assertExists()
        compose.onNodeWithText("Historical fiction").assertExists()
        compose
            .onNodeWithTag("novel-details-form")
            .performScrollToNode(hasTestTag("synopsis-input"))
        compose.onNodeWithTag("synopsis-input").performTextReplacement("Word ".repeat(51))
        compose.onNodeWithTag("synopsis-input").assertTextEquals("")
        compose
            .onNodeWithTag("synopsis-input")
            .performTextReplacement("Remembering ".repeat(50).trim())
        compose.onNodeWithText("50 / 50 words").assertExists()
        compose.onNodeWithText("Save changes").assertIsDisplayed()
        screenshot("synopsis")
        compose.onNodeWithText("Save changes").performClick()
        compose.waitUntil(10000) {
            compose.onAllNodesWithText("Save changes").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithContentDescription("Workspace actions").performClick()
        compose.onNodeWithText("Edit workspace details").performClick()
        compose
            .onNodeWithTag("novel-details-form")
            .performScrollToNode(hasTestTag("synopsis-input"))
        compose.onNodeWithTag("synopsis-input").assertTextEquals("Remembering ".repeat(50).trim())
        compose.onNodeWithContentDescription("Close novel details").performClick()
        compose.onNodeWithContentDescription("Back to library").performClick()
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Writing").performScrollTo().performClick()
        compose.onNodeWithText("Content limits").performClick()
        compose.onNodeWithText("Reset to 500 words").performClick()
    }
}
