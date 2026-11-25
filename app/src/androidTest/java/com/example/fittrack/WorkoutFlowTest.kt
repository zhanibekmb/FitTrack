package com.example.fittrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import com.example.fittrack.data.repository.FitTrackContainer
import java.util.Locale

class WorkoutFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun awaitStorage() {
        compose.waitUntil(timeoutMillis = 10_000) { !FitTrackContainer.repository.data.value.isLoading }
    }

    @Test
    fun createEditAndDeleteAPlan() {
        compose.onNodeWithText("Workout Plans").performScrollTo().performClick()
        compose.onNodeWithText("Create plan", ignoreCase = true).performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Defense Test Plan")
        compose.onNode(hasClickAction() and hasText("Create plan", ignoreCase = true)).performScrollTo().performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            FitTrackContainer.repository.data.value.plans.any { it.name == "Defense Test Plan" }
        }

        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Edit plan"))
        compose.onNodeWithText("Edit plan").performClick()
        compose.onNode(hasSetTextAction()).performTextReplacement("Renamed Defense Plan")
        compose.onNodeWithText("Save").performScrollTo().performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            FitTrackContainer.repository.data.value.plans.any { it.name == "Renamed Defense Plan" }
        }
        compose.onNodeWithText("Renamed Defense Plan").assertExists()

        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Delete plan"))
        compose.onNodeWithText("Delete plan").performClick()
        compose.onNode(hasClickAction() and hasText("Delete plan") and hasAnyAncestor(isDialog())).performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            FitTrackContainer.repository.data.value.plans.none { it.name == "Renamed Defense Plan" }
        }
        compose.onNodeWithText("Renamed Defense Plan").assertDoesNotExist()
        compose.onNodeWithText("Push Day").assertExists()
    }

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
        compose.waitUntil(timeoutMillis = 10_000) { FitTrackContainer.repository.data.value.activeSession == null }
        compose.onNodeWithText("Workout History").performScrollTo().performClick()
        compose.onAllNodesWithText("Dumbbell Bench Press").onFirst().assertExists()
        compose.onNodeWithText("View progress").performClick()
        compose.onNodeWithText("Exercise Progress").assertIsDisplayed()
        val weights = FitTrackContainer.repository.data.value.sessions.sortedBy { it.finishedAt }
            .mapNotNull { session -> session.completedSets.filter { it.exerciseId == "db-bench" }.maxOfOrNull { it.weightKg } }
        val best = String.format(Locale.ENGLISH, "Best weight: %.1f kg", weights.max())
        compose.onNodeWithText(best).assertExists()
        val description = weights.joinToString(", ") { String.format(Locale.ENGLISH, "%.1f", it) }
        compose.onNodeWithContentDescription("Best weights in chronological order: $description kilograms").assertExists()
    }
}
