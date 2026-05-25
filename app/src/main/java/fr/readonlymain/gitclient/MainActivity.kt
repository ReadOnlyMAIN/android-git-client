package fr.readonlymain.gitclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
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

            val snackbarHostState = remember { SnackbarHostState() }

            val isCompact =
                !currentWindowAdaptiveInfo().windowSizeClass.isAtLeastBreakpoint(
                    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
                    WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
                )

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
                    Scaffold(
                        containerColor = Color.Transparent,
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState) { data ->
                                val isError = data.visuals.actionLabel == "Copy"

                                Snackbar(
                                    containerColor = if (isError) MaterialTheme.colorScheme.onErrorContainer
                                    else SnackbarDefaults.color,
                                    contentColor = if (isError) MaterialTheme.colorScheme.errorContainer
                                    else SnackbarDefaults.contentColor,
                                    action = data.visuals.actionLabel?.let { actionLabel ->
                                        {
                                            TextButton(onClick = { data.performAction() }) {
                                                Text(actionLabel)
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = data.visuals.message,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        val screenPadding =
                            if (isCompact) PaddingValues(top = padding.calculateTopPadding()) else padding
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                //.systemBarsPadding()
                                .padding(screenPadding)
                        ) {
                            AppNavHost(
                                navController = navController,
                                themeMode = themeMode,
                                onThemeChange = { newTheme ->
                                    coroutineScope.launch {
                                        prefs.setTheme(newTheme)
                                    }
                                },
                                snackbarHostState = snackbarHostState,
                                isCompact = isCompact
                            )
                        }
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