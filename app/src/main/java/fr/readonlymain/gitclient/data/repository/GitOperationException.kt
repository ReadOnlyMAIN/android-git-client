package fr.readonlymain.gitclient.data.repository

import org.eclipse.jgit.api.errors.CheckoutConflictException
import org.eclipse.jgit.api.errors.TransportException

enum class GitFailureKind { AUTHENTICATION, TRANSPORT, REJECTED, CONFLICT, LOCAL }

class GitOperationException(val kind: GitFailureKind, message: String, cause: Throwable? = null) :
    Exception(message, cause)

/** Only explicit authentication failures are retryable; unknown failures fail closed. */
fun classifyGitFailure(error: Exception): GitOperationException {
    if (error is GitOperationException) return error
    val causes = generateSequence<Throwable>(error) { it.cause }.take(10).toList()
    val message = causes.joinToString(" ") { it.message.orEmpty() }.lowercase()
    val kind = when {
        error is CheckoutConflictException -> GitFailureKind.CONFLICT
        causes.any { it is TransportException || it is org.eclipse.jgit.errors.TransportException } -> {
            if (listOf("not authorized", "authentication is required", "authentication not supported",
                    "auth fail", "authentication failed", "401 unauthorized").any { it in message }) {
                GitFailureKind.AUTHENTICATION
            } else GitFailureKind.TRANSPORT
        }
        else -> GitFailureKind.LOCAL
    }
    return GitOperationException(kind, error.localizedMessage ?: "Git operation failed", error)
}
