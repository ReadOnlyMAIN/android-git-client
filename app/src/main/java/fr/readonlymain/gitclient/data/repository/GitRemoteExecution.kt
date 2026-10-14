package fr.readonlymain.gitclient.data.repository

import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.transport.CredentialsProvider
import kotlin.coroutines.CoroutineContext

internal class GitProgressMonitor(private val context: CoroutineContext) : ProgressMonitor {
    override fun start(totalTasks: Int) {}
    override fun beginTask(title: String, totalWork: Int) {}
    override fun update(completed: Int) {}
    override fun endTask() {}
    override fun isCancelled() = !context.isActive
    override fun showDuration(enabled: Boolean) {}
}

internal suspend fun <T> withRemoteCredentials(
    url: String,
    credentials: List<GitCredential>,
    operation: (CredentialsProvider?, ProgressMonitor) -> T
): T {
    val context = currentCoroutineContext()
    var failure: Exception? = null
    for (credential in credentialAttempts(url, credentials)) {
        context.ensureActive()
        try {
            return operation(credential?.let(::scopedCredentials), GitProgressMonitor(context))
        } catch (e: Exception) {
            e.preserveCancellation()
            val classified = classifyGitFailure(e)
            if (classified.kind != GitFailureKind.AUTHENTICATION) throw classified
            failure = classified
        }
    }
    throw failure ?: GitOperationException(GitFailureKind.AUTHENTICATION, "Authentication failed")
}
