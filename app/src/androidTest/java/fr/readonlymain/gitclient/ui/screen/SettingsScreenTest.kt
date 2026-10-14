package fr.readonlymain.gitclient.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.GitCredential
import fr.readonlymain.gitclient.data.preferences.*
import fr.readonlymain.gitclient.ui.components.ObserveUiEvents
import fr.readonlymain.gitclient.ui.theme.GitClientTheme
import fr.readonlymain.gitclient.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun content(preferences: CredentialsPreferences = FakeCredentialsPreferences()): SettingsViewModel {
        lateinit var viewModel: SettingsViewModel
        compose.runOnUiThread {
            viewModel = SettingsViewModel(preferences, FakeGitConfigPreferences(), FakeThemePreferences())
        }
        compose.setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbar = remember { SnackbarHostState() }
            GitClientTheme(state.theme) {
                ObserveUiEvents(viewModel.uiEvent, snackbar)
                Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
                    Box(Modifier.padding(padding)) {
                    SettingsScreen(state.theme, { viewModel.setTheme(it) }, state,
                        { name, user, token, success -> viewModel.addCredential(name, user, token, success) },
                        { viewModel.deleteCredential(it) },
                        { name, email, success -> viewModel.setGitConfig(name, email, success) })
                    }
                }
            }
        }
        return viewModel
    }

    @Test fun credential_read_error_is_visible_and_settings_remain_usable() {
        val failing = object : CredentialsPreferences by FakeCredentialsPreferences() {
            override val credentialsFlow = flow<List<GitCredential>> { error("secret-must-not-appear") }
        }
        content(failing)
        compose.onNodeWithText("Cannot read saved credentials").assertExists()
        compose.onNodeWithText("secret-must-not-appear", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Dark").performClick()
        compose.onNodeWithText("Theme").assertExists()
    }

    @Test fun credential_form_closes_only_after_successful_save() {
        val vm = content()
        compose.onNodeWithText("Add Git credential").performScrollTo().performClick()
        compose.onNodeWithText("Account name").performTextInput("validation")
        compose.onNodeWithText("Username / Email").performTextInput("test-user")
        compose.onNodeWithText("Personal Access Token (PAT)").performTextInput("test-token")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil { vm.uiState.value.credentials.size == 1 }
        compose.onNodeWithText("Add Git credentials").assertDoesNotExist()
        assertThat(vm.uiState.value.credentials.single().accountName).isEqualTo("validation")
    }

    @Test fun failed_save_keeps_form_open_and_displays_safe_error() {
        val failing = object : CredentialsPreferences by FakeCredentialsPreferences() {
            override suspend fun addCredential(credential: GitCredential) { error("secret-write-error") }
        }
        content(failing)
        compose.onNodeWithText("Add Git credential").performScrollTo().performClick()
        compose.onNodeWithText("Account name").performTextInput("validation")
        compose.onNodeWithText("Username / Email").performTextInput("test-user")
        compose.onNodeWithText("Personal Access Token (PAT)").performTextInput("test-token")
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("Add Git credentials").assertExists()
        // The dialog remains open; the snackbar belongs to the underlying screen.
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Cannot save Git credential").assertExists()
        compose.onNodeWithText("secret-write-error", substring = true).assertDoesNotExist()
    }
}
