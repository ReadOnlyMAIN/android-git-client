package fr.readonlymain.gitclient.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.GitConfig
import fr.readonlymain.gitclient.data.model.Repository
import fr.readonlymain.gitclient.data.preferences.*
import fr.readonlymain.gitclient.data.repository.DefaultGitRepository
import fr.readonlymain.gitclient.data.repository.RepositoryOperationCoordinator
import fr.readonlymain.gitclient.data.repository.SerializedGitRepository
import fr.readonlymain.gitclient.ui.components.workspace.CommitAction
import fr.readonlymain.gitclient.ui.components.workspace.WorkspaceFileLists
import fr.readonlymain.gitclient.ui.theme.GitClientTheme
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.UUID

class WorkspaceWorkflowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private var directory: File? = null

    private fun workspace(): WorkspaceViewModel {
        val root = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "workflow-${UUID.randomUUID()}")
        directory = root
        Git.init().setDirectory(root).setInitialBranch("main").call().use { git ->
            File(root, "file.txt").writeText("initial")
            git.add().addFilepattern(".").call()
            git.commit().setMessage("initial").setAuthor("Test", "test@example.invalid")
                .setCommitter("Test", "test@example.invalid").call()
        }
        File(root, "file.txt").writeText("modified")
        val preferences = FakeRepositoriesPreferences()
        val config = FakeGitConfigPreferences()
        runBlocking {
            preferences.addRepository(Repository(name = "workflow", localPath = root.path))
            preferences.saveSelectedRepo(root.path)
            config.setGitConfig(GitConfig(name = "Test", email = "test@example.invalid"))
        }
        lateinit var vm: WorkspaceViewModel
        compose.runOnUiThread {
            vm = WorkspaceViewModel(SerializedGitRepository(DefaultGitRepository(), RepositoryOperationCoordinator()),
                preferences, FakeCredentialsPreferences(), config)
        }
        return vm
    }

    @After fun cleanup() { directory?.deleteRecursively() }

    @Test fun stage_unstage_and_commit_update_real_repository_without_navigation() {
        val vm = workspace()
        compose.setContent {
            val state = vm.uiState.value
            GitClientTheme(ThemeMode.LIGHT) {
                Column(Modifier.fillMaxSize()) {
                    WorkspaceFileLists(state, Modifier.weight(1f), vm::discardSelection, vm::stageSelection,
                        vm::stageAll, vm::unstageSelection, vm::unstageAll, vm::toggleUnstagedFileSelection, vm::toggleStagedFileSelection)
                    CommitAction(vm::onCommit, rememberTextFieldState(), false)
                }
            }
        }
        compose.waitUntil(10_000) { vm.unstagedFiles.value.contains("file.txt") }
        compose.onNodeWithContentDescription("Stage all files").performClick()
        compose.waitUntil(10_000) { vm.stagedFiles.value.contains("file.txt") }
        compose.onNodeWithContentDescription("Unstage all files").performClick()
        compose.waitUntil(10_000) { vm.stagedFiles.value.isEmpty() }
        compose.onNodeWithContentDescription("Stage all files").performClick()
        compose.waitUntil(10_000) { vm.stagedFiles.value.contains("file.txt") }
        compose.onNodeWithText("Commit message").performTextInput("UI validation commit")
        compose.onNodeWithText("Commit", useUnmergedTree = true).performClick()
        compose.waitUntil(10_000) { vm.commitsByRepo.value.any { it.commitMessage == "UI validation commit" } }
        assertThat(vm.stagedFiles.value).isEmpty()
        Git.open(directory!!).use { git -> assertThat(git.log().call().first().shortMessage).isEqualTo("UI validation commit") }
    }

    @Test fun compact_workspace_opens_and_closes_commit_panel() {
        val vm = workspace()
        compose.setContent {
            GitClientTheme(ThemeMode.LIGHT) {
                WorkspaceScreen(remember { SnackbarHostState() }, isCompact = true, viewModel = vm)
            }
        }
        compose.waitUntil(10_000) { vm.unstagedFiles.value.isNotEmpty() }
        compose.onNodeWithText("New Commit").performClick()
        compose.onNodeWithText("Review commit").assertExists()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Review commit").assertDoesNotExist()
    }
}
