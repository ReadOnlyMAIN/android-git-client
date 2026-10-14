package fr.readonlymain.gitclient.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.readonlymain.gitclient.ui.screen.RepositoriesScreen
import fr.readonlymain.gitclient.ui.screen.SettingsScreen
import fr.readonlymain.gitclient.ui.screen.WorkspaceScreen
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import fr.readonlymain.gitclient.ui.viewmodel.RepositoriesViewModel
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceViewModel
import fr.readonlymain.gitclient.ui.viewmodel.SettingsViewModel
import fr.readonlymain.gitclient.ui.components.ObserveUiEvents
import fr.readonlymain.gitclient.utils.materialFadeThroughIn
import fr.readonlymain.gitclient.utils.materialFadeThroughOut

@Composable
fun AppNavHost(
    navController: NavHostController,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    snackbarHostState: SnackbarHostState,
    isCompact: Boolean = false,
    workspaceViewModel: WorkspaceViewModel = hiltViewModel(),
    repositoriesViewModel: RepositoriesViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()
    ObserveUiEvents(settingsViewModel.uiEvent, snackbarHostState)
    NavHost(
        navController = navController,
        startDestination = Route.Workspace.route,
        // Global transitions (Material 3 Fade Through)
        enterTransition = { materialFadeThroughIn() },
        exitTransition = { materialFadeThroughOut() },
        popEnterTransition = { materialFadeThroughIn() },
        popExitTransition = { materialFadeThroughOut() }
    ) {
        composable(Route.Workspace.route) {
            WorkspaceScreen(
                snackbarHostState = snackbarHostState,
                isCompact = isCompact,
                viewModel = workspaceViewModel
            )
        }
        composable(Route.Repositories.route) {
            RepositoriesScreen(
                snackbarHostState = snackbarHostState,
                viewModel = repositoriesViewModel
            )
        }
        composable(Route.Settings.route) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeChange = onThemeChange,
                state = settings,
                onAddCredential = { name, username, token, success -> settingsViewModel.addCredential(name, username, token, success) },
                onDeleteCredential = { settingsViewModel.deleteCredential(it) },
                onSaveGitConfig = { name, email, success -> settingsViewModel.setGitConfig(name, email, success) }
            )
        }
    }
}
