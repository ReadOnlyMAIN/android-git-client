package fr.readonlymain.gitclient.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import org.junit.Rule
import org.junit.Test

class AppNavHostTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun verify_startDestination_is_Workspace() {
        composeTestRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            AppNavHost(
                navController = rememberNavController(),
                themeMode = ThemeMode.SYSTEM,
                onThemeChange = {},
                snackbarHostState = snackbarHostState
            )
        }

        composeTestRule.onNodeWithText("Workspace").assertExists()
    }

    @Test
    fun navigate_to_settings() {
        composeTestRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            AppNavHost(
                navController = rememberNavController(),
                themeMode = ThemeMode.SYSTEM,
                onThemeChange = {},
                snackbarHostState = snackbarHostState
            )
        }

        // Simuler la navigation si vous avez un bouton "Settings"
        // composeTestRule.onNodeWithText("Settings").performClick()
        // composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
    }
}