package fr.readonlymain.gitclient.data.repository

import fr.readonlymain.gitclient.data.model.canonicalRepositoryPath
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryOperationCoordinator @Inject constructor() {
    private val locks = ConcurrentHashMap<String, Mutex>()

    suspend fun <T> withRepository(path: String, operation: suspend () -> T): T =
        locks.getOrPut(canonicalRepositoryPath(path)) { Mutex() }.withLock { operation() }
}
