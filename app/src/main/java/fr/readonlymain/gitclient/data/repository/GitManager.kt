package fr.readonlymain.gitclient.data.repository

import android.net.Uri
import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.CloneResult
import fr.readonlymain.gitclient.data.model.CommitInfo
import fr.readonlymain.gitclient.data.model.GitCredential
import fr.readonlymain.gitclient.utils.resolveUriToPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Repository
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
    ): CloneResult = withContext(Dispatchers.IO) {
        val baseFolderPath = resolveUriToPath(treeUri)
            ?: return@withContext CloneResult("Error : Can't resolve directory.", false)

        val folderName = url.substringAfterLast("/").replace(".git", "")
        val localFolder = File(baseFolderPath, folderName)

        if (localFolder.exists()) {
            return@withContext CloneResult("Error : Folder already exists.", false)
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

                return@withContext CloneResult(
                    message = "Success: ${localFolder.name} successfully cloned.",
                    success = true,
                    folderPath = localFolder.absolutePath,
                    username = cred?.username
                )

            } catch (e: Exception) {
                lastErrorMessage = e.localizedMessage ?: "Unknown error."
                continue
            }
        }

        if (localFolder.exists()) localFolder.deleteRecursively()
        CloneResult("Can't clone repository. Last error: $lastErrorMessage", false)
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
    ): CloneResult = withContext(Dispatchers.IO) {
        val path = resolveUriToPath(treeUri)
            ?: return@withContext CloneResult("Can't resolve directory.", false)

        val folder = File(path)
        val gitDir = File(folder, ".git")

        if (!gitDir.exists()) {
            return@withContext CloneResult(
                "Selected folder isn't a valid Git repository (no .git folder found).",
                false
            )
        }

        try {
            val git = Git.open(folder)
            val config = git.repository.config
            val remoteUrl = config.getString("remote", "origin", "url") ?: "Unknown distant URL."
            git.close()

            CloneResult(
                message = remoteUrl,
                success = true,
                folderPath = path,
                username = null
            )
        } catch (e: Exception) {
            CloneResult("Can't open repository: ${e.localizedMessage}", false)
        }
    }

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
    suspend fun getCommits(repoPath: String): List<CommitInfo> = withContext(Dispatchers.IO) {
        val commitList = mutableListOf<CommitInfo>()
        val dateFormatter = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

        try {
            Git.open(File(repoPath)).use { git ->
                val logs = git.log().call()

                for (rev in logs) {
                    val author = rev.authorIdent
                    val date = Date(rev.commitTime.toLong() * 1000)

                    commitList.add(
                        CommitInfo(
                            commitHash = rev.name,
                            authorName = author.name ?: "Unknown",
                            authorEmail = author.emailAddress ?: "",
                            commitMessage = rev.shortMessage,
                            commitDate = dateFormatter.format(date)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext commitList
    }

    suspend fun getBranches(repoPath: String): List<String> = withContext(Dispatchers.IO) {
        try {
            Git.open(File(repoPath)).use { git ->
                git.branchList().setListMode(ListBranchCommand.ListMode.ALL)
                    .call()
                    .map { ref ->
                        Repository.shortenRefName(ref.name)
                    }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getBranchesFullRefs(repoPath: String): List<String> = withContext(Dispatchers.IO) {
        try {
            Git.open(File(repoPath)).use { git ->
                git.branchList().setListMode(ListBranchCommand.ListMode.ALL)
                    .call()
                    .map { ref ->
                        ref.name
                    }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun checkoutBranch(repoPath: String, branch: Branch): String =
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
                    return@withContext branch.name
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext branch.name
            }
        }
}