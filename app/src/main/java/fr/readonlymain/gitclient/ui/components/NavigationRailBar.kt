package fr.readonlymain.gitclient.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    WORKSPACE("Workspace", Icons.Filled.TableRestaurant, Icons.Outlined.TableRestaurant),
    REPOSITORIES("Repositories", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
}

@Composable
fun NavigationRailBar(
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    content: @Composable () -> Unit = {}
) {
    NavigationSuiteScaffold(
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
                    onClick = { onItemSelected(destination.label) }
                )
            }
        }
    ) {
        content()
    }
}
