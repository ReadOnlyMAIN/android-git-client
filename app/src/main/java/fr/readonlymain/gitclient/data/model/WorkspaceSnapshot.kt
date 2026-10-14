package fr.readonlymain.gitclient.data.model

/** One consistent read of a repository, independent of Compose and presentation state. */
data class WorkspaceSnapshot(
    val branch: String,
    val branches: List<Branch>,
    val commits: List<CommitInfo>,
    val ahead: Int,
    val behind: Int,
    val status: RepositoryStatus
)
