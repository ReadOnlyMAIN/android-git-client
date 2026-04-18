package fr.readonlymain.gitclient.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableRestaurant
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