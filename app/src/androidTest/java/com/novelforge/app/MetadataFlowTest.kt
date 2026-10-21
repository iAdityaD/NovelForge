package com.novelforge.app

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetadataFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun screenshot(name: String) {
        android.util.Log.i("MetadataFlow", "Step 18")
        compose.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        // Shared media survives UTP uninstalling the app before the host collects reports.
        val resolver = compose.activity.contentResolver
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NovelForgeTests")
            put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = checkNotNull(resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        checkNotNull(resolver.openOutputStream(uri)).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        values.clear()
        values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        bitmap.recycle()
    }

    @Test(timeout = 90000)
    fun limitsGenresAndCustomStarterFoldersPersist() {
        android.util.Log.i("MetadataFlow", "Step 30")
        compose.onNodeWithText("Settings").performClick()
        android.util.Log.i("MetadataFlow", "Step 31")
        compose.onNodeWithText("Writing").performScrollTo().performClick()
        android.util.Log.i("MetadataFlow", "Step 32")
        compose.onNodeWithText("Content limits").performClick()
        android.util.Log.i("MetadataFlow", "Step 33")
        compose.onNodeWithText("Maximum synopsis words").performTextReplacement("50")
        android.util.Log.i("MetadataFlow", "Step 34")
        compose.onNodeWithText("Save limit").performClick()
        android.util.Log.i("MetadataFlow", "Step 35")
        screenshot("limits")
        android.util.Log.i("MetadataFlow", "Step 36")
        compose.onNodeWithText("Library").performClick()
        android.util.Log.i("MetadataFlow", "Step 37")
        compose.waitUntil(10000) {
            compose.onAllNodesWithText("New workspace", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithText("New workspace", useUnmergedTree = true).onFirst().performClick()
        android.util.Log.i("MetadataFlow", "Step 38")
        compose.onNodeWithText("Novel title").performTextInput("The Two Worlds")
        android.util.Log.i("MetadataFlow", "Step 39")
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Add genre"))
        android.util.Log.i("MetadataFlow", "Step 40")
        compose.onNodeWithText("Add genre").performClick()
        android.util.Log.i("MetadataFlow", "Step 41")
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Custom genre"))
        android.util.Log.i("MetadataFlow", "Step 42")
        compose.onNodeWithText("Custom genre").performTextInput("Historical fiction")
        android.util.Log.i("MetadataFlow", "Step 43")
        compose.onNodeWithText("Add", substring = false).performClick()
        android.util.Log.i("MetadataFlow", "Step 44")
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Add folder"))
        android.util.Log.i("MetadataFlow", "Step 45")
        compose.onNodeWithText("Add folder").performClick()
        android.util.Log.i("MetadataFlow", "Step 46")
        compose.onNodeWithText("Folder name").performTextInput("World maps")
        android.util.Log.i("MetadataFlow", "Step 47")
        compose.onNodeWithText("Save folder").performClick()
        android.util.Log.i("MetadataFlow", "Step 48")
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("World maps"))
        android.util.Log.i("MetadataFlow", "Step 49")
        screenshot("custom-folders")
        android.util.Log.i("MetadataFlow", "Step 50")
        compose.onNodeWithText("Create workspace").performClick()
        android.util.Log.i("MetadataFlow", "Step 51")
        compose.waitUntil(10000) {
            compose
                .onAllNodesWithContentDescription("Workspace actions")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        android.util.Log.i("MetadataFlow", "Step 57")
        compose.onNodeWithTag("explorer-tree").performScrollToNode(hasText("World maps"))
        android.util.Log.i("MetadataFlow", "Step 58")
        compose.onNodeWithText("World maps").assertExists()
        android.util.Log.i("MetadataFlow", "Step 59")
        screenshot("workspace")
        android.util.Log.i("MetadataFlow", "Step 60")
        compose.onNodeWithContentDescription("Workspace actions").performClick()
        android.util.Log.i("MetadataFlow", "Step 61")
        compose.onNodeWithText("Edit workspace details").performClick()
        android.util.Log.i("MetadataFlow", "Step 62")
        compose.onNodeWithText("Fantasy").assertExists()
        android.util.Log.i("MetadataFlow", "Step 63")
        compose.onNodeWithText("Historical fiction").assertExists()
        compose
            .onNodeWithTag("novel-details-form")
            .performScrollToNode(hasText("Synopsis", substring = false))
        android.util.Log.i("MetadataFlow", "Step 67")
        compose.onNodeWithTag("synopsis-input").performTextReplacement("Word ".repeat(51))
        android.util.Log.i("MetadataFlow", "Step 68")
        compose.onNodeWithTag("synopsis-input").assertTextEquals("")
        compose
            .onNodeWithTag("synopsis-input")
            .performTextReplacement("Remembering ".repeat(50).trim())
        android.util.Log.i("MetadataFlow", "Step 72")
        compose.onNodeWithText("50 / 50 words").assertExists()
        android.util.Log.i("MetadataFlow", "Step 73")
        compose.onNodeWithText("Save changes").assertIsDisplayed()
        android.util.Log.i("MetadataFlow", "Step 74")
        screenshot("synopsis")
        android.util.Log.i("MetadataFlow", "Step 75")
        compose.onNodeWithText("Save changes").performClick()
        android.util.Log.i("MetadataFlow", "Step 76")
        compose.waitUntil(10000) {
            compose.onAllNodesWithText("Save changes").fetchSemanticsNodes().isEmpty()
        }
        android.util.Log.i("MetadataFlow", "Step 79")
        compose.onNodeWithContentDescription("Workspace actions").performClick()
        android.util.Log.i("MetadataFlow", "Step 80")
        compose.onNodeWithText("Edit workspace details").performClick()
        compose
            .onNodeWithTag("novel-details-form")
            .performScrollToNode(hasText("Synopsis", substring = false))
        android.util.Log.i("MetadataFlow", "Step 84")
        compose.onNodeWithTag("synopsis-input").assertTextEquals("Remembering ".repeat(50).trim())
        android.util.Log.i("MetadataFlow", "Step 85")
        compose.onNodeWithContentDescription("Close novel details").performClick()
        android.util.Log.i("MetadataFlow", "Step 86")
        compose.onNodeWithContentDescription("Back to library").performClick()
        android.util.Log.i("MetadataFlow", "Step 87")
        compose.onNodeWithText("Settings").performClick()
        android.util.Log.i("MetadataFlow", "Step 88")
        compose.onNodeWithText("Writing").performScrollTo().performClick()
        android.util.Log.i("MetadataFlow", "Step 89")
        compose.onNodeWithText("Content limits").performClick()
        android.util.Log.i("MetadataFlow", "Step 90")
        compose.onNodeWithText("Reset to 500 words").performClick()
    }
}
