package fr.readonlymain.gitclient.ui.components.repositories

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.readonlymain.gitclient.data.model.Repository

/**
 * A composable component that displays a summary of a Git repository within a card.
 *
 * It shows the repository's name and remote URL, and provides a button to trigger
 * a deletion action.
 *
 * @param modifier The [Modifier] to be applied to the card.
 * @param repositoryData The [Repository] data object containing the information to display.
 */
@Composable
fun RepositoryCard(
    modifier: Modifier = Modifier,
    repositoryData: Repository,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        ListItem(
            modifier = Modifier.clickable {
                onEdit()
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            headlineContent = { Text(repositoryData.name) },
            supportingContent = {
                Text(
                    repositoryData.remoteUrl,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.Inventory2,
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
}