package fr.readonlymain.gitclient.ui.viewmodel

import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.CommitInfo
import fr.readonlymain.gitclient.data.model.RepositoryStatus

data class WorkspaceUiState(
    val repositoryPath: String? = null,
    val repositoryName: String = "",
    val branchName: String = "",
    val branches: List<Branch> = emptyList(),
    val commits: List<CommitInfo> = emptyList(),
    val status: RepositoryStatus = RepositoryStatus(),
    val selectedUnstaged: Set<String> = emptySet(),
    val selectedStaged: Set<String> = emptySet(),
    val needPull: Boolean = false,
    val needPush: Boolean = false,
    val isLoading: Boolean = false,
    val isPushing: Boolean = false,
    val isPulling: Boolean = false,
    val isSynchronizing: Boolean = false
)
