package com.example.fittrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WorkoutFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun recordWorkoutAndSeeRealHistoryAndProgressInEnglish() {
        assertEquals("en", compose.activity.resources.configuration.locales[0].language)
        compose.onNodeWithText("Workout Plans").performScrollTo().performClick()
        compose.onNodeWithText("Push Day").performClick()
        compose.onNodeWithText("Start Workout").performClick()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasContentDescription("Set 1 completed"))
        compose.onAllNodesWithContentDescription("Set 1 completed").onFirst().performClick()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Finish Workout"))
        compose.onNodeWithText("Finish Workout").performClick()
        compose.onNode(hasText("Finish Workout") and hasClickAction() and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText("Workout History").performScrollTo().performClick()
        compose.onAllNodesWithText("Dumbbell Bench Press").onFirst().assertExists()
        compose.onNodeWithText("View progress").performClick()
        compose.onNodeWithText("Exercise Progress").assertIsDisplayed()
        compose.onNodeWithText("Best weight: 15.0 kg").assertExists()
        compose.onNodeWithContentDescription("Best weights in chronological order: 15.0 kilograms").assertExists()
    }
}
