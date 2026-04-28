package fr.readonlymain.gitclient.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.CommitInfo
import fr.readonlymain.gitclient.data.model.Repository
import fr.readonlymain.gitclient.data.model.UiEvent
import fr.readonlymain.gitclient.data.preferences.CredentialsPreferences
import fr.readonlymain.gitclient.data.preferences.GitConfigPreferences
import fr.readonlymain.gitclient.data.preferences.RepositoriesPreferences
import fr.readonlymain.gitclient.data.repository.GitManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.eclipse.jgit.lib.Repository as JGitRepository

@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val gitManager: GitManager,
    private val repositoriesPreferences: RepositoriesPreferences,
    private val credentialsPreferences: CredentialsPreferences,
    private val gitConfigPreferences: GitConfigPreferences
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    //region Variables
    var repoName = mutableStateOf("")
        private set

    var branchName = mutableStateOf("")
        private set

    var repositories = mutableStateOf<List<Repository>>(emptyList())
        private set
    var branches = mutableStateOf<List<Branch>>(emptyList())
        private set

    var needPull = mutableStateOf(false)
        private set

    var needPush = mutableStateOf(false)
        private set

    var unstagedFiles = mutableStateOf<Set<String>>(emptySet())
        private set

    var selectedUnstagedFiles = mutableStateOf<Set<String>>(emptySet())
        private set

    var stagedFiles = mutableStateOf<Set<String>>(emptySet())
        private set

    var selectedStagedFiles = mutableStateOf<Set<String>>(emptySet())
        private set

    var commitsByRepo = mutableStateOf<List<CommitInfo>>(emptyList())
        private set

    var isLoadingCommits = mutableStateOf(false)
        private set
    //endregion

    init {
        viewModelScope.launch {
            repositories.value = repositoriesPreferences.repositoriesFlow.first()
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            if (selectedRepo != null) {
                selectRepo(selectedRepo)
                val selectedBranchName = repositoriesPreferences.selectedBranchFlow.first()
                if (selectedBranchName != null) {
                    val branch = branches.value.find { it.name == selectedBranchName }
                    if (branch != null) {
                        onBranchSelected(branch)
                    } else {
                        refreshCommitList(selectedRepo)
                    }
                } else {
                    refreshCommitList(selectedRepo)
                }
            }
        }
    }

    //region Repository & Branch Actions
    fun onRepositorySelected(repoPath: String) {
        viewModelScope.launch {
            repositoriesPreferences.saveSelectedRepo(repoPath)
            repositoriesPreferences.resetSelectedBranch()

            selectRepo(repoPath)
            refreshCommitList(repoPath)
        }
    }

    fun onBranchSelected(newBranch: Branch) {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            if (selectedRepo != null) {
                val result = gitManager.checkoutBranch(selectedRepo, newBranch)
                result.onSuccess { actualBranch ->
                    repositoriesPreferences.saveSelectedBranch(actualBranch)
                    branchName.value = actualBranch

                    refreshBranches(selectedRepo)
                    refreshCommitList(selectedRepo)
                    _uiEvent.emit(UiEvent.Success("Successfully checkout on $actualBranch"))
                }.onFailure { error ->
                    _uiEvent.emit(UiEvent.Error("Error: Can't checkout on $newBranch ($error)"))
                }


            }
        }
    }
    //endregion

    //region Remote Operations
    fun onSynchronize() {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            val credentials = credentialsPreferences.credentialsFlow.first()
            if (selectedRepo != null) {
                val result = gitManager.fetch(selectedRepo, credentials)
                if (result.isSuccess) {
                    refreshCommitList(selectedRepo)
                    refreshBranches(selectedRepo)
                    _uiEvent.emit(UiEvent.Success("Successfully synchronized"))
                } else {
                    val error = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                    _uiEvent.emit(UiEvent.Error("Error: Can't synchronize ($error)"))
                }
            }
        }
    }

    fun onPull() {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            val credentials = credentialsPreferences.credentialsFlow.first()

            if (selectedRepo != null) {
                // Can add isLoading state here
                val result = gitManager.pull(selectedRepo, credentials)

                if (result.isSuccess) {
                    refreshCommitList(selectedRepo)
                    refreshBranches(selectedRepo)
                    _uiEvent.emit(UiEvent.Success("Successfully pulled current branch"))
                } else {
                    val error = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                    _uiEvent.emit(UiEvent.Error("Error: Can't pull ($error)"))
                }
            }
        }
    }

    fun onPush() {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            val credentials = credentialsPreferences.credentialsFlow.first()

            if (selectedRepo != null) {
                val result = gitManager.push(selectedRepo, credentials)
                if (result.isSuccess) {
                    refreshCommitList(selectedRepo)
                    _uiEvent.emit(UiEvent.Success("Successfully pushed refs"))
                } else {
                    val error = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                    _uiEvent.emit(UiEvent.Error("Error: Can't push some ref ($error)"))
                }
            }
        }
    }
    //endregion

    //region Selection Management
    fun toggleUnstagedFileSelection(filePath: String) {
        val currentSelection = selectedUnstagedFiles.value
        if (currentSelection.contains(filePath)) {
            selectedUnstagedFiles.value = currentSelection - filePath
        } else {
            selectedUnstagedFiles.value = currentSelection + filePath
        }
    }

    fun toggleStagedFileSelection(filePath: String) {
        val currentSelection = selectedStagedFiles.value
        if (currentSelection.contains(filePath)) {
            selectedStagedFiles.value = currentSelection - filePath
        } else {
            selectedStagedFiles.value = currentSelection + filePath
        }
    }

    //endregion

    //region Index & Staging Operations
    fun stageSelection() {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            val filesToStage = selectedUnstagedFiles.value.toList()

            if (selectedRepo != null && filesToStage.isNotEmpty()) {
                val result = gitManager.stageFiles(selectedRepo, filesToStage)

                result.onSuccess {
                    selectedUnstagedFiles.value = emptySet()
                    refreshCommitList(selectedRepo)
                    _uiEvent.emit(UiEvent.Success("${filesToStage.size} files staged"))
                }.onFailure { error ->
                    _uiEvent.emit(UiEvent.Error("Error: Failed to stage files (${error.localizedMessage})"))
                }
            }
        }
    }

    fun stageAll() {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()

            if (selectedRepo != null && unstagedFiles.value.toList().isNotEmpty()) {
                val result = gitManager.stageAll(selectedRepo)

                result.onSuccess {
                    selectedUnstagedFiles.value = emptySet()
                    refreshRepoStatus(selectedRepo)
                    _uiEvent.emit(UiEvent.Success("All changes staged"))
                }.onFailure { error ->
                    _uiEvent.emit(UiEvent.Error("Error: Failed to stage all (${error.localizedMessage})"))
                }
            }
        }
    }

    fun unstageSelection() {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            val filesToUnstage = selectedStagedFiles.value.toList()

            if (selectedRepo != null && filesToUnstage.isNotEmpty()) {
                val result = gitManager.unstageFiles(selectedRepo, filesToUnstage)

                result.onSuccess {
                    selectedStagedFiles.value = emptySet()
                    refreshRepoStatus(selectedRepo)
                    _uiEvent.emit(UiEvent.Success("${filesToUnstage.size} files unstaged"))
                }.onFailure { error ->
                    _uiEvent.emit(UiEvent.Error("Error: Failed to unstage selection (${error.localizedMessage})"))
                }
            }
        }
    }

    fun unstageAll() {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            if (selectedRepo != null) {
                val result = gitManager.unstageFiles(selectedRepo, null)

                result.onSuccess {
                    selectedStagedFiles.value = emptySet()
                    refreshRepoStatus(selectedRepo)
                    _uiEvent.emit(UiEvent.Success("All files unstaged"))
                }.onFailure { error ->
                    _uiEvent.emit(UiEvent.Error("Error: Failed to unstage all (${error.localizedMessage})"))
                }
            }
        }
    }

    fun discardSelection() {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()

            if (selectedRepo != null) {
                val filesToDiscard = selectedUnstagedFiles.value.toList()

                if (filesToDiscard.isNotEmpty()) {
                    val result = gitManager.discardFiles(selectedRepo, filesToDiscard)

                    result.onSuccess {
                        selectedUnstagedFiles.value = emptySet()
                        refreshCommitList(selectedRepo)
                        _uiEvent.emit(UiEvent.Success("Changes discarded for ${filesToDiscard.size} files"))
                    }.onFailure { error ->
                        _uiEvent.emit(UiEvent.Error("Failed to discard changes: ${error.localizedMessage}"))
                    }
                } else { // Discard all changes if no selection (TODO: Add confirmation dialog)
                    val allUnstagedFiles = unstagedFiles.value.toList()
                    val result = gitManager.discardFiles(selectedRepo, allUnstagedFiles)

                    result.onSuccess {
                        refreshCommitList(selectedRepo)
                        _uiEvent.emit(UiEvent.Success("All changes discarded"))
                    }.onFailure { error ->
                        _uiEvent.emit(UiEvent.Error("Error: Failed to discard all changes (${error.localizedMessage})"))
                    }
                }
            }
        }
    }

    //endregion

    //region Refresh Logic
    private suspend fun refreshBranches(repoPath: String) {
        val result = gitManager.getBranchesFullRefs(repoPath)

        result.onSuccess { rawBranches ->
            branches.value = transformRefsToBranches(rawBranches)
        }.onFailure { error ->
            _uiEvent.emit(UiEvent.Error("Error: Failed to get branches (${error.localizedMessage})"))
        }
    }

    private suspend fun refreshCommitList(repoPath: String) {
        isLoadingCommits.value = true

        val commits = gitManager.getCommits(repoPath, branchName.value)
        commitsByRepo.value = commits

        val result = gitManager.getTrackingStatus(repoPath, branchName.value)

        result.onSuccess { (ahead, behind) ->
            needPull.value = behind > 0
            needPush.value = ahead > 0
            refreshRepoStatus(repoPath)
        }.onFailure { error ->
            _uiEvent.emit(UiEvent.Error("Error: Failed to get tracking status (${error.localizedMessage})"))
        }

        isLoadingCommits.value = false
    }

    private suspend fun refreshRepoStatus(repoPath: String) {
        val status = gitManager.getRepoStatus(repoPath)
        unstagedFiles.value = status["unstaged"] ?: emptySet()
        stagedFiles.value = status["staged"] ?: emptySet()
    }
    //endregion

    //region Internal Helpers
    private suspend fun selectRepo(repoPath: String) {
        val repoInfo = repositories.value.find { it.localPath == repoPath }
        repoName.value = repoInfo?.name ?: repoPath.substringAfterLast("/")
        branchName.value = repoInfo?.defaultBranch ?: "main"

        refreshBranches(repoPath)
    }

    private fun transformRefsToBranches(refs: List<String>): List<Branch> {
        val branchMap = mutableMapOf<String, Branch>()

        refs.forEach { ref ->
            val isLocal = ref.startsWith("refs/heads/")
            val isRemote = ref.startsWith("refs/remotes/")

            val name = when {
                isLocal -> ref.removePrefix("refs/heads/")
                isRemote -> ref.removePrefix("refs/remotes/")
                    .substringAfter("/") // Retire "origin/"
                else -> JGitRepository.shortenRefName(ref)
            }

            val existing = branchMap[name]

            branchMap[name] = Branch(
                name = name,
                isLocal = isLocal || (existing?.isLocal ?: false),
                isRemote = isRemote || (existing?.isRemote ?: false),
                localRef = if (isLocal) ref else existing?.localRef,
                remoteRef = if (isRemote) ref else existing?.remoteRef
            )
        }

        return branchMap.values.toList().sortedBy { it.name }
    }

    //endregion

    fun onCommit(message: String, onCommitSuccess: () -> Unit) {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first() ?: return@launch
            val gitConfig = gitConfigPreferences.gitConfigurationFlow.first()

            if (message.isBlank()) {
                return@launch
            }
            if (stagedFiles.value.isEmpty()) {
                return@launch
            }

            val result = gitManager.commit(selectedRepo, gitConfig, message)

            result.onSuccess {
                refreshCommitList(selectedRepo)
                refreshRepoStatus(selectedRepo)

                _uiEvent.emit(UiEvent.Success("Successfully commited"))
                onCommitSuccess()
            }.onFailure { error ->
                _uiEvent.emit(UiEvent.Error("Error: Failed to commit (${error.localizedMessage})"))
            }
        }
    }


}