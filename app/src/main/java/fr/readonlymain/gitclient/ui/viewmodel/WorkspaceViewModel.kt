package fr.readonlymain.gitclient.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.Repository
import fr.readonlymain.gitclient.data.model.UiEvent
import fr.readonlymain.gitclient.data.model.canonicalRepositoryPath
import fr.readonlymain.gitclient.data.preferences.CredentialsPreferences
import fr.readonlymain.gitclient.data.preferences.GitConfigPreferences
import fr.readonlymain.gitclient.data.preferences.RepositoriesPreferences
import fr.readonlymain.gitclient.data.repository.GitRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val gitRepository: GitRepository,
    private val repositoriesPreferences: RepositoriesPreferences,
    private val credentialsPreferences: CredentialsPreferences,
    private val gitConfigPreferences: GitConfigPreferences
) : ViewModel() {
    private val state = mutableStateOf(WorkspaceUiState())
    val uiState: State<WorkspaceUiState> = state
    private fun <T> select(read: (WorkspaceUiState) -> T): State<T> = derivedStateOf { read(state.value) }

    // Compatibility projections keep all existing composables unchanged and read-only.
    val repoName = select { it.repositoryName }
    val branchName = select { it.branchName }
    val branches = select { it.branches }
    val needPull = select { it.needPull }
    val needPush = select { it.needPush }
    val unstagedFiles = select { it.status.unstaged }
    val stagedFiles = select { it.status.staged }
    val selectedUnstagedFiles = select { it.selectedUnstaged }
    val selectedStagedFiles = select { it.selectedStaged }
    val commitsByRepo = select { it.commits }
    val isLoadingCommits = select { it.isLoading }
    val isPushing = select { it.isPushing }
    val isPulling = select { it.isPulling }
    val isSynchronizing = select { it.isSynchronizing }

    private val events = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = events.receiveAsFlow()
    val repositories: StateFlow<List<Repository>> = repositoriesPreferences.repositoriesFlow.catch { error ->
        if (error is CancellationException) throw error
        events.send(UiEvent.Error("Cannot read saved repositories"))
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    private val actions = Mutex()
    private val refreshes = Mutex()
    private var version = 0L
    private var pushJob: Job? = null
    private var pullJob: Job? = null
    private var syncJob: Job? = null
    private var selectionJob: Job? = null

    init {
        viewModelScope.launch {
            repositoriesPreferences.selectedRepoFlow.catch { error ->
                if (error is CancellationException) throw error
                events.send(UiEvent.Error("Cannot read selected repository"))
            }.distinctUntilChanged().collectLatest { path ->
                val generation = ++version
                state.value = WorkspaceUiState(repositoryPath = path?.let(::canonicalRepositoryPath))
                safely {
                    if (path != null) {
                        val repository = repositoriesPreferences.repositoriesFlow.first().find {
                            canonicalRepositoryPath(it.localPath) == canonicalRepositoryPath(path)
                        }
                        if (generation != version) return@safely
                        state.value = state.value.copy(
                            repositoryName = repository?.name ?: java.io.File(path).name
                        )
                        refresh(path, generation)
                    }
                }
            }
        }
    }

    fun onRepositorySelected(repoPath: String) {
        val path = canonicalRepositoryPath(repoPath)
        if (state.value.repositoryPath == path) return
        val previous = state.value
        selectionJob?.cancel()
        // Invalidate in-flight loads immediately, not after the next DataStore emission.
        version++
        val generation = version
        state.value = WorkspaceUiState(repositoryPath = path)
        selectionJob = viewModelScope.launch {
            try {
                repositoriesPreferences.resetSelectedBranch()
                repositoriesPreferences.saveSelectedRepo(path)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (version == generation) {
                    version++
                    state.value = previous
                }
                events.send(UiEvent.Error("Cannot save selected repository"))
            }
        }
    }

    fun onBranchSelected(newBranch: Branch) {
        if (branchName.value == newBranch.name) return
        action { path, generation ->
            val actualBranch = gitRepository.checkoutBranch(path, newBranch).getOrThrow()
            if (!current(path, generation)) return@action
            val nextGeneration = ++version
            state.value = state.value.copy(branchName = actualBranch, selectedUnstaged = emptySet(), selectedStaged = emptySet())
            repositoriesPreferences.saveSelectedBranch(actualBranch)
            refreshAfterOperation(Result.success(Unit)) { refresh(path, nextGeneration) }
        }
    }

    fun onBranchCreated(branchName: String, sourceBranch: Branch, overwriteExisting: Boolean) {
        action { path, generation ->
            gitRepository.createBranch(path, branchName, sourceBranch, overwriteExisting).getOrThrow()
            refreshAfterOperation(Result.success(Unit)) { refresh(path, generation) }
            events.send(UiEvent.Success("Branch $branchName created based on branch ${sourceBranch.name}"))
        }
    }

    fun onBranchDeleted(branch: Branch, deleteRemote: Boolean, forceDelete: Boolean) {
        action { path, generation ->
            val result = gitRepository.deleteBranch(path, branch, credentialsPreferences.credentialsFlow.first(), deleteRemote, forceDelete)
            refreshAfterOperation(result) { refresh(path, generation) }
            events.send(UiEvent.Success("Branch ${branch.name} deleted"))
        }
    }

    fun onSynchronize() {
        if (syncJob?.isActive == true) { syncJob?.cancel(); return }
        syncJob = remote(RemoteAction.FETCH)
    }

    fun onPull() {
        if (pullJob?.isActive == true) { pullJob?.cancel(); return }
        pullJob = remote(RemoteAction.PULL)
    }

    fun onPush() {
        if (pushJob?.isActive == true) { pushJob?.cancel(); return }
        pushJob = remote(RemoteAction.PUSH)
    }

    private enum class RemoteAction { FETCH, PULL, PUSH }

    private fun remote(operation: RemoteAction): Job = action { path, generation ->
        fun busy(value: Boolean) {
            if (current(path, generation)) state.value = when (operation) {
                RemoteAction.FETCH -> state.value.copy(isSynchronizing = value)
                RemoteAction.PULL -> state.value.copy(isPulling = value)
                RemoteAction.PUSH -> state.value.copy(isPushing = value)
            }
        }
        busy(true)
        try {
            val credentials = credentialsPreferences.credentialsFlow.first()
            val result = when (operation) {
                RemoteAction.FETCH -> gitRepository.fetch(path, credentials)
                RemoteAction.PULL -> gitRepository.pull(path, credentials)
                RemoteAction.PUSH -> gitRepository.push(path, credentials)
            }
            // A failed pull can still change the worktree or enter a conflicted state.
            refreshAfterOperation(result) { refresh(path, generation) }
        } finally {
            busy(false)
        }
    }

    fun toggleUnstagedFileSelection(filePath: String) {
        state.value = state.value.copy(selectedUnstaged = toggle(state.value.selectedUnstaged, filePath))
    }

    fun toggleStagedFileSelection(filePath: String) {
        state.value = state.value.copy(selectedStaged = toggle(state.value.selectedStaged, filePath))
    }

    private fun toggle(selection: Set<String>, path: String) =
        if (path in selection) selection - path else selection + path

    fun stageSelection() {
        val files = selectedUnstagedFiles.value.toList()
        if (files.isEmpty()) return
        mutate("${files.size} files staged") { gitRepository.stageFiles(it, files) }
    }

    fun stageAll() {
        if (unstagedFiles.value.isEmpty()) return
        mutate("All changes staged") { gitRepository.stageAll(it) }
    }

    fun unstageSelection() {
        val files = selectedStagedFiles.value.toList()
        if (files.isEmpty()) return
        mutate("${files.size} files unstaged") { gitRepository.unstageFiles(it, files) }
    }

    fun unstageAll() = mutate("All files unstaged") { gitRepository.unstageFiles(it, null) }

    fun discardSelection() {
        val selection = selectedUnstagedFiles.value
        val files = (selection.ifEmpty { unstagedFiles.value }).toList()
        if (files.isEmpty()) return
        mutate(if (selection.isEmpty()) "All changes discarded" else "Changes discarded for ${files.size} files") {
            gitRepository.discardFiles(it, files)
        }
    }

    private fun mutate(message: String, mutation: suspend (String) -> Result<Unit>) {
        action { path, generation ->
            val result = mutation(path)
            refreshAfterOperation(result) { refreshStatus(path, generation) }
            if (current(path, generation)) {
                state.value = state.value.copy(selectedUnstaged = emptySet(), selectedStaged = emptySet())
            }
            events.send(UiEvent.Success(message))
        }
    }

    fun onCommit(message: String, onCommitSuccess: () -> Unit) {
        if (message.isBlank() || stagedFiles.value.isEmpty()) return
        action { path, generation ->
            gitRepository.commit(path, gitConfigPreferences.gitConfigurationFlow.first(), message).getOrThrow()
            refreshAfterOperation(Result.success(Unit)) { refresh(path, generation) }
            events.send(UiEvent.Success("Successfully commited"))
            if (current(path, generation)) onCommitSuccess()
        }
    }

    /** Capture the context at invocation, before waiting for a previous mutation. */
    private fun action(block: suspend (String, Long) -> Unit): Job {
        val path = state.value.repositoryPath
        val generation = version
        return viewModelScope.launch {
            safely {
                if (path == null) return@safely
                actions.withLock {
                    if (current(path, generation)) block(path, generation)
                }
            }
        }
    }

    private suspend fun safely(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            events.send(UiEvent.Error("Error: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    private fun current(path: String, generation: Long) =
        generation == version && state.value.repositoryPath == canonicalRepositoryPath(path)

    private suspend fun refresh(path: String, generation: Long) = refreshes.withLock {
        if (!current(path, generation)) return@withLock
        state.value = state.value.copy(isLoading = true)
        try {
            val snapshot = gitRepository.readWorkspace(path)
            val status = snapshot.status
            if (current(path, generation)) {
                state.value = state.value.copy(
                    branchName = snapshot.branch,
                    branches = snapshot.branches, commits = snapshot.commits,
                    needPush = snapshot.ahead > 0, needPull = snapshot.behind > 0, status = status,
                    selectedUnstaged = state.value.selectedUnstaged.intersect(status.unstaged),
                    selectedStaged = state.value.selectedStaged.intersect(status.staged)
                )
            }
        } finally {
            if (current(path, generation)) state.value = state.value.copy(isLoading = false)
        }
    }

    private suspend fun refreshStatus(path: String, generation: Long) = refreshes.withLock {
        val status = gitRepository.getRepoStatus(path)
        if (current(path, generation)) state.value = state.value.copy(
            status = status,
            selectedUnstaged = state.value.selectedUnstaged.intersect(status.unstaged),
            selectedStaged = state.value.selectedStaged.intersect(status.staged)
        )
    }

    /** A refresh failure must never turn a completed Git mutation into a reported Git failure. */
    private suspend fun refreshAfterOperation(result: Result<Unit>, refresh: suspend () -> Unit) {
        try {
            refresh()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val original = result.exceptionOrNull()
            if (original != null) original.addSuppressed(e)
            else events.send(UiEvent.Error("Operation completed, but repository information could not be refreshed"))
        }
        result.getOrThrow()
    }

}
