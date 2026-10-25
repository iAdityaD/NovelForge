package com.novelforge.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DocumentFirstFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test(timeout = 120000)
    fun attachesSourceAndSavesUntitledOnNavigation() {
        compose.onNodeWithTag("new-workspace").performClick()
        compose.onNodeWithText("Project title").performTextInput("Source and draft journey")
        compose.onNodeWithTag("workspace-form").performScrollToNode(hasText("Build from scratch"))
        compose.onNodeWithText("Build from scratch").performClick()
        compose.onNodeWithText("Create workspace").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Files").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("workspace-tab-1").performClick()
        compose.onNodeWithText("Attach link").performClick()
        compose.onNodeWithText("Link name").performTextInput("History source")
        compose.onNodeWithText("+ New folder").performClick()
        compose.onNodeWithText("New folder name").performTextInput("Research")
        compose.onNodeWithText("Website URL").performTextInput("https://example.com/history")
        compose.onNodeWithText("Attach", substring = false).performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Manuscript text").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Manuscript text").assertTextContains("https://example.com/history", substring = true)
        compose.onNodeWithContentDescription("Switch to read mode").performClick()
        compose.onNodeWithText("Open website").assertExists()
        compose.onNodeWithContentDescription("Switch to write mode").performClick()
        compose.onNodeWithContentDescription("Page actions").performClick()
        compose.onNodeWithText("New file").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Untitled").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Manuscript text").performTextReplacement("A new beginning.")
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNode(hasText("Save") and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText("Filename").performTextReplacement("Opening")
        compose.onNode(hasText("Save") and hasAnyAncestor(isDialog())).performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Files").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("workspace-tab-1").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Manuscript text").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Manuscript text").assertTextContains("A new beginning.")
        compose.onNodeWithContentDescription("Page actions").performClick()
        compose.onNodeWithText("New file").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Untitled").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Manuscript text").performTextReplacement("Discard me only.")
        compose.onNodeWithContentDescription("Back to workspace").performClick()
        compose.onNodeWithText("Discard", substring = false).performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("Files").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Files").performClick()
        compose.onNodeWithText("Opening.md").assertExists()
        compose.onNodeWithText("Untitled.md").assertDoesNotExist()
        compose.onNodeWithContentDescription("Expand Research").performClick()
        compose.onNodeWithText("History source.md").assertExists()
    }
}
