package com.example.fittrack

import android.content.res.Configuration
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.view.WindowCompat
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun themeSelectionUpdatesTheAppAndSurvivesActivityRecreation() {
        openSettings()
        try {
            selectTheme("Light theme")
            assertLightSystemBars(expected = true)
            selectTheme("Dark theme")
            assertLightSystemBars(expected = false)

            compose.onNodeWithContentDescription("Back").performClick()
            assertLightSystemBars(expected = false)
            compose.activityRule.scenario.recreate()
            openSettings()
            compose.onNodeWithText("Dark theme").assertIsSelected()
            assertLightSystemBars(expected = false)

            selectTheme("System default")
            val nightMode = compose.activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            assertLightSystemBars(expected = nightMode != Configuration.UI_MODE_NIGHT_YES)
        } finally {
            // Restore the default for other tests if Settings is still open.
            val options = compose.onAllNodes(hasText("System default"))
            if (options.fetchSemanticsNodes().isNotEmpty()) selectTheme("System default")
        }
    }

    private fun openSettings() {
        compose.onNodeWithText("Settings").performScrollTo().performClick()
    }

    private fun selectTheme(label: String) {
        val option = compose.onNodeWithText(label)
        option.performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            val nodes = compose.onAllNodes(hasText(label) and
                isSelected())
            nodes.fetchSemanticsNodes().isNotEmpty()
        }
        option.assertIsSelected()
    }

    private fun assertLightSystemBars(expected: Boolean) {
        compose.runOnIdle {
            val window = compose.activity.window
            val systemBars = WindowCompat.getInsetsController(window, window.decorView)
            assertEquals(expected, systemBars.isAppearanceLightStatusBars)
        }
    }
}
