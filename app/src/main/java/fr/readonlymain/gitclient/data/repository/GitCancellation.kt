package fr.readonlymain.gitclient.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** JGit may wrap cancellation in a TransportException rather than propagate it. */
internal suspend fun Exception.preserveCancellation() {
    if (this is CancellationException) throw this
    currentCoroutineContext().ensureActive()
}
