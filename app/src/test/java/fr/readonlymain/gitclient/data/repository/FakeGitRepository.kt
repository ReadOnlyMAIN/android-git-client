package fr.readonlymain.gitclient.data.repository

import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.CloneResult
import fr.readonlymain.gitclient.data.model.CommitInfo
import fr.readonlymain.gitclient.data.model.GitConfig
import fr.readonlymain.gitclient.data.model.GitCredential
import fr.readonlymain.gitclient.data.model.Repository

class FakeGitRepository : GitRepository {
    var commitsToReturn = listOf<CommitInfo>()
    var branchesToReturn = listOf<String>()
    var statusToReturn = mapOf("unstaged" to emptySet<String>(), "staged" to emptySet<String>())
    var trackingStatusToReturn = Pair(0, 0)
    var shouldFail = false
    var lastError = "Fake Repository Error"

    var lastActionCalled: String? = null
    var lastFilePatterns: List<String>? = null

    private fun <T> handleResult(value: T): Result<T> {
        return if (shouldFail) Result.failure(Exception(lastError)) else Result.success(value)
    }

    override suspend fun cloneRepo(
        url: String,
        credentials: List<GitCredential>,
        localPath: String,
        onProgress: (String, Float) -> Unit
    ): Result<CloneResult> {
        lastActionCalled = "cloneRepo"
        onProgress("Cloning...", 0.5f)
        return handleResult(CloneResult(url, "/fake/path", "fake_user"))
    }

    override suspend fun importExistingRepo(localPath: String): Result<CloneResult> {
        lastActionCalled = "importExistingRepo"
        return handleResult(CloneResult("https://fake.url", "/fake/path", ""))
    }

    override suspend fun getBranchesFullRefs(repoPath: String): Result<List<String>> {
        lastActionCalled = "getBranchesFullRefs"
        return handleResult(branchesToReturn)
    }

    override suspend fun checkoutBranch(repoPath: String, branch: Branch): Result<String> {
        lastActionCalled = "checkoutBranch"
        return handleResult(branch.name)
    }

    override suspend fun getTrackingStatus(
        repoPath: String,
        branchName: String
    ): Result<Pair<Int, Int>> {
        lastActionCalled = "getTrackingStatus"
        return handleResult(trackingStatusToReturn)
    }

    override suspend fun fetch(repoPath: String, credentials: List<GitCredential>): Result<Unit> {
        lastActionCalled = "fetch"
        return handleResult(Unit)
    }

    override suspend fun pull(repoPath: String, credentials: List<GitCredential>): Result<Unit> {
        lastActionCalled = "pull"
        return handleResult(Unit)
    }

    override suspend fun push(repoPath: String, credentials: List<GitCredential>): Result<Unit> {
        lastActionCalled = "push"
        return handleResult(Unit)
    }

    override suspend fun getCommits(repoPath: String, branchName: String?): List<CommitInfo> {
        lastActionCalled = "getCommits"
        return commitsToReturn
    }

    override suspend fun getRepoStatus(repoPath: String): Map<String, Set<String>> {
        lastActionCalled = "getRepoStatus"
        return statusToReturn
    }

    override suspend fun resetRepository(repoPath: String): Result<Unit> {
        lastActionCalled = "resetRepository"
        return handleResult(Unit)
    }

    override suspend fun discardFiles(repoPath: String, filePatterns: List<String>): Result<Unit> {
        lastActionCalled = "discardFiles"
        lastFilePatterns = filePatterns
        return handleResult(Unit)
    }

    override suspend fun stageFiles(repoPath: String, filePatterns: List<String>): Result<Unit> {
        lastActionCalled = "stageFiles"
        lastFilePatterns = filePatterns
        return handleResult(Unit)
    }

    override suspend fun stageAll(repoPath: String): Result<Unit> {
        lastActionCalled = "stageAll"
        return handleResult(Unit)
    }

    override suspend fun unstageFiles(repoPath: String, filePatterns: List<String>?): Result<Unit> {
        lastActionCalled = "unstageFiles"
        lastFilePatterns = filePatterns
        return handleResult(Unit)
    }

    override suspend fun commit(
        repoPath: String,
        gitConfig: GitConfig,
        message: String
    ): Result<Unit> {
        lastActionCalled = "commit"
        return handleResult(Unit)
    }

    override suspend fun editRepository(repo: Repository): Result<Unit> {
        lastActionCalled = "editRepository"
        return handleResult(Unit)
    }
}
