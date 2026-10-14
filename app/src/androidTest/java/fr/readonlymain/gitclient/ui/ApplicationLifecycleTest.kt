package fr.readonlymain.gitclient.ui

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import fr.readonlymain.gitclient.MainActivity
import org.junit.Rule
import org.junit.Test

/** Exercise the actual Hilt graph and Android saved state, not only a fake screen. */
class ApplicationLifecycleTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun settings_destination_and_persisted_theme_survive_activity_recreation() {
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("Dark").performClick()
        compose.onNodeWithText("Dark").assertIsSelected()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Theme").assertExists()
        compose.onNodeWithText("Dark").assertIsSelected()
        // Leave the disposable test installation in its default theme.
        compose.onNodeWithText("System").performClick()
    }
}
