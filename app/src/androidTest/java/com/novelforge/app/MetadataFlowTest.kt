package com.novelforge.app

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
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
        compose.onNodeWithText("Project title").performTextInput("The Two Worlds")
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Optional details"))
        compose.onNodeWithText("Optional details").performClick()
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Add genre"))
        compose.onNodeWithText("Add genre").performClick()
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Custom genre"))
        compose.onNodeWithText("Custom genre").performTextInput("Historical fiction")
        compose.onNodeWithText("Add", substring = false).performClick()
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Standard structure"))
        screenshot("creation-modes")
        compose.onNodeWithText("Create workspace").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Open writing workspace").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("workspace-tab-0").assertIsDisplayed()
        compose.waitUntil(10000) {
            val insets = androidx.core.view.ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
            insets?.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()) != true
        }
        screenshot("workspace-overview")
        compose.onNodeWithTag("workspace-content").performScrollToNode(hasText("Open project notes"))
        compose.onNodeWithText("Open project notes").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Back to workspace").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Manuscript text").assertExists()
        screenshot("overview-guide")
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Back to library").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Edit workspace details").performClick()

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
        compose.onNodeWithTag("workspace-tab-2").performClick()
        compose.onNodeWithText("Coming soon").assertIsDisplayed()
        screenshot("workspace-ai")
        compose.onNodeWithTag("workspace-tab-1").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Open file explorer").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Open file explorer").performClick()
        compose.onNodeWithTag("explorer-tree").performScrollToNode(hasText("Manuscript"))
        compose.onNodeWithContentDescription("Expand Manuscript").performClick()
        compose.onNodeWithContentDescription("Outline options").performClick()
        compose.onNodeWithText("Reorder files").performClick()
        compose.onNodeWithTag("explorer-tree").performScrollToNode(hasText("Chapter 2.md"))
        screenshot("workspace-write-outline")

        val firstBounds = compose.onNodeWithText("Chapter 1.md").fetchSemanticsNode().boundsInRoot
        val secondBounds = compose.onNodeWithText("Chapter 2.md").fetchSemanticsNode().boundsInRoot
        compose.onNodeWithText("Chapter 2.md").performTouchInput {
            down(center)
            advanceEventTime(700)
            moveTo(Offset(center.x, firstBounds.top + 3f - secondBounds.top))
            advanceEventTime(100)
            up()
        }
        compose.waitUntil(10000) {
            compose.onNodeWithText("Chapter 2.md").fetchSemanticsNode().boundsInRoot.top <
                compose.onNodeWithText("Chapter 1.md").fetchSemanticsNode().boundsInRoot.top
        }
        screenshot("arrange-after-drag")
        compose.onNodeWithContentDescription("Actions for Chapter 2").performClick()
        compose.onNodeWithText("Move down").performClick()
        compose.onNodeWithText("Remove from manuscript").assertDoesNotExist()
        compose.onNodeWithTag("workspace-tab-3").assertDoesNotExist()
        compose.onNodeWithText("Chapter 1.md").assertExists()
        compose.onNodeWithText("Chapter 2.md").assertExists()
        compose.onNodeWithContentDescription("Close file explorer").performClick()
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNodeWithContentDescription("Back to library").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Filter").performClick()
        compose.onNodeWithText("All statuses").performClick()
        compose.onNodeWithText("Completed").performClick()
        compose.onNodeWithText("All genres").performClick()
        compose.onNode(hasText("Historical fiction") and hasAnyAncestor(isPopup())).performClick()
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
