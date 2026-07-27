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
import fr.readonlymain.gitclient.data.repository.GitRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.eclipse.jgit.lib.Repository as JGitRepository

@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val gitRepository: GitRepository,
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

    val repositories: StateFlow<List<Repository>> = repositoriesPreferences.repositoriesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            //started = SharingStarted.WhileSubscribed(5000),
            //started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

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

    var isPushing = mutableStateOf(false)
        private set

    var isPulling = mutableStateOf(false)
        private set

    var isSynchronizing = mutableStateOf(false)
        private set
    //endregion

    init {
        viewModelScope.launch {
            repositoriesPreferences.selectedRepoFlow.collect { selectedRepo ->
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
                } else {
                    // Clear state when no repository is selected
                    repoName.value = ""
                    branchName.value = ""
                    branches.value = emptyList()
                    commitsByRepo.value = emptyList()
                    unstagedFiles.value = emptySet()
                    stagedFiles.value = emptySet()
                    selectedUnstagedFiles.value = emptySet()
                    selectedStagedFiles.value = emptySet()
                    needPull.value = false
                    needPush.value = false
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
                val result = gitRepository.checkoutBranch(selectedRepo, newBranch)
                result.onSuccess { actualBranch ->
                    repositoriesPreferences.saveSelectedBranch(actualBranch)
                    branchName.value = actualBranch

                    refreshBranches(selectedRepo)
                    refreshCommitList(selectedRepo)
                    //_uiEvent.emit(UiEvent.Success("Successfully checkout on $actualBranch"))
                }.onFailure { error ->
                    _uiEvent.emit(UiEvent.Error("Error: Can't checkout on $newBranch ($error)"))
                }
            }
        }
    }

    fun onBranchCreated(branchName: String, sourceBranch: Branch, overwriteExisting: Boolean) {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            if (selectedRepo != null) {
                val result = gitRepository.createBranch(
                    repoPath = selectedRepo,
                    newBranchName = branchName,
                    sourceBranch = sourceBranch,
                    force = overwriteExisting
                )
                result.onSuccess {
                    refreshBranches(selectedRepo)
                    _uiEvent.emit(UiEvent.Success("Branch $branchName created based on branch ${sourceBranch.name}"))
                }.onFailure { error ->
                    _uiEvent.emit(UiEvent.Error("Error: Can't create branch $branchName (${error.localizedMessage})"))
                }
            }
        }
    }

    fun onBranchDeleted(branch: Branch, deleteRemote: Boolean, forceDelete: Boolean) {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            val credentials = credentialsPreferences.credentialsFlow.first()
            if (selectedRepo != null) {
                val result = gitRepository.deleteBranch(
                    repoPath = selectedRepo,
                    branch = branch,
                    credentials = credentials,
                    deleteRemote = deleteRemote,
                    forceDelete = forceDelete
                )
                result.onSuccess {
                    refreshBranches(selectedRepo)
                    _uiEvent.emit(UiEvent.Success("Branch ${branch.name} deleted"))
                }.onFailure { error ->
                    _uiEvent.emit(UiEvent.Error("Error: Can't delete branch ${branch.name} (${error.localizedMessage})"))
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
                isSynchronizing.value = true
                try {
                    val result = gitRepository.fetch(selectedRepo, credentials)
                    if (result.isSuccess) {
                        refreshCommitList(selectedRepo)
                        refreshBranches(selectedRepo)
                        //_uiEvent.emit(UiEvent.Success("Successfully synchronized"))
                    } else {
                        val error = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                        _uiEvent.emit(UiEvent.Error("Error: Can't synchronize ($error)"))
                    }
                } finally {
                    isSynchronizing.value = false
                }
            }
        }
    }

    fun onPull() {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            val credentials = credentialsPreferences.credentialsFlow.first()

            if (selectedRepo != null) {
                isPulling.value = true
                try {
                    val result = gitRepository.pull(selectedRepo, credentials)

                    if (result.isSuccess) {
                        refreshCommitList(selectedRepo)
                        refreshBranches(selectedRepo)
                        //_uiEvent.emit(UiEvent.Success("Successfully pulled refs"))
                    } else {
                        val error = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                        _uiEvent.emit(UiEvent.Error("Error: Can't pull ($error)"))
                    }
                } finally {
                    isPulling.value = false
                }
            }
        }
    }

    fun onPush() {
        viewModelScope.launch {
            val selectedRepo = repositoriesPreferences.selectedRepoFlow.first()
            val credentials = credentialsPreferences.credentialsFlow.first()

            if (selectedRepo != null) {
                isPushing.value = true
                try {
                    val result = gitRepository.push(selectedRepo, credentials)
                    if (result.isSuccess) {
                        refreshCommitList(selectedRepo)
                        refreshBranches(selectedRepo)
                        //_uiEvent.emit(UiEvent.Success("Successfully pushed refs"))
                    } else {
                        val error = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                        _uiEvent.emit(UiEvent.Error("Error: Can't push some ref ($error)"))
                    }
                } finally {
                    isPushing.value = false
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
                val result = gitRepository.stageFiles(selectedRepo, filesToStage)

                result.onSuccess {
                    selectedUnstagedFiles.value = emptySet()
                    //refreshCommitList(selectedRepo)
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
                val result = gitRepository.stageAll(selectedRepo)

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
                val result = gitRepository.unstageFiles(selectedRepo, filesToUnstage)

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
                val result = gitRepository.unstageFiles(selectedRepo, null)

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
                    val result = gitRepository.discardFiles(selectedRepo, filesToDiscard)

                    result.onSuccess {
                        selectedUnstagedFiles.value = emptySet()
                        refreshCommitList(selectedRepo)
                        _uiEvent.emit(UiEvent.Success("Changes discarded for ${filesToDiscard.size} files"))
                    }.onFailure { error ->
                        _uiEvent.emit(UiEvent.Error("Failed to discard changes: ${error.localizedMessage}"))
                    }
                } else { // Discard all changes if no selection (TODO: Add confirmation dialog)
                    val allUnstagedFiles = unstagedFiles.value.toList()
                    val result = gitRepository.discardFiles(selectedRepo, allUnstagedFiles)

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
        val result = gitRepository.getBranchesFullRefs(repoPath)

        result.onSuccess { rawBranches ->
            branches.value = transformRefsToBranches(rawBranches)
        }.onFailure { error ->
            _uiEvent.emit(UiEvent.Error("Error: Failed to get branches (${error.localizedMessage})"))
        }
    }

    private suspend fun refreshCommitList(repoPath: String) {
        isLoadingCommits.value = true

        val commits = gitRepository.getCommits(repoPath, branchName.value)
        commitsByRepo.value = commits

        val result = gitRepository.getTrackingStatus(repoPath, branchName.value)

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
        val status = gitRepository.getRepoStatus(repoPath)
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

            val result = gitRepository.commit(selectedRepo, gitConfig, message)

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