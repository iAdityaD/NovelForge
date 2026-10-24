package com.novelforge.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WritingFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test(timeout = 90000)
    fun createsCustomWorkspaceFileAndAnalytics() {
        compose.onNodeWithTag("new-workspace").performClick()
        compose.onNodeWithText("Project title").performTextInput("The Glass World")
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Build from scratch"))
        compose.onNodeWithText("Build from scratch").performClick()
        compose.onNodeWithText("Create workspace").performClick()
        compose.waitUntil(10000) {
            compose
                .onAllNodesWithText("Files")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithTag("workspace-tab-1").performClick()
        compose.onNodeWithText("Collection").assertDoesNotExist()
        compose.onNodeWithContentDescription("Add planning item").performClick()
        compose.onNodeWithText("New folder").performClick()
        compose.onNodeWithText("Folder name").performTextInput("My own category")
        compose.onNodeWithText("Create").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("My own category").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Add planning item").performClick()
        compose.onNodeWithText("New resource").performClick()
        compose.onNodeWithText("Title").performTextInput("My first resource")
        compose.onNodeWithText("Type label (optional)").performTextInput("Ideas")
        compose.onNodeWithText("Create").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Manuscript text").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Back to library").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("My own category").assertExists()
        compose.onNodeWithContentDescription("Expand My own category").performClick()
        compose.onNodeWithText("My first resource.md").assertExists()
        compose.onNodeWithTag("workspace-tab-0").performClick()
        compose.onNodeWithTag("workspace-content").performScrollToNode(hasContentDescription("Edit synopsis"))
        compose.onNodeWithContentDescription("Edit synopsis").performClick()
        compose.onNodeWithText("Short story summary").performTextInput("Two worlds share one secret.")
        compose.onNodeWithText("Save synopsis").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Two worlds share one secret.").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Recent documents").assertDoesNotExist()
        compose.onNodeWithText("Files").performClick()
        compose.onNodeWithText("Manuscript").assertDoesNotExist()
        compose.onNodeWithContentDescription("New folder").performClick()
        compose.onNodeWithText("Folder name").performTextInput("World Two")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("World Two").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("World Two").performClick()
        compose.onNodeWithContentDescription("New file").performClick()
        compose.onNodeWithText("Title").performTextInput("Magic system")
        compose.onNodeWithText("Create").performClick()
        compose.waitUntil(10000) {
            compose
                .onAllNodesWithContentDescription("Manuscript text")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose
            .onNodeWithContentDescription("Manuscript text")
            .performTextReplacement("Magic has a cost.")
        compose.onNodeWithText("Magic has a cost.").assertExists()
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Back to library").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Files").performClick()
        compose.onNodeWithText("Magic system.md").assertExists()
        compose.onNodeWithContentDescription("Actions for Magic system").performClick()
        compose.onNodeWithText("Move").performClick()
        compose.onNode(hasText("Workspace root") and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithContentDescription("Close file explorer").performClick()
        compose.onNodeWithContentDescription("Back to library").performClick()
        compose.onNodeWithText("Analytics").performClick()
        compose.onNodeWithText("Your writing portfolio").assertExists()
        compose.onNodeWithText("All workspaces").performClick()
        compose.onNodeWithText("The Glass World").performClick()
        compose.onNodeWithText("MANUSCRIPT GOAL").assertExists()
        compose.onNodeWithText("Your writing portfolio").assertDoesNotExist()
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Writing").performScrollTo().performClick()
        compose.onNodeWithText("My templates").performClick()
        compose.onNodeWithText("Save project structure").performClick()
        compose.onNodeWithText("Template name").performTextInput("Reusable layout")
        compose.onNodeWithText("Save template").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Reusable layout").fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Reusable layout").assertExists()
        compose.onNodeWithText("Library").performClick()
        compose.onNodeWithTag("new-workspace").performClick()
        compose.onNodeWithText("Project title").performTextInput("From my layout")
        compose.onNodeWithText("Novel", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Reusable layout · My template").performClick()
        compose.onNodeWithText("Create workspace").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Files").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Files").performClick()
        compose.onNodeWithText("My own category").assertExists()
        compose.onNodeWithText("World Two").assertExists()
        compose.onNodeWithText("Magic system.md").assertDoesNotExist()
    }
}
