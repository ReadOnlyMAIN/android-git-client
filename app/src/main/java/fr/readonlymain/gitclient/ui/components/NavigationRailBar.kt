package fr.readonlymain.gitclient.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.ui.navigation.Route

@Composable
fun NavigationRailBar(
    currentRoute: String?,
    onItemSelected: (String) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
    ) {
        NavigationRailItem(
            selected = currentRoute == Route.Workspace.route,
            onClick = { onItemSelected(Route.Workspace.route) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_table_restaurant),
                    contentDescription = "Workspace",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            label = { Text("Workspace") }
        )
        NavigationRailItem(
            selected = currentRoute == Route.Repositories.route,
            onClick = { onItemSelected(Route.Repositories.route) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_inventory_2),
                    contentDescription = "Workspace",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            label = { Text("Home") }
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationRailItem(
            selected = currentRoute == Route.Settings.route,
            onClick = { onItemSelected(Route.Settings.route) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Workspace",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            label = { Text("Settings") }
        )

    }
}