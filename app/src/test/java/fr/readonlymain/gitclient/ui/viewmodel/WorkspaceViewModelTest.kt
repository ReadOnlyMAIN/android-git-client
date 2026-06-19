package fr.readonlymain.gitclient.ui.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.CommitInfo
import fr.readonlymain.gitclient.data.model.CommitStatus
import fr.readonlymain.gitclient.data.model.Repository
import fr.readonlymain.gitclient.data.model.UiEvent
import fr.readonlymain.gitclient.data.preferences.FakeCredentialsPreferences
import fr.readonlymain.gitclient.data.preferences.FakeGitConfigPreferences
import fr.readonlymain.gitclient.data.preferences.FakeRepositoriesPreferences
import fr.readonlymain.gitclient.data.repository.FakeGitRepository
import fr.readonlymain.gitclient.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WorkspaceViewModel
    private lateinit var fakeRepository: FakeGitRepository
    private lateinit var fakeRepoPrefs: FakeRepositoriesPreferences
    private lateinit var fakeCredPrefs: FakeCredentialsPreferences
    private lateinit var fakeConfigPrefs: FakeGitConfigPreferences

    private val testRepo = Repository(
        name = "TestRepo",
        remoteUrl = "https://github.com/test/repo.git",
        localPath = "/fake/path",
        username = "testuser"
    )

    @Before
    fun setup() {
        fakeRepository = FakeGitRepository()
        fakeRepoPrefs = FakeRepositoriesPreferences()
        fakeCredPrefs = FakeCredentialsPreferences()
        fakeConfigPrefs = FakeGitConfigPreferences()

        viewModel = WorkspaceViewModel(
            gitRepository = fakeRepository,
            repositoriesPreferences = fakeRepoPrefs,
            credentialsPreferences = fakeCredPrefs,
            gitConfigPreferences = fakeConfigPrefs
        )
    }

    @Test
    fun `initial state should load repositories from preferences`() = runTest {
        // Given
        fakeRepoPrefs.addRepository(testRepo)

        // Then
        viewModel.repositories.test {
            assertThat(awaitItem()).containsExactly(testRepo)
        }
    }

    @Test
    fun `onRepositorySelected should update current repo and refresh data`() = runTest {
        // Given
        fakeRepoPrefs.addRepository(testRepo)
        val commits =
            listOf(CommitInfo("hash", "Author", "email", "Message", "Date", CommitStatus.SYNCED))
        fakeRepository.commitsToReturn = commits

        // When
        viewModel.onRepositorySelected(testRepo.localPath)

        // Then
        assertThat(viewModel.repoName.value).isEqualTo("TestRepo")
        assertThat(fakeRepository.lastActionCalled).isEqualTo("getTrackingStatus") // refreshCommitList call
        assertThat(viewModel.commitsByRepo.value).isEqualTo(commits)
    }

    @Test
    fun `onPull success should emit success event`() = runTest {
        // Given
        fakeRepoPrefs.saveSelectedRepo(testRepo.localPath)
        fakeRepository.shouldFail = false

        viewModel.uiEvent.test {
            // When
            viewModel.onPull()

            // Then
            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.Success::class.java)
            assertThat((event as UiEvent.Success).message).contains("Successfully pulled")
        }
    }

    @Test
    fun `onPull failure should emit error event`() = runTest {
        // Given
        fakeRepoPrefs.saveSelectedRepo(testRepo.localPath)
        fakeRepository.shouldFail = true
        fakeRepository.lastError = "Network error"

        viewModel.uiEvent.test {
            // When
            viewModel.onPull()

            // Then
            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.Error::class.java)
            assertThat((event as UiEvent.Error).message).contains("Network error")
        }
    }

    @Test
    fun `toggleUnstagedFileSelection should update selection set`() {
        val file = "file.txt"

        // When
        viewModel.toggleUnstagedFileSelection(file)
        // Then
        assertThat(viewModel.selectedUnstagedFiles.value).contains(file)

        // When toggle again
        viewModel.toggleUnstagedFileSelection(file)
        // Then
        assertThat(viewModel.selectedUnstagedFiles.value).isEmpty()
    }

    @Test
    fun `stageSelection should call repository stageFiles and clear selection`() = runTest {
        // Given
        fakeRepoPrefs.saveSelectedRepo(testRepo.localPath)
        val file = "modified.kt"
        viewModel.toggleUnstagedFileSelection(file)

        // When
        viewModel.stageSelection()

        // Then
        assertThat(fakeRepository.lastActionCalled).isEqualTo("stageFiles")
        assertThat(fakeRepository.lastFilePatterns).contains(file)
        assertThat(viewModel.selectedUnstagedFiles.value).isEmpty()
    }

    @Test
    fun `onCommit with empty message should do nothing`() = runTest {
        // Given
        fakeRepoPrefs.saveSelectedRepo(testRepo.localPath)

        // When
        viewModel.onCommit("", onCommitSuccess = {})

        // Then
        assertThat(fakeRepository.lastActionCalled).isNull()
    }

    @Test
    fun `onCommit success should refresh and notify`() = runTest {
        // Given
        fakeRepoPrefs.saveSelectedRepo(testRepo.localPath)
        // Simulate staged files
        fakeRepository.statusToReturn =
            mapOf("staged" to setOf("file.kt"), "unstaged" to emptySet())

        // Force refresh to update internal stagedFiles state in VM
        viewModel.onRepositorySelected(testRepo.localPath)

        var successCallbackCalled = false

        viewModel.uiEvent.test {
            // When
            viewModel.onCommit("Initial commit", onCommitSuccess = { successCallbackCalled = true })

            // Then
            assertThat(awaitItem()).isInstanceOf(UiEvent.Success::class.java)
            assertThat(successCallbackCalled).isTrue()
            assertThat(fakeRepository.lastActionCalled).isEqualTo("getRepoStatus") // Part of refresh
        }
    }

    @Test
    fun `onBranchSelected should checkout and update branch name`() = runTest {
        // Given
        fakeRepoPrefs.saveSelectedRepo(testRepo.localPath)
        val newBranch = Branch("develop", isLocal = true, isRemote = false)

        // When
        viewModel.onBranchSelected(newBranch)

        // Then
        assertThat(viewModel.branchName.value).isEqualTo("develop")
        assertThat(fakeRepository.lastActionCalled).isEqualTo("getTrackingStatus")
    }
}
