package fr.readonlymain.gitclient.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import fr.readonlymain.gitclient.data.model.Repository
import fr.readonlymain.gitclient.data.preferences.FakeCredentialsPreferences
import fr.readonlymain.gitclient.data.preferences.FakeGitConfigPreferences
import fr.readonlymain.gitclient.data.preferences.FakeRepositoriesPreferences
import fr.readonlymain.gitclient.data.repository.FakeGitRepository
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import fr.readonlymain.gitclient.ui.viewmodel.RepositoriesViewModel
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppNavHostTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var fakeRepo: FakeGitRepository
    private lateinit var fakePrefs: FakeRepositoriesPreferences
    private lateinit var fakeConfig: FakeGitConfigPreferences
    private lateinit var fakeCredentials: FakeCredentialsPreferences
    private lateinit var workspaceViewModel: WorkspaceViewModel
    private lateinit var repositoriesViewModel: RepositoriesViewModel

    @Before
    fun setup() {
        fakeRepo = FakeGitRepository()
        fakePrefs = FakeRepositoriesPreferences()
        fakeConfig = FakeGitConfigPreferences()
        fakeCredentials = FakeCredentialsPreferences()

        workspaceViewModel = WorkspaceViewModel(
            fakeRepo, fakePrefs, fakeCredentials, fakeConfig
        )
        repositoriesViewModel = RepositoriesViewModel(
            fakeRepo, fakePrefs
        )
    }

    @Test
    fun verify_startDestination_is_Workspace() {
        // Setup some initial data
        val repoPath = "/fake/repo"
        kotlinx.coroutines.runBlocking {
            fakePrefs.addRepository(Repository("Test Repo", "url", repoPath, "user"))
            fakePrefs.saveSelectedRepo(repoPath)
        }

        composeTestRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            AppNavHost(
                navController = rememberNavController(),
                themeMode = ThemeMode.SYSTEM,
                onThemeChange = {},
                snackbarHostState = snackbarHostState,
                workspaceViewModel = workspaceViewModel,
                repositoriesViewModel = repositoriesViewModel
            )
        }

        composeTestRule.onNodeWithText("Unstaged").assertExists()
    }

    @Test
    fun navigate_to_repositories() {
        lateinit var navController: androidx.navigation.NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            AppNavHost(
                navController = navController,
                themeMode = ThemeMode.SYSTEM,
                onThemeChange = {},
                snackbarHostState = snackbarHostState,
                workspaceViewModel = workspaceViewModel,
                repositoriesViewModel = repositoriesViewModel
            )
        }

        composeTestRule.runOnUiThread {
            navController.navigate(Route.Repositories.route)
        }
        composeTestRule.waitForIdle()

        // Check if either the list is shown or the permission warning
        try {
            composeTestRule.onNodeWithText("Repositories list (empty)").assertExists()
        } catch (e: AssertionError) {
            composeTestRule.onNodeWithText("Permission is required").assertExists()
        }
    }

    @Test
    fun navigate_to_settings() {
        lateinit var navController: androidx.navigation.NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            AppNavHost(
                navController = navController,
                themeMode = ThemeMode.SYSTEM,
                onThemeChange = { },
                snackbarHostState = snackbarHostState,
                workspaceViewModel = workspaceViewModel,
                repositoriesViewModel = repositoriesViewModel
            )
        }

        composeTestRule.runOnUiThread {
            navController.navigate(Route.Settings.route)
        }

        composeTestRule.onNodeWithText("Theme").assertExists()
    }

    @Test
    fun navigate_to_workspace() {
        lateinit var navController: androidx.navigation.NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            AppNavHost(
                navController = navController,
                themeMode = ThemeMode.SYSTEM,
                onThemeChange = {},
                snackbarHostState = snackbarHostState,
                workspaceViewModel = workspaceViewModel,
                repositoriesViewModel = repositoriesViewModel
            )
        }

        composeTestRule.runOnUiThread {
            navController.navigate(Route.Settings.route)
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnUiThread {
            navController.navigate(Route.Workspace.route)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Unstaged").assertExists()
    }
}