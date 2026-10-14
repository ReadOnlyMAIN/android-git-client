package fr.readonlymain.gitclient.data.repository

import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.branchesFromRefs
import kotlinx.coroutines.test.runTest
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.URIish
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class BranchIdentityTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun `same named branches from different remotes retain distinct refs`() {
        val branches = branchesFromRefs(listOf("refs/heads/main", "refs/remotes/origin/main", "refs/remotes/upstream/main"),
            mapOf("main" to "refs/remotes/origin/main"))
        assertThat(branches.map { it.name }).containsExactly("main", "upstream/main")
        assertThat(branches.single { it.name == "main" }.remoteRef).isEqualTo("refs/remotes/origin/main")
        assertThat(branches.single { it.name == "upstream/main" }.isLocal).isFalse()
        assertThat(branches.single { it.name == "upstream/main" }.remoteRef).isEqualTo("refs/remotes/upstream/main")
    }

    @Test fun `remote symbolic HEAD is not exposed as a branch`() {
        val branches = branchesFromRefs(listOf("refs/remotes/origin/HEAD", "refs/remotes/origin/feature/nested"))
        assertThat(branches.single().name).isEqualTo("feature/nested")
        assertThat(branches.single().remoteRef).isEqualTo("refs/remotes/origin/feature/nested")
    }

    @Test fun `remote deletion uses full remote identity rather than display name`() = runTest {
        val fixture = GitTestFixture(temporary.root)
        val (origin) = fixture.remoteWithSeed()
        val upstream = File(temporary.root, "upstream.git")
        Git.cloneRepository().setBare(true).setURI(origin.toURI().toString()).setDirectory(upstream).call().close()
        Git.open(origin).use { it.branchCreate().setName("topic").setStartPoint("trunk").call() }
        Git.open(upstream).use { it.branchCreate().setName("topic").setStartPoint("trunk").call() }
        val local = fixture.clone(origin, "local")
        Git.open(local).use { git ->
            git.remoteAdd().setName("upstream").setUri(URIish(upstream.toURI().toString())).call()
            git.fetch().setRemote("upstream").call()
        }
        val repository = DefaultGitRepository()
        val branch = repository.getBranches(local.path).getOrThrow().single { it.remoteRef == "refs/remotes/upstream/topic" }
        assertThat(branch.name).isEqualTo("upstream/topic")
        repository.deleteBranch(local.path, branch, emptyList(), deleteRemote = true).getOrThrow()
        assertThat(Git.open(upstream).use { it.repository.resolve("refs/heads/topic") }).isNull()
        assertThat(Git.open(origin).use { it.repository.resolve("refs/heads/topic") }).isNotNull()
        assertThat(repository.getCurrentBranch(local.path).getOrThrow()).isEqualTo("trunk")
    }
}
