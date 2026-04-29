package fr.readonlymain.gitclient.data.repository

import android.net.Uri
import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.CloneResult
import fr.readonlymain.gitclient.data.model.CommitInfo
import fr.readonlymain.gitclient.data.model.CommitStatus
import fr.readonlymain.gitclient.data.model.GitConfig
import fr.readonlymain.gitclient.data.model.GitCredential
import fr.readonlymain.gitclient.data.model.Repository
import fr.readonlymain.gitclient.utils.resolveUriToPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.BranchTrackingStatus
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class responsible for handling Git operations such as cloning remote repositories
 * and importing existing local repositories.
 *
 * This class leverages the JGit library to perform git actions and provides integration
 * with the Android file system by resolving [Uri] paths. It supports both public
 * anonymous access and authenticated access using [GitCredential].
 *
 */
@Singleton
class GitManager @Inject constructor() {
    //region Repo Management
    /**
     * Clones a Git repository to a local directory.
     *
     * This function attempts to clone a repository using a list of potential credentials,
     * starting with an anonymous (public) attempt. It resolves the provided [treeUri]
     * to a local file system path and creates a subfolder based on the repository URL.
     *
     * @param url The remote Git repository URL to clone.
     * @param credentials A list of [GitCredential] to attempt for authentication if public access fails.
     * @param treeUri The [Uri] representing the base directory where the repository should be cloned.
     * @param onProgress A callback invoked during the cloning process, providing a status message (String)
     * and the progress ratio (Float, ranging from 0.0 to 1.0, or -1.0 if progress is indeterminate).
     * @return A [CloneResult] indicating the success or failure of the operation, including the local path or an error message.
     */
    suspend fun cloneRepo(
        url: String,
        credentials: List<GitCredential>,
        treeUri: Uri,
        onProgress: (String, Float) -> Unit
    ): Result<CloneResult> = withContext(Dispatchers.IO) {
        val baseFolderPath = resolveUriToPath(treeUri)
            ?: return@withContext Result.failure(Exception("Can't resolve directory."))

        val folderName = url.substringAfterLast("/").replace(".git", "")
        val localFolder = File(baseFolderPath, folderName)

        if (localFolder.exists()) {
            return@withContext Result.failure(Exception("Folder already exists."))
        }

        // Attempts list : First null (public), then saved credentials
        val attempts = listOf<GitCredential?>(null) + credentials
        var lastErrorMessage = "Authentification Failed"

        for (cred in attempts) {
            try {
                if (localFolder.exists()) localFolder.deleteRecursively()
                localFolder.mkdirs()

                val statusText = if (cred == null) "Public access attempt..."
                else "Trying with ${cred.accountName}..."
                onProgress(statusText, -1f)

                val cloneCommand = Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(localFolder)

                if (cred != null) {
                    cloneCommand.setCredentialsProvider(
                        UsernamePasswordCredentialsProvider(cred.username, cred.token)
                    )
                }

                cloneCommand.setProgressMonitor(object : ProgressMonitor {
                    private var currentTask = ""
                    private var total = 0
                    private var current = 0

                    override fun start(totalTasks: Int) {}
                    override fun beginTask(title: String, totalWork: Int) {
                        currentTask = title
                        total = totalWork
                        current = 0
                        report()
                    }

                    override fun update(completed: Int) {
                        current += completed
                        report()
                    }

                    override fun endTask() {}
                    override fun isCancelled(): Boolean = false
                    override fun showDuration(enabled: Boolean) {}
                    private fun report() {
                        val p = if (total > 0) {
                            current.toFloat() / total
                        } else {
                            -1f
                        }
                        onProgress(currentTask, p)
                    }
                })

                Git.cloneRepository().apply {
                }

                val gitResult = cloneCommand.call()
                gitResult.close()

                return@withContext Result.success(
                    CloneResult(
                        url,
                        localFolder.absolutePath,
                        cred?.username ?: ""
                    )
                )

            } catch (e: Exception) {
                lastErrorMessage = e.localizedMessage ?: "Unknown error."
                continue
            }
        }

        if (localFolder.exists()) localFolder.deleteRecursively()
        Result.failure(Exception("Can't clone repository. Last error: $lastErrorMessage"))
    }

