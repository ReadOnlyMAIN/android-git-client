package fr.readonlymain.gitclient.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.readonlymain.gitclient.data.model.CloneResult
import fr.readonlymain.gitclient.data.model.GitCredential
import fr.readonlymain.gitclient.utils.resolveUriToPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
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
class GitManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
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
}