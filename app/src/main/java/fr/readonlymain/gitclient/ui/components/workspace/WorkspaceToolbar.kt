package fr.readonlymain.gitclient.ui.components.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.readonlymain.gitclient.R

@Composable
fun WorkspaceToolbar(
    onNewRepository: () -> Unit,
    onNewBranch: () -> Unit,
    onSynchronize: () -> Unit,
    onPull: () -> Unit,
    onPush: () -> Unit
) {
    var showRepoDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxHeight()
            .padding(end = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { showRepoDialog = true },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors()
            ) {
                Icon(Icons.Outlined.Inventory2, contentDescription = "Select repository")
            }
            IconButton(
                onClick = { /* doSomething() */ },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outlined_graph_8),
                    contentDescription = "Select branch"
                )
            }
            IconButton(
                onClick = { /* doSomething() */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Outlined.Sync, contentDescription = "Synchronize")
            }
            IconButton(
                onClick = { /* doSomething() */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outlined_download),
                    contentDescription = "Pull"
                )
            }
            IconButton(
                onClick = { /* doSomething() */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outlined_upload),
                    contentDescription = "Push"
                )
            }
        }
    }

    if (showRepoDialog) {
        RepositorySelectorDialog(
            onDismiss = { showRepoDialog = false },
            onRepositorySelected = { repoName ->
                showRepoDialog = false
                // After selection logic.
            }
        )
    }
}

@Composable
fun RepositorySelectorDialog(onDismiss: () -> Unit, onRepositorySelected: (String) -> Unit) {
    val mockRepos = listOf("Android-Git-Client", "Personal-Blog", "Work-Project", "Open-Source-Lib")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Switch repository",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(verticalArrangement = spacedBy(8.dp)) {
                    items(mockRepos) { repo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRepositorySelected(repo) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Folder, contentDescription = null)
                                Spacer(Modifier.width(12.dp))
                                Text(repo)
                            }
                        }
                    }
                }
            }
        }
    }
}