    /**
     * Imports an existing Git repository from a given directory URI.
     *
     * This function verifies if the selected directory contains a valid `.git` folder,
     * opens the repository to extract the remote "origin" URL, and returns the result.
     *
     * @param treeUri The [Uri] of the directory to be imported as a repository.
     * @return A [CloneResult] containing the success status, the remote URL (as the message),
     * and the absolute path to the local folder.
     */
    suspend fun importExistingRepo(
        treeUri: Uri
    ): Result<CloneResult> = withContext(Dispatchers.IO) {
        val path = resolveUriToPath(treeUri)
            ?: return@withContext Result.failure(Exception("Can't resolve directory."))

        val folder = File(path)
        val gitDir = File(folder, ".git")

        if (!gitDir.exists()) {
            return@withContext Result.failure(
                Exception(
                    "Selected folder isn't a valid Git repository (no .git folder found)."
                )
            )
        }

        try {
            val git = Git.open(folder)
            val config = git.repository.config
            val remoteUrl = config.getString("remote", "origin", "url") ?: "Unknown distant URL."
            git.close()

            Result.success(CloneResult(remoteUrl, path, ""))
        } catch (e: Exception) {
            Result.failure(Exception("Can't open repository: ${e.localizedMessage}"))
        }
    }
    //endregion

    //region Branch Management
    /*suspend fun getBranches(repoPath: String): List<String> = withContext(Dispatchers.IO) {
        try {
            Git.open(File(repoPath)).use { git ->
                git.branchList().setListMode(ListBranchCommand.ListMode.ALL)
                    .call()
                    .map { ref ->
                        org.eclipse.jgit.lib.Repository.shortenRefName(ref.name)
                    }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }*/

