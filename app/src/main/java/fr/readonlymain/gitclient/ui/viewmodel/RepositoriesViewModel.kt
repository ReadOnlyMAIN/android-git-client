package fr.readonlymain.gitclient.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.readonlymain.gitclient.data.model.CloneResult
import fr.readonlymain.gitclient.data.model.GitCredential
import fr.readonlymain.gitclient.data.model.Repository
import fr.readonlymain.gitclient.data.model.UiEvent
import fr.readonlymain.gitclient.data.preferences.RepositoriesPreferences
import fr.readonlymain.gitclient.data.repository.GitManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing repository-related operations such as cloning and importing.
 *
 * It tracks the state of ongoing operations and provides progress updates to the UI.
 *
 * @property gitManager The [GitManager] used to perform the actual Git operations.
 * @property isCloning Indicates whether a cloning operation is currently in progress.
 * @property progressTask A description of the current task being performed during cloning.
 * @property progress The completion percentage (from 0.0 to 1.0) of the current cloning task.
 * @property uiEvent A shared flow that emits [CloneResult] events to notify the UI of operation outcomes.
 */
@HiltViewModel
class RepositoriesViewModel @Inject constructor(
    private val gitManager: GitManager,
    private val repositoriesPreferences: RepositoriesPreferences,
) : ViewModel() {
    var isCloning by mutableStateOf(false)
        private set

    var progressTask by mutableStateOf("")
        private set

    var progress by mutableFloatStateOf(0f)
        private set

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    /**
     * Initiates the cloning process of a Git repository from a remote URL.
     */
    fun startClone(url: String, credentials: List<GitCredential>, uri: Uri) {
        viewModelScope.launch {
            isCloning = true
            val result = gitManager.cloneRepo(url, credentials, uri) { task, p ->
                progressTask = task
                progress = p
            }
            isCloning = false
            result.onSuccess { cloneResult ->
                saveRepoData(cloneResult.repoUrl, cloneResult.folderPath, cloneResult.username)
                _uiEvent.emit(UiEvent.Success("Successfully cloned repository."))
            }.onFailure { error ->
                _uiEvent.emit(UiEvent.Error("Error: Can't clone repository ($error)"))
            }
        }
    }

    /**
     * Starts the process of importing an existing Git repository from the specified [Uri].
     *
     * This function launches a coroutine to call the [GitManager], and once the operation
     * is complete, it emits the resulting [CloneResult] to the [uiEvent] flow.
     *
     * @param uri The [Uri] representing the local directory of the existing repository to import.
     */
    fun startImport(uri: Uri) {
        viewModelScope.launch {
            val result = gitManager.importExistingRepo(uri)
            result.onSuccess { cloneResult ->
                saveRepoData(cloneResult.repoUrl, cloneResult.folderPath, cloneResult.username)
                _uiEvent.emit(UiEvent.Success("Successfully imported repository."))
            }.onFailure { error ->
                _uiEvent.emit(UiEvent.Error("Error: Can't import repository ($error)"))
            }
        }
    }

    private suspend fun saveRepoData(repoUrl: String, folderPath: String, username: String) {
        val folderName = folderPath.substringAfterLast("/")

        repositoriesPreferences.addRepository(
            Repository(
                name = folderName,
                remoteUrl = repoUrl,
                localPath = folderPath,
                username = username
            )
        )
    }

    fun editRepository(updatedRepo: Repository) {
        viewModelScope.launch {
            val result = gitManager.editRepository(updatedRepo)
            result.onSuccess {
                repositoriesPreferences.updateRepository(updatedRepo)
            }.onFailure { error ->
                _uiEvent.emit(UiEvent.Error("Error: Can't update repository ${updatedRepo.name} ($error)"))
            }
        }
    }
}
