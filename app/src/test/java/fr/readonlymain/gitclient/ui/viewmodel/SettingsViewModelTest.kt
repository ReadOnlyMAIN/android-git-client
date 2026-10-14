package fr.readonlymain.gitclient.ui.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.GitConfig
import fr.readonlymain.gitclient.data.model.GitCredential
import fr.readonlymain.gitclient.data.model.UiEvent
import fr.readonlymain.gitclient.data.preferences.*
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import fr.readonlymain.gitclient.util.MainDispatcherRule
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val credentials = FakeCredentialsPreferences()
    private val config = FakeGitConfigPreferences()
    private val theme = FakeThemePreferences()

    @Test fun `preferences are projected without exposing stored tokens`() = runTest {
        credentials.addCredential(GitCredential(accountName = "account", username = "user", token = "do-not-expose"))
        config.setGitConfig(GitConfig(name = "Author", email = "author@example.invalid"))
        theme.setTheme(ThemeMode.DARK)
        val vm = SettingsViewModel(credentials, config, theme)
        assertThat(vm.uiState.value.credentials.single().accountName).isEqualTo("account")
        assertThat(vm.uiState.value.toString()).doesNotContain("do-not-expose")
        assertThat(vm.uiState.value.gitConfig.name).isEqualTo("Author")
        assertThat(vm.uiState.value.theme).isEqualTo(ThemeMode.DARK)
    }

    @Test fun `read failure is retained until UI subscribes and does not crash`() = runTest {
        val failing = object : CredentialsPreferences by credentials {
            override val credentialsFlow = flow<List<GitCredential>> { error("sensitive-payload") }
        }
        val vm = SettingsViewModel(failing, config, theme)
        assertThat(vm.uiState.value.credentialsReadable).isFalse()
        val error = vm.uiEvent.first() as UiEvent.Error
        assertThat(error.message).isEqualTo("Cannot read saved credentials")
        assertThat(error.message).doesNotContain("sensitive-payload")
        vm.setTheme(ThemeMode.DARK).join()
        assertThat(vm.uiState.value.theme).isEqualTo(ThemeMode.DARK)
    }

    @Test fun `failed writes do not invoke success callbacks or corrupt existing state`() = runTest {
        val failing = object : CredentialsPreferences by credentials {
            override suspend fun addCredential(credential: GitCredential) { error("token-secret") }
        }
        val vm = SettingsViewModel(failing, config, theme)
        var callback = false
        vm.addCredential("account", "user", "token") { callback = true }.join()
        assertThat(callback).isFalse()
        assertThat(vm.uiState.value.isSaving).isFalse()
        val error = vm.uiEvent.first() as UiEvent.Error
        assertThat(error.message).isEqualTo("Cannot save Git credential")
        assertThat(error.message).doesNotContain("token-secret")
        assertThat(credentials.credentialsFlow.first()).isEmpty()
    }

    @Test fun `successful writes update data and close the form only after persistence`() = runTest {
        val vm = SettingsViewModel(credentials, config, theme)
        var saved = false
        vm.addCredential("account", "user", "token") { saved = true }.join()
        assertThat(saved).isTrue()
        val credential = credentials.credentialsFlow.first().single()
        vm.deleteCredential(credential.id).join()
        assertThat(credentials.credentialsFlow.first()).isEmpty()
        val id = vm.uiState.value.gitConfig.id
        vm.setGitConfig("Author", "author@example.invalid").join()
        assertThat(config.gitConfigurationFlow.first().id).isEqualTo(id)
    }

    @Test fun `reload recovers after a transient credential read failure`() = runTest {
        var fail = true
        val flaky = object : CredentialsPreferences by credentials {
            override val credentialsFlow = flow {
                if (fail) error("unavailable")
                emit(emptyList<GitCredential>())
            }
        }
        val vm = SettingsViewModel(flaky, config, theme)
        vm.uiEvent.first()
        assertThat(vm.uiState.value.credentialsReadable).isFalse()
        fail = false
        vm.reload()
        assertThat(vm.uiState.value.credentialsReadable).isTrue()
    }

    @Test fun `cancelled write clears saving state without emitting an error`() = runTest {
        val slow = object : CredentialsPreferences by credentials {
            override suspend fun addCredential(credential: GitCredential) = awaitCancellation()
        }
        val vm = SettingsViewModel(slow, config, theme)
        vm.uiEvent.test {
            val job = vm.addCredential("account", "user", "token")
            assertThat(vm.uiState.value.isSaving).isTrue()
            job.cancel(); job.join()
            assertThat(vm.uiState.value.isSaving).isFalse()
            expectNoEvents()
        }
    }

    @Test fun `unreadable credentials cannot be overwritten by an add action`() = runTest {
        val failing = object : CredentialsPreferences by credentials {
            override val credentialsFlow = flow<List<GitCredential>> { error("lost key") }
        }
        val vm = SettingsViewModel(failing, config, theme)
        vm.uiEvent.first()
        vm.addCredential("account", "user", "token").join()
        assertThat(credentials.credentialsFlow.first()).isEmpty()
        assertThat(vm.uiEvent.first()).isInstanceOf(UiEvent.Error::class.java)
    }
}