    suspend fun getBranchesFullRefs(repoPath: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                Git.open(File(repoPath)).use { git ->
                    Result.success(
                        git.branchList().setListMode(ListBranchCommand.ListMode.ALL)
                            .call()
                            .map { ref ->
                                ref.name
                            }
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun checkoutBranch(repoPath: String, branch: Branch): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                Git.open(File(repoPath)).use { git ->
                    val command = git.checkout().setName(branch.name)

                    if (!branch.isLocal && branch.isRemote && branch.remoteRef != null) {
                        command.setCreateBranch(true)
                            .setStartPoint(branch.remoteRef) // Utilise "refs/remotes/origin/main"
                            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    }

                    command.call()
                    Result.success(branch.name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(
                    Exception(
                        "Error while checkout on ${branch.name}: ${e.localizedMessage}",
                        e
                    )
                )
            }
        }

    suspend fun getTrackingStatus(repoPath: String, branchName: String): Result<Pair<Int, Int>> =
        withContext(Dispatchers.IO) {
            try {
                Git.open(File(repoPath)).use { git ->
                    val status = BranchTrackingStatus.of(git.repository, branchName)
                    if (status != null) {
                        Result.success(Pair(status.aheadCount, status.behindCount))
                    } else {
                        Result.success(Pair(0, 0))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    //endregion

    //region Remote Operations
    suspend fun fetch(repoPath: String, credentials: List<GitCredential>): Result<Unit> =
        withContext(Dispatchers.IO) {
            val attempts = listOf<GitCredential?>(null) + credentials
            var lastException: Exception? = null

            for (cred in attempts) {
                try {
                    Git.open(File(repoPath)).use { git ->
                        val fetchCommand = git.fetch()
                        if (cred != null) {
                            fetchCommand.setCredentialsProvider(
                                UsernamePasswordCredentialsProvider(cred.username, cred.token)
                            )
                        }
                        fetchCommand.call()
                        return@withContext Result.success(Unit)
                    }
                } catch (e: Exception) {
                    lastException = e
                }
            }
            Result.failure(lastException ?: Exception("Fetch failed"))
        }

    suspend fun pull(repoPath: String, credentials: List<GitCredential>): Result<Unit> =
        withContext(Dispatchers.IO) {
            val attempts = listOf<GitCredential?>(null) + credentials
            var lastException: Exception? = null

            for (cred in attempts) {
                try {
                    Git.open(File(repoPath)).use { git ->
                        val pullCommand = git.pull()

                        if (cred != null) {
                            pullCommand.setCredentialsProvider(
                                UsernamePasswordCredentialsProvider(cred.username, cred.token)
                            )
                        }

                        val result = pullCommand.call()

                        if (result.isSuccessful) {
                            return@withContext Result.success(Unit)
                        } else {
                            val mergeStatus = result.mergeResult?.mergeStatus ?: "Unknown"
                            lastException = Exception("Pull failed: $mergeStatus")
                        }
                    }
                } catch (e: Exception) {
                    lastException = e
                }
            }
            Result.failure(lastException ?: Exception("Pull failed"))
        }

    suspend fun push(repoPath: String, credentials: List<GitCredential>): Result<Unit> =
        withContext(Dispatchers.IO) {
            val attempts = listOf<GitCredential?>(null) + credentials
            var lastException: Exception? = null

            for (cred in attempts) {
                try {
                    Git.open(File(repoPath)).use { git ->
                        val pushCommand = git.push()

                        if (cred != null) {
                            pushCommand.setCredentialsProvider(
                                UsernamePasswordCredentialsProvider(cred.username, cred.token)
                            )
                        }

                        val pushResults = pushCommand.call()

                        pushResults.forEach { pushResult ->
                            pushResult.remoteUpdates.forEach { update ->
                                when (update.status) {
                                    RemoteRefUpdate.Status.OK,
                                    RemoteRefUpdate.Status.UP_TO_DATE -> {
                                    }

                                    else -> {
                                        throw Exception("Push failed for ${update.remoteName}: ${update.status}")
                                    }
                                }
                            }
                        }
                        return@withContext Result.success(Unit)
                    }

                } catch (e: Exception) {
                    lastException = e
                }
            }
            Result.failure(lastException ?: Exception("Push failed"))
        }
    //endregion

    //region Commit Management
    /**
     * Retrieves the list of commits from the current branch of a local Git repository.
     *
     * This function opens the repository located at [repoPath], iterates through the commit
     * history, and extracts relevant information such as the short hash, author details,
     * message, and formatted date.
     *
     * @param repoPath The absolute file system path to the local repository directory.
     * @return A list of [CommitInfo] objects representing the history of the current branch.
     * Returns an empty list if an error occurs or if no commits are found.
     */
    suspend fun getCommits(repoPath: String, branchName: String? = null): List<CommitInfo> =
        withContext(Dispatchers.IO) {
            val commitList = mutableListOf<CommitInfo>()
            val dateFormatter = SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
            )

            try {
                Git.open(File(repoPath)).use { git ->
                    val repo = git.repository
                    val walk = RevWalk(repo)

                    val targetBranch = if (branchName.isNullOrBlank()) repo.branch else branchName

                    val logCommand = git.log()
                    var hasStarted = false

                    // Add local branch
                    val localRef = repo.resolve("refs/heads/$targetBranch")
                    if (localRef != null) {
                        logCommand.add(localRef)
                        hasStarted = true
                    }

                    // Add all remote tracking branches with that name
                    repo.refDatabase.getRefsByPrefix("refs/remotes/").forEach { ref ->
                        if (ref.name.endsWith("/$targetBranch")) {
                            logCommand.add(ref.objectId)
                            hasStarted = true
                        }
                    }

                    if (!hasStarted) {
                        repo.resolve("HEAD")?.let { logCommand.add(it) }
                    }

                    val localRefs = repo.refDatabase.getRefsByPrefix("refs/heads/")
                        .map { walk.parseCommit(it.objectId) }
                    val remoteRefs = repo.refDatabase.getRefsByPrefix("refs/remotes/")
                        .map { walk.parseCommit(it.objectId) }

                    val logs = logCommand.call()

                    for (rev in logs) {
                        val author = rev.authorIdent
                        val date = Date(rev.commitTime.toLong() * 1000)
                        val currentCommit = walk.parseCommit(rev.id)

                        val isLocal = localRefs.any { walk.isMergedInto(currentCommit, it) }
                        val isRemote = remoteRefs.any { walk.isMergedInto(currentCommit, it) }

                        val status = when {
                            isLocal && isRemote -> CommitStatus.SYNCED
                            isLocal -> CommitStatus.LOCAL_ONLY
                            isRemote -> CommitStatus.REMOTE_ONLY
                            else -> CommitStatus.UNKNOWN
                        }

                        commitList.add(
                            CommitInfo(
                                commitHash = rev.name,
                                authorName = author.name ?: "Unknown",
                                authorEmail = author.emailAddress ?: "",
                                commitMessage = rev.shortMessage,
                                commitDate = dateFormatter.format(date),
                                status = status
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext commitList
        }
    //endregion

    //region Status & Working Directory
    suspend fun getRepoStatus(repoPath: String): Map<String, Set<String>> =
        withContext(Dispatchers.IO) {
            Git.open(File(repoPath)).use { git ->
                val status = git.status().call()
                mapOf(
                    "unstaged" to (status.modified + status.untracked + status.missing),
                    "staged" to (status.added + status.changed + status.removed)
                )
            }
        }

    suspend fun resetRepository(repoPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Git.open(File(repoPath)).use { git ->
                git.reset().setMode(ResetCommand.ResetType.HARD).call()
                git.clean().setCleanDirectories(true).setIgnore(false).call()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun discardFiles(repoPath: String, filePatterns: List<String>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Git.open(File(repoPath)).use { git ->
                    val status = git.status().call()
                    val trackedToCheckout = mutableListOf<String>()
                    val untrackedToDelete = mutableListOf<String>()

                    filePatterns.forEach { path ->
                        if (status.untracked.contains(path)) {
                            untrackedToDelete.add(path)
                        } else {
                            trackedToCheckout.add(path)
                        }
                    }

                    if (trackedToCheckout.isNotEmpty()) {
                        val checkout = git.checkout()
                        trackedToCheckout.forEach { checkout.addPath(it) }
                        checkout.call()
                    }

                    untrackedToDelete.forEach { path ->
                        File(repoPath, path).delete()
                    }

                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    //endregion

    //region Index & Staging Management
    suspend fun stageFiles(repoPath: String, filePatterns: List<String>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Git.open(File(repoPath)).use { git ->
                    val status = git.status().call()
                    val addCommand = git.add()
                    val rmCommand = git.rm()
                    var hasAdd = false
                    var hasRm = false

                    filePatterns.forEach { pattern ->
                        if (status.missing.contains(pattern)) {
                            rmCommand.addFilepattern(pattern)
                            hasRm = true
                        } else {
                            addCommand.addFilepattern(pattern)
                            hasAdd = true
                        }
                    }

                    if (hasAdd) addCommand.call()
                    if (hasRm) rmCommand.call()
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun stageAll(repoPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Git.open(File(repoPath)).use { git ->
                git.add().addFilepattern(".").call()

                val status = git.status().call()
                if (status.missing.isNotEmpty()) {
                    val rm = git.rm()
                    status.missing.forEach { rm.addFilepattern(it) }
                    rm.call()
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unstageFiles(repoPath: String, filePatterns: List<String>? = null): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Git.open(File(repoPath)).use { git ->
                    val reset = git.reset()

                    filePatterns?.forEach { reset.addPath(it) }

                    reset.call()
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    //endregion
    suspend fun commit(repoPath: String, gitConfig: GitConfig, message: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Git.open(File(repoPath)).use { git ->
                    git.commit()
                        .setMessage(message)
                        .setAuthor(gitConfig.name, gitConfig.email)
                        .call()
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun editRepository(repo: Repository): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Git.open(File(repo.localPath)).use { git ->
                val config = git.repository.config
                config.setString("remote", "origin", "url", repo.remoteUrl)
                config.save()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}