package com.novelforge.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WritingFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun sampleOpensEditorAndSavesBeforeNavigation() {
        compose.onNodeWithText("Explore a sample project").performScrollTo().performClick()
        compose.waitUntil(10000) {
            compose
                .onAllNodesWithText("Write", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Write").performScrollTo().performClick()
        compose.waitUntil(10000) {
            compose
                .onAllNodesWithContentDescription("Manuscript text")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose
            .onNodeWithContentDescription("Manuscript text")
            .performTextReplacement("A durable sentence survives navigation.")
        compose.onNodeWithContentDescription("Back to manuscript").performClick()
        compose.waitForIdle()
        val app = compose.activity.application as ForgeApplication
        val id = app.getSharedPreferences("preferences", 0).getLong("lastEntry", 0)
        runBlocking { assertTrue(app.db.dao().entry(id)!!.body.contains("durable sentence")) }
        compose.onNodeWithText("Timeline").performScrollTo().performClick()
        compose.onAllNodesWithText("Add event")[0].performClick()
        compose.onNodeWithText("Timeline event title").performTextInput("The first midnight")
        compose.onNodeWithText("Create timeline event").performClick()
        compose.waitUntil(20000) {
            compose
                .onAllNodesWithContentDescription("Manuscript text")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose
            .onNodeWithContentDescription("Manuscript text")
            .assertTextContains("## When", substring = true)
        compose.onNodeWithContentDescription("Back to manuscript").performClick()
        compose.onNodeWithText("Timeline").performScrollTo().performClick()
        compose.onNodeWithText("The first midnight").assertExists()
    }
}
