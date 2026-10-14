package fr.readonlymain.gitclient.data.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RepositoryOperationCoordinatorTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun `aliases serialize while independent repositories can progress`() = runTest {
        val coordinator = RepositoryOperationCoordinator()
        val gate = CompletableDeferred<Unit>()
        val events = mutableListOf<String>()
        val path = temporary.newFolder("repo")
        val first = launch { coordinator.withRepository(path.path) { events += "first"; gate.await(); events += "finished" } }
        val second = launch { coordinator.withRepository(path.resolve(".").path) { events += "second" } }
        val independent = launch { coordinator.withRepository(temporary.root.resolve("other").path) { events += "independent" } }
        runCurrent()
        assertThat(events).containsExactly("first", "independent").inOrder()
        gate.complete(Unit)
        first.join(); second.join(); independent.join()
        assertThat(events).containsExactly("first", "independent", "finished", "second").inOrder()
    }

    @Test fun `cancelled waiter never executes and does not strand the lock`() = runTest {
        val coordinator = RepositoryOperationCoordinator()
        val gate = CompletableDeferred<Unit>()
        val path = temporary.root.path
        val first = launch { coordinator.withRepository(path) { gate.await() } }
        var executed = false
        val waiting = launch { coordinator.withRepository(path) { executed = true } }
        runCurrent()
        waiting.cancel()
        gate.complete(Unit)
        first.join(); waiting.join()
        assertThat(executed).isFalse()
        coordinator.withRepository(path) { executed = true }
        assertThat(executed).isTrue()
    }
}
