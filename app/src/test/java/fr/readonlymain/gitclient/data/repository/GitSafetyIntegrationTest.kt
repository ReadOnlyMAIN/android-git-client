package fr.readonlymain.gitclient.data.repository

import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.GitConfig
import kotlinx.coroutines.test.runTest
import org.eclipse.jgit.api.Git
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GitSafetyIntegrationTest {
    @get:Rule val temporary = TemporaryFolder()
    private val repository = DefaultGitRepository()

    @Test fun `stage and unstage modified added and deleted files`() = runTest {
        val directory = GitTestFixture(temporary.root).repository()
        File(directory, "file.txt").delete()
        File(directory, "new.txt").writeText("new")
        repository.stageFiles(directory.path, listOf("file.txt", "new.txt")).getOrThrow()
        assertThat(repository.getRepoStatus(directory.path).staged).containsExactly("file.txt", "new.txt")
        repository.unstageFiles(directory.path, listOf("new.txt")).getOrThrow()
        assertThat(repository.getRepoStatus(directory.path).staged).containsExactly("file.txt")
        repository.unstageFiles(directory.path, null).getOrThrow()
        assertThat(repository.getRepoStatus(directory.path).staged).isEmpty()
        repository.stageAll(directory.path).getOrThrow()
        assertThat(repository.getRepoStatus(directory.path).staged).containsExactly("file.txt", "new.txt")
    }

    @Test fun `commit writes staged content with configured identity`() = runTest {
        val directory = GitTestFixture(temporary.root).repository()
        File(directory, "file.txt").writeText("changed")
        repository.stageAll(directory.path).getOrThrow()
        repository.commit(directory.path, GitConfig(name = "Author", email = "author@example.invalid"), "subject\n\nbody").getOrThrow()
        Git.open(directory).use { git ->
            val commit = git.log().call().first()
            assertThat(commit.fullMessage).isEqualTo("subject\n\nbody")
            assertThat(commit.authorIdent.name).isEqualTo("Author")
        }
        assertThat(repository.getRepoStatus(directory.path).staged).isEmpty()
    }

    @Test fun `branch creation checkout and deletion preserve source`() = runTest {
        val directory = GitTestFixture(temporary.root).repository()
        val source = Branch("trunk", true, false, "refs/heads/trunk")
        repository.createBranch(directory.path, "feature", source, false).getOrThrow()
        repository.checkoutBranch(directory.path, Branch("feature", true, false)).getOrThrow()
        assertThat(repository.getCurrentBranch(directory.path).getOrThrow()).isEqualTo("feature")
        assertThat(repository.deleteBranch(directory.path, Branch("feature", true, false), emptyList()).isFailure).isTrue()
        repository.checkoutBranch(directory.path, source).getOrThrow()
        repository.deleteBranch(directory.path, Branch("feature", true, false), emptyList()).getOrThrow()
        assertThat(repository.getBranchesFullRefs(directory.path).getOrThrow()).containsExactly("refs/heads/trunk")
    }

    @Test fun `discard validates all paths before changing any file`() = runTest {
        val directory = GitTestFixture(temporary.root).repository()
        File(directory, "file.txt").writeText("keep my changes")
        assertThat(repository.discardFiles(directory.path, listOf("file.txt", "../outside.txt")).isFailure).isTrue()
        assertThat(File(directory, "file.txt").readText()).isEqualTo("keep my changes")
        assertThat(repository.discardFiles(directory.path, listOf(".git/config")).isFailure).isTrue()
        assertThat(repository.discardFiles(directory.path, listOf(File(directory, "file.txt").absolutePath)).isFailure).isTrue()
    }

    @Test fun `discard restores tracked files and deletes only requested untracked files`() = runTest {
        val directory = GitTestFixture(temporary.root).repository()
        File(directory, "file.txt").writeText("changed")
        File(directory, "remove.txt").writeText("remove")
        File(directory, "keep.txt").writeText("keep")
        repository.discardFiles(directory.path, listOf("file.txt", "remove.txt")).getOrThrow()
        assertThat(File(directory, "file.txt").readLines()).containsExactly("initial")
        assertThat(File(directory, "remove.txt").exists()).isFalse()
        assertThat(File(directory, "keep.txt").readText()).isEqualTo("keep")
    }

    @Test fun `hard reset preserves ignored files`() = runTest {
        val fixture = GitTestFixture(temporary.root)
        val directory = fixture.repository()
        Git.open(directory).use { git ->
            File(directory, ".gitignore").writeText("ignored/\n")
            fixture.commit(git, "ignore local data")
        }
        File(directory, "ignored").mkdir()
        File(directory, "ignored/precious.txt").writeText("precious")
        File(directory, "untracked.txt").writeText("untracked")
        repository.resetRepository(directory.path).getOrThrow()
        assertThat(File(directory, "ignored/precious.txt").readText()).isEqualTo("precious")
        assertThat(File(directory, "untracked.txt").exists()).isFalse()
    }

    @Test fun `diverged push is rejected without changing remote HEAD`() = runTest {
        val fixture = GitTestFixture(temporary.root)
        val (remote) = fixture.remoteWithSeed()
        val first = fixture.clone(remote, "first")
        val second = fixture.clone(remote, "second")
        Git.open(first).use { git -> File(first, "first.txt").writeText("first"); fixture.commit(git, "first") }
        repository.push(first.path, emptyList()).getOrThrow()
        val before = Git.open(remote).use { it.repository.resolve("refs/heads/trunk") }
        Git.open(second).use { git -> File(second, "second.txt").writeText("second"); fixture.commit(git, "second") }
        val failure = repository.push(second.path, emptyList()).exceptionOrNull() as GitOperationException
        assertThat(failure.kind).isEqualTo(GitFailureKind.REJECTED)
        assertThat(Git.open(remote).use { it.repository.resolve("refs/heads/trunk") }).isEqualTo(before)
    }

    @Test fun `pull conflicts are classified and retained in status`() = runTest {
        val fixture = GitTestFixture(temporary.root)
        val (remote) = fixture.remoteWithSeed()
        val first = fixture.clone(remote, "first")
        val second = fixture.clone(remote, "second")
        Git.open(first).use { git -> File(first, "file.txt").writeText("first\n"); fixture.commit(git, "first") }
        repository.push(first.path, emptyList()).getOrThrow()
        Git.open(second).use { git -> File(second, "file.txt").writeText("second\n"); fixture.commit(git, "second") }
        val failure = repository.pull(second.path, emptyList()).exceptionOrNull() as GitOperationException
        assertThat(failure.kind).isEqualTo(GitFailureKind.CONFLICT)
        assertThat(repository.getRepoStatus(second.path).conflicts).containsExactly("file.txt")
    }

    @Test fun `remote deletion failure reports that local branch was already deleted`() = runTest {
        val directory = GitTestFixture(temporary.root).repository()
        Git.open(directory).use { git ->
            git.branchCreate().setName("feature").call()
            git.repository.config.apply {
                setString("remote", "origin", "url", File(temporary.root, "missing.git").toURI().toString())
                save()
            }
        }
        val failure = repository.deleteBranch(directory.path,
            Branch("feature", true, true, "refs/heads/feature", "refs/remotes/origin/feature"), emptyList(), true).exceptionOrNull()
        assertThat(failure).isInstanceOf(PartialBranchDeletionException::class.java)
        assertThat((failure as PartialBranchDeletionException).localBranchDeleted).isTrue()
        assertThat(repository.getBranchesFullRefs(directory.path).getOrThrow()).doesNotContain("refs/heads/feature")
    }
}
