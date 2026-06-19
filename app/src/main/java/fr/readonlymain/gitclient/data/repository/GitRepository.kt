package fr.readonlymain.gitclient.data.repository

import android.net.Uri
import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.CloneResult
import fr.readonlymain.gitclient.data.model.CommitInfo
import fr.readonlymain.gitclient.data.model.GitConfig
import fr.readonlymain.gitclient.data.model.GitCredential
import fr.readonlymain.gitclient.data.model.Repository

/**
 * Interface defining the Git operations supported by the application.
 */
interface GitRepository {

    /**
     * Clones a Git repository to a local directory.
     */
    suspend fun cloneRepo(
        url: String,
        credentials: List<GitCredential>,
        treeUri: Uri,
        onProgress: (String, Float) -> Unit
    ): Result<CloneResult>

    /**
     * Imports an existing Git repository from a given directory URI.
     */
    suspend fun importExistingRepo(treeUri: Uri): Result<CloneResult>

    /**
     * Retrieves the list of all branch references (local and remote).
     */
    suspend fun getBranchesFullRefs(repoPath: String): Result<List<String>>

    /**
     * Switches the working directory to the specified branch.
     */
    suspend fun checkoutBranch(repoPath: String, branch: Branch): Result<String>

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
    suspend fun getRepoStatus(repoPath: String): Map<String, Set<String>>

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
