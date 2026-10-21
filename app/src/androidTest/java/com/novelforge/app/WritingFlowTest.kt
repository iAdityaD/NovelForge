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
        compose.waitUntil(10000) {
            compose.onAllNodesWithText("New workspace", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithText("New workspace", useUnmergedTree = true).onLast().performClick()
        compose.onNodeWithText("Novel title").performTextInput("The Glass World")
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("03_Lore"))
        compose.onNodeWithText("03_Lore").performClick()
        compose.onNodeWithText("Create workspace").performClick()
        compose.waitUntil(10000) {
            compose
                .onAllNodesWithContentDescription("New folder")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("03_Lore").assertDoesNotExist()
        compose.onNodeWithContentDescription("New folder").performClick()
        compose.onNodeWithText("Folder name").performTextInput("World Two")
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("World Two").performClick()
        compose.onNodeWithContentDescription("New file").performClick()
        compose.onNodeWithText("Note title").performTextInput("Magic system")
        compose.onNodeWithText("Create note").performClick()
        compose.waitUntil(10000) {
            compose
                .onAllNodesWithContentDescription("Manuscript text")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose
            .onNodeWithContentDescription("Manuscript text")
            .performTextReplacement("Magic has a cost.")
        compose.onNodeWithContentDescription("Read this page").performClick()
        compose.onNodeWithText("Magic has a cost.").assertExists()
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNodeWithContentDescription("Expand World Two").performClick()
        compose.onNodeWithText("Magic system.md").assertExists()
        compose.onNodeWithContentDescription("Actions for Magic system").performClick()
        compose.onNodeWithText("Move").performClick()
        compose.onNodeWithText("Workspace root").performClick()
        compose.onNodeWithContentDescription("Back to library").performClick()
        compose.onNodeWithText("Analytics").performClick()
        compose.onNodeWithText("Your writing portfolio").assertExists()
        compose.onNodeWithText("All workspaces").performClick()
        compose.onNodeWithText("The Glass World").performClick()
        compose.onNodeWithText("MANUSCRIPT GOAL").assertExists()
        compose.onNodeWithText("Your writing portfolio").assertDoesNotExist()
    }
}
