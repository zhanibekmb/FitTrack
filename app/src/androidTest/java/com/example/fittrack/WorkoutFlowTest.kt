package com.example.fittrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WorkoutFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun createEditAndDeleteAPlan() {
        compose.onNodeWithText("Workout Plans").performScrollTo().performClick()
        compose.onNodeWithText("Create plan", ignoreCase = true).performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Defense Test Plan")
        compose.onNode(hasClickAction() and hasText("Create plan", ignoreCase = true)).performScrollTo().performClick()

        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Edit plan"))
        compose.onNodeWithText("Edit plan").performClick()
        compose.onNode(hasSetTextAction()).performTextReplacement("Renamed Defense Plan")
        compose.onNodeWithText("Save").performScrollTo().performClick()
        compose.onNodeWithText("Renamed Defense Plan").assertExists()

        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Delete plan"))
        compose.onNodeWithText("Delete plan").performClick()
        compose.onNode(hasClickAction() and hasText("Delete plan") and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText("Renamed Defense Plan").assertDoesNotExist()
        compose.onNodeWithText("Push Day").assertExists()
    }

    @Test
    fun recordWorkoutAndSeeRealHistoryAndProgressInEnglish() {
        assertEquals("en", compose.activity.resources.configuration.locales[0].language)
        compose.onNodeWithText("Workout Plans").performScrollTo().performClick()
        compose.onNodeWithText("Push Day").performClick()
        compose.onNodeWithText("Start Workout").performClick()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Record Set"))
        compose.onAllNodesWithText("Record Set").onFirst().performClick()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Finish Workout"))
        compose.onNodeWithText("Finish Workout").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Workout History").performScrollTo().performClick()
        compose.onAllNodesWithText("Dumbbell Bench Press").onFirst().assertExists()
        compose.onNodeWithText("View progress").performClick()
        compose.onNodeWithText("Exercise Progress").assertIsDisplayed()
        compose.onNodeWithText("Best weight: 15.0 kg").assertExists()
        compose.onNodeWithContentDescription("Best weights in chronological order: 15.0 kilograms").assertExists()
    }
}
