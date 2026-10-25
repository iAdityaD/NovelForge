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

    @Test(timeout = 120000)
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
        compose.onNodeWithText("New file").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Manuscript text").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Manuscript text").performTextReplacement("Magic has a cost.")
        compose.onNodeWithContentDescription("Switch to read mode").performClick()
        compose.onNodeWithContentDescription("Manuscript text").assertDoesNotExist()
        compose.onNodeWithText("Magic has a cost.").assertExists()
        compose.onNodeWithContentDescription("Switch to write mode").performClick()
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNodeWithText("Keep draft").performClick()
        compose.onNodeWithText("Files").performClick()
        compose.onNodeWithText("Untitled.md").assertDoesNotExist()
        compose.onNodeWithContentDescription("Close file explorer").performClick()
        compose.onNodeWithTag("workspace-tab-1").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Save file").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Save file").performClick()
        compose.onNodeWithText("Filename").performTextReplacement("Magic system")
        compose.onNodeWithText("+ New folder").performClick()
        compose.onNodeWithText("New folder name").performTextInput("My own category")
        compose.onNode(hasText("Save") and hasAnyAncestor(isDialog())).performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Filename").fetchSemanticsNodes().isEmpty() }
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Manuscript text").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Manuscript text").assertTextContains("Magic has a cost.")
        compose.onNodeWithContentDescription("Open file explorer").performClick()
        compose.onNodeWithContentDescription("Expand My own category").performClick()
        compose.onNodeWithText("Magic system.md").assertExists()
        compose.onNodeWithText("My own category").performClick()
        compose.onNodeWithContentDescription("New folder").performClick()
        compose.onNodeWithText("Folder name").performTextInput("World Two")
        compose.onNode(hasText("Save") and hasAnyAncestor(isDialog())).performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("World Two").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Actions for Magic system").performClick()
        compose.onNodeWithText("Move").performClick()
        compose.onNode(hasText("Workspace root") and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithContentDescription("Close file explorer").performClick()
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNodeWithContentDescription("Back to library").performClick()
        compose.onNodeWithText("Analytics").performClick()
        compose.onNodeWithText("Your writing portfolio").assertExists()
        compose.onNodeWithText("All workspaces").performClick()
        compose.onNodeWithText("The Glass World").performClick()
        compose.onNodeWithText("MANUSCRIPT GOAL").assertExists()
        compose.onNodeWithText("Your writing portfolio").assertDoesNotExist()
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Writing").performScrollTo().performClick()
        compose.onNodeWithText("Custom fields").performClick()
        compose.onNodeWithText("Add field").performClick()
        compose.onNodeWithText("Field name").performTextInput("Atmosphere")
        compose.onNodeWithText("Save field").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Atmosphere").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Back to writing settings").performClick()
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
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Novel", substring = false))
        compose.onNodeWithText("Novel", substring = false).performClick()
        compose.onNodeWithText("Reusable layout · My template").performClick()
        compose.onNodeWithText("Create workspace").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Files").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Files").performClick()
        compose.onNodeWithText("My own category").assertExists()
        compose.onNodeWithContentDescription("Expand My own category").performClick()
        compose.onNodeWithText("World Two").assertExists()
        compose.onNodeWithText("Magic system.md").assertDoesNotExist()
    }
}
