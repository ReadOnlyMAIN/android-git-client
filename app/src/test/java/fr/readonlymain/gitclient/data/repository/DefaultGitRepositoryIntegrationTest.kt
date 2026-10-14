package fr.readonlymain.gitclient.data.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.eclipse.jgit.api.Git
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DefaultGitRepositoryIntegrationTest {
    @get:Rule val temporary = TemporaryFolder()
    private val repository = DefaultGitRepository()

    @Test fun `cancelled clone callback removes its owned partial directory`() = runTest {
        val fixture = GitTestFixture(temporary.root)
        val (remote) = fixture.remoteWithSeed()
        val destination = temporary.newFolder("cancelled-clone")
        val failure = runCatching {
            repository.cloneRepo(remote.toURI().toString(), emptyList(), destination.path) { _, _ ->
                throw kotlinx.coroutines.CancellationException("cancel")
            }
        }.exceptionOrNull()
        assertThat(failure).isInstanceOf(kotlinx.coroutines.CancellationException::class.java)
        assertThat(destination.listFiles()).isEmpty()
    }

    @Test fun `history distinguishes an empty repository from an unavailable repository`() = runTest {
        val empty = temporary.newFolder("empty")
        Git.init().setDirectory(empty).call().close()
        assertThat(repository.getCommits(empty.path)).isEmpty()
        assertThat(runCatching { repository.getCommits(temporary.root.resolve("missing").path) }.isFailure).isTrue()
    }

    @Test fun `clone creates a subfolder and preserves existing destinations`() = runTest {
        val fixture = GitTestFixture(temporary.root)
        val (remote) = fixture.remoteWithSeed()
        val destination = temporary.newFolder("clones")
        val result = repository.cloneRepo(remote.toURI().toString(), emptyList(), destination.path) { _, _ -> }.getOrThrow()
        assertThat(File(result.folderPath, ".git").isDirectory).isTrue()
        assertThat(repository.getCurrentBranch(result.folderPath).getOrThrow()).isEqualTo("trunk")
        assertThat(repository.cloneRepo(remote.toURI().toString(), emptyList(), destination.path) { _, _ -> }.isFailure).isTrue()
        assertThat(File(result.folderPath, "file.txt").readLines()).containsExactly("initial")
    }

    @Test fun `import reports canonical local path`() = runTest {
        val directory = GitTestFixture(temporary.root).repository()
        val result = repository.importExistingRepo(File(directory, ".").path).getOrThrow()
        assertThat(result.folderPath).isEqualTo(directory.canonicalPath)
        assertThat(repository.importExistingRepo(temporary.newFolder("not-git").path).isFailure).isTrue()
    }

    @Test fun `HEAD is read from disk including detached HEAD`() = runTest {
        val directory = GitTestFixture(temporary.root).repository()
        assertThat(repository.getCurrentBranch(directory.path).getOrThrow()).isEqualTo("trunk")
        val hash = Git.open(directory).use { git ->
            val head = git.repository.resolve("HEAD").name
            git.checkout().setName(head).call()
            head
        }
        assertThat(repository.getCurrentBranch(directory.path).getOrThrow()).isEqualTo(hash)
    }

    @Test fun `fetch pull and push work against local bare remote`() = runTest {
        val fixture = GitTestFixture(temporary.root)
        val (remote) = fixture.remoteWithSeed()
        val first = fixture.clone(remote, "first")
        val second = fixture.clone(remote, "second")
        Git.open(first).use { git ->
            File(first, "new.txt").writeText("new")
            fixture.commit(git, "new file")
        }
        repository.push(first.path, emptyList()).getOrThrow()
        repository.fetch(second.path, emptyList()).getOrThrow()
        assertThat(repository.getTrackingStatus(second.path, "trunk").getOrThrow().second).isEqualTo(1)
        repository.pull(second.path, emptyList()).getOrThrow()
        assertThat(File(second, "new.txt").readText()).isEqualTo("new")
        assertThat(repository.getTrackingStatus(second.path, "trunk").getOrThrow()).isEqualTo(0 to 0)
    }

    @Test fun `history identifies local and remote commits`() = runTest {
        val fixture = GitTestFixture(temporary.root)
        val (remote) = fixture.remoteWithSeed()
        val clone = fixture.clone(remote, "clone")
        Git.open(clone).use { git ->
            File(clone, "local.txt").writeText("local")
            fixture.commit(git, "local")
        }
        val history = repository.getCommits(clone.path, "trunk")
        assertThat(history).hasSize(2)
        assertThat(history.first().status.name).isEqualTo("LOCAL_ONLY")
        assertThat(history.last().status.name).isEqualTo("SYNCED")
    }
}
