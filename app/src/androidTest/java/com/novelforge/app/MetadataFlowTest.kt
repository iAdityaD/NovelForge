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
    fun standardWorkspaceSettingsMetadataFiltersAndDeletion() {
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Writing").performScrollTo().performClick()
        compose.onNodeWithText("Workspace structure").performClick()
        compose.onNodeWithContentDescription("Rename Novel Overview").performScrollTo().performClick()
        compose.onNodeWithText("Folder name").performTextReplacement("Home")
        compose.onNodeWithText("Save folder").performClick()
        compose.onNodeWithText("Add starter folder").performScrollTo().performClick()
        compose.onNodeWithText("Folder name").performTextInput("World maps")
        compose.onNodeWithText("Save folder").performClick()
        compose.onNodeWithText("World maps").performScrollTo().assertExists()
        screenshot("structure-settings")
        compose.onNodeWithText("Library").performClick()
        compose.onNodeWithContentDescription("Import backup").assertIsDisplayed()
        compose.onNodeWithTag("new-workspace").performClick()
        compose.onNodeWithText("Novel title").performTextInput("The Two Worlds")
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Add genre"))
        compose.onNodeWithText("Add genre").performClick()
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Custom genre"))
        compose.onNodeWithText("Custom genre").performTextInput("Historical fiction")
        compose.onNodeWithText("Add", substring = false).performClick()
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Standard structure"))
        screenshot("creation-modes")
        compose.onNodeWithText("Create workspace").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Your story, at a glance").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("workspace-tab-0").assertIsDisplayed()
        screenshot("workspace-overview")
        compose.onNodeWithText("Open novel overview").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Back to workspace").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Home").assertExists()
        screenshot("overview-guide")
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNodeWithContentDescription("Edit workspace details").performClick()
        compose.onNodeWithText("Fantasy").assertExists()
        compose.onNodeWithText("Historical fiction").assertExists()
        compose.onNodeWithTag("novel-details-form").performScrollToNode(hasText("Writing status"))
        compose.onNodeWithText("In progress").performClick()
        compose.onNodeWithText("Completed").performClick()
        compose.onNodeWithTag("synopsis-input").assertDoesNotExist()
        screenshot("workspace-details")
        compose.onNodeWithText("Save changes").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Save changes").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Files").performClick()
        compose.onNodeWithTag("explorer-tree").performScrollToNode(hasText("World maps"))
        compose.onNodeWithText("World maps").assertExists()
        screenshot("workspace")
        compose.onNodeWithContentDescription("Close file explorer").performClick()
        compose.onNodeWithTag("workspace-tab-1").performClick()
        screenshot("workspace-plan")
        compose.onNodeWithTag("workspace-tab-2").performClick()
        screenshot("workspace-write")
        compose.onNodeWithTag("workspace-tab-3").performClick()
        screenshot("workspace-arrange")
        compose.onNodeWithContentDescription("Move Chapter 2 earlier").performClick()
        compose.onAllNodesWithText("Remove from manuscript").onFirst().performClick()
        compose.onNodeWithText("Add document").performClick()
        compose.onNodeWithText("Add to manuscript").assertExists()
        compose.onNodeWithText("A new piece of your story").assertDoesNotExist()
        compose.onNode(hasText("Chapter 2") and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText("Add selected (1)").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Add to manuscript").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("workspace-tab-4").performClick()
        screenshot("workspace-read")
        compose.onNodeWithText("Read from the beginning").performClick()
        compose.onNodeWithContentDescription("File explorer").performClick()
        compose.onNodeWithContentDescription("Close file explorer").performClick()
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNodeWithContentDescription("Back to library").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Filter").performClick()
        compose.onNodeWithText("All statuses").performClick()
        compose.onNodeWithText("Completed").performClick()
        compose.onNodeWithText("All genres").performClick()
        compose.onNodeWithText("Historical fiction").performClick()
        screenshot("library-filter")
        compose.onNodeWithText("Apply filters").performClick()
        compose.onAllNodesWithText("The Two Worlds").onLast().performClick()
        compose.onNodeWithContentDescription("Workspace actions").performClick()
        compose.onNodeWithText("Delete workspace").performClick()
        compose.onNodeWithText("Delete entire workspace?").assertExists()
        screenshot("delete-confirmation")
        compose.onNodeWithText("Keep workspace").performClick()
        compose.onNodeWithContentDescription("Workspace actions").performClick()
        compose.onNodeWithText("Delete workspace").performClick()
        compose.onNode(hasText("Delete workspace") and hasAnyAncestor(isDialog())).performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithTag("new-workspace").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("The Two Worlds").assertDoesNotExist()
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Writing").performScrollTo().performClick()
        compose.onNodeWithText("Workspace structure").performClick()
        compose.onNodeWithText("Restore default structure").performScrollTo().performClick()
    }
}
