package fr.readonlymain.gitclient.ui.components.repositories

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable component that displays a warning card when the "Manage External Storage" permission is missing.
 *
 * This UI notifies the user that the application requires access to all files to successfully
 * clone repositories to custom locations on the device. It provides a button to trigger
 * the permission request process.
 *
 * @param onAskPermission Callback invoked when the user clicks the button to grant the required permission.
 */
@Composable
fun ManageStoragePermissionWarning(
    onAskPermission: () -> Unit
) {
    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Permission is required",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                "The app needs access to all files in order to clone repositories anywhere you need on your device.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = { onAskPermission() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Allow permission")
            }
        }
    }
}