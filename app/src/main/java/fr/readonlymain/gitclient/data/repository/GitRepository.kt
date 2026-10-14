package fr.readonlymain.gitclient.data.repository

import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.CloneResult
import fr.readonlymain.gitclient.data.model.CommitInfo
import fr.readonlymain.gitclient.data.model.GitConfig
import fr.readonlymain.gitclient.data.model.GitCredential
import fr.readonlymain.gitclient.data.model.Repository
import fr.readonlymain.gitclient.data.model.RepositoryStatus
import fr.readonlymain.gitclient.data.model.WorkspaceSnapshot
import fr.readonlymain.gitclient.data.model.branchesFromRefs

/**
 * Interface defining the Git operations supported by the application.
 */
interface GitRepository {
    suspend fun readWorkspace(repoPath: String): WorkspaceSnapshot {
        val branch = getCurrentBranch(repoPath).getOrThrow()
        val branches = getBranches(repoPath).getOrThrow()
        val commits = getCommits(repoPath, branch)
        val (ahead, behind) = getTrackingStatus(repoPath, branch).getOrThrow()
        return WorkspaceSnapshot(branch, branches, commits, ahead, behind, getRepoStatus(repoPath))
    }

    suspend fun getBranches(repoPath: String): Result<List<Branch>> =
        getBranchesFullRefs(repoPath).map { branchesFromRefs(it) }

    /** Actual symbolic branch, or the object ID when HEAD is detached. */
    suspend fun getCurrentBranch(repoPath: String): Result<String>

    /**
     * Clones a Git repository to a local directory.
     */
    suspend fun cloneRepo(
        url: String,
        credentials: List<GitCredential>,
        localPath: String,
        onProgress: (String, Float) -> Unit
    ): Result<CloneResult>

    /**
     * Imports an existing Git repository from a given local path.
     */
    suspend fun importExistingRepo(localPath: String): Result<CloneResult>

    /**
     * Retrieves the list of all branch references (local and remote).
     */
    suspend fun getBranchesFullRefs(repoPath: String): Result<List<String>>

    /**
     * Switches the working directory to the specified branch.
     */
    suspend fun checkoutBranch(repoPath: String, branch: Branch): Result<String>

    /**
     * Creates a new branch.
     */
    suspend fun createBranch(
        repoPath: String,
        newBranchName: String,
        sourceBranch: Branch,
        force: Boolean = false
    ): Result<Unit>

    /**
     * Deletes the specified branch.
     */
    suspend fun deleteBranch(
        repoPath: String,
        branch: Branch,
        credentials: List<GitCredential>,
        deleteRemote: Boolean = false,
        forceDelete: Boolean = false
    ): Result<Unit>

    /**
     * Gets the number of commits ahead and behind the remote tracking branch.
     */
    suspend fun getTrackingStatus(repoPath: String, branchName: String): Result<Pair<Int, Int>>

    /**
     * Fetches updates from the remote repository.
     */
    suspend fun fetch(repoPath: String, credentials: List<GitCredential>): Result<Unit>

    /**
     * Pulls changes from the remote repository and merges them into the current branch.
     */
    suspend fun pull(repoPath: String, credentials: List<GitCredential>): Result<Unit>

    /**
     * Pushes local commits to the remote repository.
     */
    suspend fun push(repoPath: String, credentials: List<GitCredential>): Result<Unit>

    /**
     * Retrieves the list of commits from a specific branch or the current HEAD.
     */
    suspend fun getCommits(repoPath: String, branchName: String? = null): List<CommitInfo>

    /**
     * Gets the status of the working directory (staged vs unstaged files).
     */
    suspend fun getRepoStatus(repoPath: String): RepositoryStatus

    /**
     * Performs a hard reset and cleans the working directory.
     */
    suspend fun resetRepository(repoPath: String): Result<Unit>

    /**
     * Discards changes in the specified files.
     */
    suspend fun discardFiles(repoPath: String, filePatterns: List<String>): Result<Unit>

    /**
     * Stages specific files for the next commit.
     */
    suspend fun stageFiles(repoPath: String, filePatterns: List<String>): Result<Unit>

    /**
     * Stages all changes in the working directory.
     */
    suspend fun stageAll(repoPath: String): Result<Unit>

    /**
     * Unstages specific files (reset from index).
     */
    suspend fun unstageFiles(repoPath: String, filePatterns: List<String>? = null): Result<Unit>

    /**
     * Creates a new commit with the staged changes.
     */
    suspend fun commit(repoPath: String, gitConfig: GitConfig, message: String): Result<Unit>

    /**
     * Updates the repository configuration (e.g., remote URL).
     */
    suspend fun editRepository(repo: Repository): Result<Unit>
}
