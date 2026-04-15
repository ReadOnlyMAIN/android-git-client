package fr.readonlymain.gitclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.readonlymain.gitclient.data.ThemePreferences
import fr.readonlymain.gitclient.ui.components.NavigationRailBar
import fr.readonlymain.gitclient.ui.navigation.AppNavHost
import fr.readonlymain.gitclient.ui.theme.GitClientTheme
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import kotlinx.coroutines.launch

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
            val oledMode by prefs.getOledMode()
                .collectAsState(initial = false)

            GitClientTheme(themeMode = themeMode, oledMode = oledMode) {

                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    NavigationRailBar(
                        currentRoute = currentRoute,
                        onItemSelected = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) {
                        AppNavHost(
                            innerPadding = innerPadding,
                            navController = navController,
                            themeMode = themeMode,
                            oledMode = oledMode,
                            onThemeChange = { newTheme ->
                                coroutineScope.launch {
                                    prefs.setTheme(newTheme)
                                }
                            },
                            onOledChange = { enabled ->
                                coroutineScope.launch {
                                    prefs.setOledMode(enabled)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}