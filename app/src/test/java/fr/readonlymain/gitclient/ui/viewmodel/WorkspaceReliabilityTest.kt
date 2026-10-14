package fr.readonlymain.gitclient.ui.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.*
import fr.readonlymain.gitclient.data.preferences.*
import fr.readonlymain.gitclient.data.repository.FakeGitRepository
import fr.readonlymain.gitclient.data.repository.GitRepository
import fr.readonlymain.gitclient.util.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WorkspaceReliabilityTest {
    @get:Rule val main = MainDispatcherRule()
    private val preferences = FakeRepositoriesPreferences()
    private val fake = FakeGitRepository()
    private fun viewModel(repository: GitRepository = fake) = WorkspaceViewModel(
        repository, preferences, FakeCredentialsPreferences(), FakeGitConfigPreferences()
    )

    @Test fun `repositories with the same name can be switched and clear selections`() = runTest {
        val first = Repository(name = "same", localPath = "/first/repo")
        val second = Repository(name = "same", localPath = "/second/repo")
        preferences.addRepository(first); preferences.addRepository(second)
        fake.currentBranch = "develop"
        val vm = viewModel()
        vm.onRepositorySelected(first.localPath)
        vm.toggleUnstagedFileSelection("old.txt")
        vm.toggleStagedFileSelection("old-staged.txt")
        vm.onRepositorySelected(second.localPath)
        assertThat(vm.uiState.value.repositoryPath).isEqualTo(canonicalRepositoryPath(second.localPath))
        assertThat(vm.branchName.value).isEqualTo("develop")
        assertThat(vm.selectedUnstagedFiles.value).isEmpty()
        assertThat(vm.selectedStagedFiles.value).isEmpty()
    }

    @Test fun `a late old repository snapshot cannot overwrite a newer selection`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = object : GitRepository by fake {
            override suspend fun getCommits(repoPath: String, branchName: String?): List<CommitInfo> {
                if (canonicalRepositoryPath(repoPath) == canonicalRepositoryPath("/old")) {
                    withContext(NonCancellable) { gate.await() }
                    return listOf(CommitInfo("old", "old", "", "old", "", CommitStatus.UNKNOWN))
                }
                return emptyList()
            }
        }
        val vm = viewModel(repository)
        vm.onRepositorySelected("/old")
        vm.onRepositorySelected("/new")
        gate.complete(Unit)
        assertThat(vm.uiState.value.repositoryPath).isEqualTo(canonicalRepositoryPath("/new"))
        assertThat(vm.commitsByRepo.value).isEmpty()
        assertThat(vm.isLoadingCommits.value).isFalse()
    }

    @Test fun `branch changes clear file selections and staging refreshes status`() = runTest {
        fake.statusToReturn = mapOf("unstaged" to setOf("file.txt"))
        val vm = viewModel()
        vm.onRepositorySelected("/repo")
        vm.toggleUnstagedFileSelection("file.txt")
        vm.onBranchSelected(Branch("feature", true, false))
        assertThat(vm.selectedUnstagedFiles.value).isEmpty()
        vm.toggleUnstagedFileSelection("file.txt")
        fake.statusToReturn = mapOf("staged" to setOf("file.txt"))
        vm.stageSelection()
        assertThat(vm.stagedFiles.value).containsExactly("file.txt")
        assertThat(vm.unstagedFiles.value).isEmpty()
    }

    @Test fun `duplicate imports report an error and preserve the original entry`() = runTest {
        val vm = RepositoriesViewModel(fake, preferences)
        vm.uiEvent.test {
            vm.startImport("/repo")
            assertThat(awaitItem()).isInstanceOf(UiEvent.Success::class.java)
            vm.startImport("/repo/.")
            val error = awaitItem() as UiEvent.Error
            assertThat(error.message).contains("already imported")
            assertThat(preferences.repositoriesFlow.first()).hasSize(1)
        }
    }

    @Test fun `canonical aliases are rejected by the repository uniqueness rule`() {
        val existing = Repository(localPath = "/repo")
        assertThat(runCatching {
            requireUniqueRepository(listOf(existing), Repository(localPath = "/repo/child/.."))
        }.isFailure).isTrue()
    }
}
