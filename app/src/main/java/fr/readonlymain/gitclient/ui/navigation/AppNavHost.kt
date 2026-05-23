package fr.readonlymain.gitclient.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.readonlymain.gitclient.ui.screen.RepositoriesScreen
import fr.readonlymain.gitclient.ui.screen.SettingsScreen
import fr.readonlymain.gitclient.ui.screen.WorkspaceScreen
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import fr.readonlymain.gitclient.utils.materialFadeThroughIn
import fr.readonlymain.gitclient.utils.materialFadeThroughOut

@Composable
fun AppNavHost(
    navController: NavHostController,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    snackbarHostState: SnackbarHostState
) {
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
                snackbarHostState = snackbarHostState
            )
        }
        composable(Route.Repositories.route) {
            RepositoriesScreen(
                snackbarHostState = snackbarHostState
            )
        }
        composable(Route.Settings.route) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeChange = onThemeChange
            )
        }
    }
}