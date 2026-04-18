package fr.readonlymain.gitclient.ui.components.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * A composable component that represents a single credential entry within a list.
 *
 * Displays account information including a display name and username, along with
 * an action to delete the credential.
 *
 * @param accountName The display name of the account or service.
 * @param username The username associated with the credential.
 * @param onEdit Callback to be invoked when the user requests to edit the credential.
 * @param onDelete Callback to be invoked when the user requests to delete the credential.
 */
@Composable
fun CredentialListItem(
    accountName: String,
    username: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        headlineContent = { Text(accountName) },
        supportingContent = { Text(username, style = MaterialTheme.typography.bodySmall) },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null
            )
        },
        trailingContent = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        }
    )
}