package fr.readonlymain.gitclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.readonlymain.gitclient.data.model.AppDestinations
import fr.readonlymain.gitclient.data.preferences.ThemePreferences
import fr.readonlymain.gitclient.ui.components.NavigationSuiteScaffoldCustom
import fr.readonlymain.gitclient.ui.navigation.AppNavHost
import fr.readonlymain.gitclient.ui.theme.GitClientTheme
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import kotlinx.coroutines.launch

/**
 * The main entry point of the GitClient application.
 *
 * This activity is responsible for:
 * - Initializing the application's root UI using Jetpack Compose.
 * - Setting up dependency injection via Hilt's [AndroidEntryPoint].
 * - Managing the global [ThemeMode] and applying the [GitClientTheme].
 * - Configuring the [NavigationSuiteScaffold] to provide a responsive navigation UI (e.g., Navigation Rail or Bottom Bar).
 * - Hosting the [AppNavHost] to handle screen transitions between [AppDestinations].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()

            val context = LocalContext.current
            val prefs = remember { ThemePreferences(context) }
            val coroutineScope = rememberCoroutineScope()

            val themeMode by prefs.getTheme()
                .collectAsState(initial = ThemeMode.SYSTEM)

            GitClientTheme(themeMode = themeMode) {

                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                NavigationSuiteScaffoldCustom(
                    currentRoute = currentRoute,
                    onChangeRoute = { destinationLabel ->
                        navController.navigate(destinationLabel) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {
                        AppNavHost(
                            navController = navController,
                            themeMode = themeMode,
                            onThemeChange = { newTheme ->
                                coroutineScope.launch {
                                    prefs.setTheme(newTheme)
                                }
                            }
                        )
                    }
                }

                /*NavigationSuiteScaffold(
                    navigationSuiteColors = NavigationSuiteDefaults.colors(
                        navigationRailContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxHeight(),
                    navigationSuiteItems = {
                        AppDestinations.entries.forEach { destination ->
                            item(
                                icon = {
                                    Icon(
                                        imageVector = if (currentRoute == destination.label) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.label
                                    )
                                },
                                label = { Text(destination.label) },
                                selected = currentRoute == destination.label,
                                onClick = {
                                    navController.navigate(destination.label) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {
                        AppNavHost(
                            navController = navController,
                            themeMode = themeMode,
                            onThemeChange = { newTheme ->
                                coroutineScope.launch {
                                    prefs.setTheme(newTheme)
                                }
                            }
                        )
                    }
                }*/
            }
        }
    }
}