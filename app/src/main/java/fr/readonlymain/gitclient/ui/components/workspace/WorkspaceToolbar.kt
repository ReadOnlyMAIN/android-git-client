package fr.readonlymain.gitclient.ui.components.workspace

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.Repository

sealed class WorkspaceToolbarDialogState {
    data object None : WorkspaceToolbarDialogState()
    data object RepositorySelection : WorkspaceToolbarDialogState()
    data object BranchSelection : WorkspaceToolbarDialogState()
}

@Composable
fun WorkspaceToolbar(
    repositories: List<Repository>,
    branches: List<Branch>,
    onRepositorySelected: (String) -> Unit,
    onBranchSelected: (Branch) -> Unit,
    onSynchronize: () -> Unit,
    needPull: Boolean,
    onPull: () -> Unit,
    needPush: Boolean,
    onPush: () -> Unit
) {
    var activeDialog by remember {
        mutableStateOf<WorkspaceToolbarDialogState>(
            WorkspaceToolbarDialogState.None
        )
    }

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
                onClick = { activeDialog = WorkspaceToolbarDialogState.RepositorySelection },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outlined_inventory_2),
                    contentDescription = "Select repository"
                )
            }
            IconButton(
                onClick = { activeDialog = WorkspaceToolbarDialogState.BranchSelection },
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
                onClick = { onSynchronize() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outlined_sync),
                    contentDescription = "Synchronize"
                )
            }

            IconButton(
                onClick = { onPull() },
                modifier = Modifier.size(48.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (needPull) {
                            Badge()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_outlined_download),
                        contentDescription = "Pull"
                    )
                }
            }
            IconButton(
                onClick = { onPush() },
                modifier = Modifier.size(48.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (needPush) {
                            Badge()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_outlined_upload),
                        contentDescription = "Push"
                    )
                }
            }
        }
    }

    when (activeDialog) {
        is WorkspaceToolbarDialogState.RepositorySelection -> {
            RepositorySelectorDialog(
                repositories = repositories,
                onDismiss = { activeDialog = WorkspaceToolbarDialogState.None },
                onRepositorySelected = { repoName ->
                    activeDialog = WorkspaceToolbarDialogState.None
                    onRepositorySelected(repoName)
                }
            )
        }

        is WorkspaceToolbarDialogState.BranchSelection -> {
            BranchSelectorDialog(
                branches = branches,
                onDismiss = { activeDialog = WorkspaceToolbarDialogState.None },
                onBranchSelected = { branchName ->
                    activeDialog = WorkspaceToolbarDialogState.None
                    onBranchSelected(branchName)
                }
            )
        }

        else -> {}
    }
}