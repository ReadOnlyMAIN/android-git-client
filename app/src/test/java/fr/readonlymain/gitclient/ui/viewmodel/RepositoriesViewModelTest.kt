package fr.readonlymain.gitclient.ui.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.Repository
import fr.readonlymain.gitclient.data.model.UiEvent
import fr.readonlymain.gitclient.data.preferences.FakeRepositoriesPreferences
import fr.readonlymain.gitclient.data.repository.FakeGitRepository
import fr.readonlymain.gitclient.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RepositoriesViewModel
    private lateinit var fakeRepository: FakeGitRepository
    private lateinit var fakeRepoPrefs: FakeRepositoriesPreferences

    @Before
    fun setup() {
        fakeRepository = FakeGitRepository()
        fakeRepoPrefs = FakeRepositoriesPreferences()

        viewModel = RepositoriesViewModel(
            gitRepository = fakeRepository,
            repositoriesPreferences = fakeRepoPrefs
        )
    }

    @Test
    fun `startClone success should save repository and emit success event`() = runTest {
        // Given
        val url = "https://github.com/test/repo.git"
        val localPath = "/storage/emulated/0/Download"
        fakeRepository.shouldFail = false

        viewModel.uiEvent.test {
            // When
            viewModel.startClone(url, emptyList(), localPath)

            // Then
            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.Success::class.java)
            assertThat((event as UiEvent.Success).message).contains("Successfully cloned")
            
            fakeRepoPrefs.repositoriesFlow.test {
                val repos = awaitItem()
                assertThat(repos).hasSize(1)
                assertThat(repos[0].remoteUrl).isEqualTo(url)
                assertThat(repos[0].localPath).isEqualTo("/fake/path")
            }
        }
    }

    @Test
    fun `startClone failure should emit error event`() = runTest {
        // Given
        val url = "https://github.com/test/repo.git"
        val localPath = "/storage/emulated/0/Download"
        fakeRepository.shouldFail = true
        fakeRepository.lastError = "Clone failed"

        viewModel.uiEvent.test {
            // When
            viewModel.startClone(url, emptyList(), localPath)

            // Then
            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.Error::class.java)
            assertThat((event as UiEvent.Error).message).contains("Clone failed")
        }
    }

    @Test
    fun `startImport success should save repository and emit success event`() = runTest {
        // Given
        val localPath = "/storage/emulated/0/ExistingRepo"
        fakeRepository.shouldFail = false

        viewModel.uiEvent.test {
            // When
            viewModel.startImport(localPath)

            // Then
            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.Success::class.java)
            assertThat((event as UiEvent.Success).message).contains("Successfully imported")

            fakeRepoPrefs.repositoriesFlow.test {
                val repos = awaitItem()
                assertThat(repos).hasSize(1)
                assertThat(repos[0].localPath).isEqualTo("/fake/path")
            }
        }
    }

    @Test
    fun `editRepository success should update preferences`() = runTest {
        // Given
        val repo = Repository(name = "Old Name", remoteUrl = "url", localPath = "/path", username = "user")
        fakeRepoPrefs.addRepository(repo)
        val updatedRepo = repo.copy(name = "New Name")
        fakeRepository.shouldFail = false

        // When
        viewModel.editRepository(updatedRepo)

        // Then
        fakeRepoPrefs.repositoriesFlow.test {
            val repos = awaitItem()
            assertThat(repos.find { it.id == repo.id }?.name).isEqualTo("New Name")
        }
    }
}
