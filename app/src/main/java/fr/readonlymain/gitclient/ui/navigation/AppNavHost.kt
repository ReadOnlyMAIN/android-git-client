package fr.readonlymain.gitclient.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.readonlymain.gitclient.ui.screen.RepositoriesScreen
import fr.readonlymain.gitclient.ui.screen.SettingsScreen
import fr.readonlymain.gitclient.ui.screen.WorkspaceScreen
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import kotlinx.coroutines.coroutineScope

@Composable
fun AppNavHost(
    navController: NavHostController,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Route.Workspace.route
    ) {
        composable(Route.Workspace.route) {
            WorkspaceScreen()
        }
        composable(Route.Repositories.route) {
            RepositoriesScreen()
        }
        composable(Route.Settings.route) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeChange = onThemeChange
            )
        }
    }
}