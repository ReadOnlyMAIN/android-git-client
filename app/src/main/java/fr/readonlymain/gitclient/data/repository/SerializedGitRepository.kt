package fr.readonlymain.gitclient.data.repository

import fr.readonlymain.gitclient.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

/** Serialize reads as well as writes so snapshots never observe a half-written index. */
@Singleton
class SerializedGitRepository @Inject constructor(
    private val delegate: DefaultGitRepository,
    private val coordinator: RepositoryOperationCoordinator
) : GitRepository {
    override suspend fun getCurrentBranch(repoPath: String) = locked(repoPath) { delegate.getCurrentBranch(repoPath) }
    override suspend fun cloneRepo(url: String, credentials: List<GitCredential>, localPath: String, onProgress: (String, Float) -> Unit) =
        locked(localPath) { delegate.cloneRepo(url, credentials, localPath, onProgress) }
    override suspend fun importExistingRepo(localPath: String) = locked(localPath) { delegate.importExistingRepo(localPath) }
    override suspend fun getBranchesFullRefs(repoPath: String) = locked(repoPath) { delegate.getBranchesFullRefs(repoPath) }
    override suspend fun checkoutBranch(repoPath: String, branch: Branch) = locked(repoPath) { delegate.checkoutBranch(repoPath, branch) }
    override suspend fun createBranch(repoPath: String, newBranchName: String, sourceBranch: Branch, force: Boolean) =
        locked(repoPath) { delegate.createBranch(repoPath, newBranchName, sourceBranch, force) }
    override suspend fun deleteBranch(repoPath: String, branch: Branch, credentials: List<GitCredential>, deleteRemote: Boolean, forceDelete: Boolean) =
        locked(repoPath) { delegate.deleteBranch(repoPath, branch, credentials, deleteRemote, forceDelete) }
    override suspend fun getTrackingStatus(repoPath: String, branchName: String) = locked(repoPath) { delegate.getTrackingStatus(repoPath, branchName) }
    override suspend fun fetch(repoPath: String, credentials: List<GitCredential>) = locked(repoPath) { delegate.fetch(repoPath, credentials) }
    override suspend fun pull(repoPath: String, credentials: List<GitCredential>) = locked(repoPath) { delegate.pull(repoPath, credentials) }
    override suspend fun push(repoPath: String, credentials: List<GitCredential>) = locked(repoPath) { delegate.push(repoPath, credentials) }
    override suspend fun getCommits(repoPath: String, branchName: String?) = locked(repoPath) { delegate.getCommits(repoPath, branchName) }
    override suspend fun getRepoStatus(repoPath: String) = locked(repoPath) { delegate.getRepoStatus(repoPath) }
    override suspend fun resetRepository(repoPath: String) = locked(repoPath) { delegate.resetRepository(repoPath) }
    override suspend fun discardFiles(repoPath: String, filePatterns: List<String>) = locked(repoPath) { delegate.discardFiles(repoPath, filePatterns) }
    override suspend fun stageFiles(repoPath: String, filePatterns: List<String>) = locked(repoPath) { delegate.stageFiles(repoPath, filePatterns) }
    override suspend fun stageAll(repoPath: String) = locked(repoPath) { delegate.stageAll(repoPath) }
    override suspend fun unstageFiles(repoPath: String, filePatterns: List<String>?) = locked(repoPath) { delegate.unstageFiles(repoPath, filePatterns) }
    override suspend fun commit(repoPath: String, gitConfig: GitConfig, message: String) = locked(repoPath) { delegate.commit(repoPath, gitConfig, message) }
    override suspend fun editRepository(repo: Repository) = locked(repo.localPath) { delegate.editRepository(repo) }

    private suspend fun <T> locked(path: String, operation: suspend () -> T): T = coordinator.withRepository(path, operation)
}
