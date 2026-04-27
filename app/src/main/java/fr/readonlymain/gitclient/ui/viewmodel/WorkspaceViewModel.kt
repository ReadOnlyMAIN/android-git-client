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
    private val credentialsPreferences: CredentialsPreferences
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    var repoName = mutableStateOf("")
        private set

    var branchName = mutableStateOf("")
        private set

    var repositories = mutableStateOf<List<Repository>>(emptyList())
        private set
    var branches = mutableStateOf<List<Branch>>(emptyList())
        private set

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

    var commitsByRepo = mutableStateOf<List<CommitInfo>>(emptyList())
        private set

    var isLoadingCommits = mutableStateOf(false)
        private set

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
                val actualBranch = gitManager.checkoutBranch(selectedRepo, newBranch)
                repositoriesPreferences.saveSelectedBranch(actualBranch)
                branchName.value = actualBranch

                refreshBranches(selectedRepo)
                refreshCommitList(selectedRepo)
            }
        }
    }

    private suspend fun selectRepo(repoPath: String) {
        val repoInfo = repositories.value.find { it.localPath == repoPath }
        repoName.value = repoInfo?.name ?: repoPath.substringAfterLast("/")
        branchName.value = repoInfo?.defaultBranch ?: "main"

        refreshBranches(repoPath)
    }

    private suspend fun refreshBranches(repoPath: String) {
        val rawBranches = gitManager.getBranchesFullRefs(repoPath)
        branches.value = transformRefsToBranches(rawBranches)
    }

    private suspend fun refreshCommitList(repoPath: String) {
        isLoadingCommits.value = true
        val commits = gitManager.getCommits(repoPath, branchName.value)
        commitsByRepo.value = commits
        isLoadingCommits.value = false
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

    }

    fun onPush() {

    }
}