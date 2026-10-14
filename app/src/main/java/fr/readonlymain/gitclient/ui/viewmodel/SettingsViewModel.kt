package fr.readonlymain.gitclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.readonlymain.gitclient.data.model.GitConfig
import fr.readonlymain.gitclient.data.model.GitCredential
import fr.readonlymain.gitclient.data.model.UiEvent
import fr.readonlymain.gitclient.data.preferences.CredentialsPreferences
import fr.readonlymain.gitclient.data.preferences.GitConfigPreferences
import fr.readonlymain.gitclient.data.preferences.ThemePreferences
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/** Only public metadata is exposed to Compose, never saved credential tokens. */
data class CredentialSummary(val id: String, val accountName: String, val username: String)

data class SettingsUiState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val gitConfig: GitConfig = GitConfig(),
    val credentials: List<CredentialSummary> = emptyList(),
    val credentialsReadable: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val credentialsPreferences: CredentialsPreferences,
    private val gitConfigPreferences: GitConfigPreferences,
    private val themePreferences: ThemePreferences
) : ViewModel() {
    private val state = MutableStateFlow(SettingsUiState())
    val uiState = state.asStateFlow()
    private val events = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = events.receiveAsFlow()
    private val writes = Mutex()
    private val observers = mutableListOf<Job>()

    init { reload() }

    fun reload() {
        observers.forEach { it.cancel() }
        observers.clear()
        state.update { it.copy(credentialsReadable = false) }
        observe(credentialsPreferences.credentialsFlow, "Cannot read saved credentials", {
            state.update { it.copy(credentialsReadable = false) }
        }) { credentials ->
            state.update { it.copy(credentialsReadable = true, credentials = credentials.map { item ->
                CredentialSummary(item.id, item.accountName, item.username)
            }) }
        }
        observe(gitConfigPreferences.gitConfigurationFlow, "Cannot read Git configuration") { config ->
            state.update { it.copy(gitConfig = config) }
        }
        observe(themePreferences.getTheme(), "Cannot read theme preferences") { theme ->
            state.update { it.copy(theme = theme) }
        }
    }

    private fun <T> observe(flow: Flow<T>, failure: String, onFailure: () -> Unit = {}, accept: (T) -> Unit) {
        observers += viewModelScope.launch {
            try {
                flow.collect { accept(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                onFailure()
                // Exception payloads can contain sensitive data; never expose them here.
                events.send(UiEvent.Error(failure))
            }
        }
    }

    fun addCredential(name: String, username: String, token: String, onSuccess: () -> Unit = {}) =
        write("Cannot save Git credential", onSuccess) {
            check(state.value.credentialsReadable)
            require(name.isNotBlank() && username.isNotBlank() && token.isNotBlank())
            credentialsPreferences.addCredential(GitCredential(accountName = name, username = username, token = token))
        }

    fun deleteCredential(id: String) = write("Cannot delete Git credential") {
        check(state.value.credentialsReadable)
        credentialsPreferences.deleteCredential(id)
    }

    fun setGitConfig(name: String, email: String, onSuccess: () -> Unit = {}) =
        write("Cannot save Git configuration", onSuccess) {
            require(name.isNotBlank() && email.isNotBlank())
            gitConfigPreferences.setGitConfig(state.value.gitConfig.copy(name = name, email = email))
        }

    fun setTheme(theme: ThemeMode) = write("Cannot save theme preference") { themePreferences.setTheme(theme) }

    private fun write(failure: String, onSuccess: () -> Unit = {}, action: suspend () -> Unit): Job =
        viewModelScope.launch {
            writes.withLock {
                state.update { it.copy(isSaving = true) }
                try {
                    action()
                    onSuccess()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    events.send(UiEvent.Error(failure))
                } finally {
                    state.update { it.copy(isSaving = false) }
                }
            }
        }
}
