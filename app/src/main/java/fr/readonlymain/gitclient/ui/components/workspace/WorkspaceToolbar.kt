package fr.readonlymain.gitclient.ui.components.workspace

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.data.model.Branch
import fr.readonlymain.gitclient.data.model.Repository

sealed class WorkspaceToolbarDialogState {
    data object None : WorkspaceToolbarDialogState()
    data object RepositorySelection : WorkspaceToolbarDialogState()
    data object BranchSelection : WorkspaceToolbarDialogState()
    data object BranchCreation : WorkspaceToolbarDialogState()
    data class BranchDeletion(val branch: Branch) : WorkspaceToolbarDialogState()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WorkspaceToolbar(
    modifier: Modifier,
    isCompact: Boolean = false,
    repositories: List<Repository>,
    branches: List<Branch>,
    selectedBranch: String,
    onRepositorySelected: (String) -> Unit,
    onBranchSelected: (Branch) -> Unit,
    onBranchCreated: (String, Branch, Boolean) -> Unit,
    onBranchDeleted: (Branch, Boolean, Boolean) -> Unit,
    isSynchronizing: Boolean,
    onSynchronize: () -> Unit,
    needPull: Boolean,
    isPulling: Boolean,
    onPull: () -> Unit,
    needPush: Boolean,
    isPushing: Boolean,
    onPush: () -> Unit,
) {
    var activeDialog by remember {
        mutableStateOf<WorkspaceToolbarDialogState>(
            WorkspaceToolbarDialogState.None
        )
    }

    val toolbarMainButtons = @Composable { shape: Shape ->
        IconButton(
            onClick = { activeDialog = WorkspaceToolbarDialogState.RepositorySelection },
            modifier = Modifier.size(48.dp),
            shape = shape, //RoundedCornerShape(8.dp),
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
            shape = shape, //RoundedCornerShape(8.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_outlined_graph_8),
                contentDescription = "Select branch"
            )
        }
    }
    val toolbarSecondaryButtons = @Composable {
        IconButton(
            onClick = { onSynchronize() },
            modifier = Modifier.size(48.dp)
        ) {
            if (isSynchronizing) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filled_close_small),
                        contentDescription = "Cancel",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outlined_sync),
                    contentDescription = "Synchronize"
                )
            }
        }

        IconButton(
            onClick = { onPull() },
            modifier = Modifier.size(48.dp)
        ) {
            if (isPulling) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filled_close_small),
                        contentDescription = "Cancel",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
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
        }
        IconButton(
            onClick = { onPush() },
            modifier = Modifier.size(48.dp)
        ) {
            if (isPushing) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filled_close_small),
                        contentDescription = "Cancel",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
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

    if (isCompact) {
        HorizontalFloatingToolbar(
            expanded = true,
            modifier = modifier.padding(16.dp),
            //colors = androidx.compose.material3.FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            content = {
                Row(
                    horizontalArrangement = spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    toolbarMainButtons(IconButtonDefaults.standardShape)
                    toolbarSecondaryButtons()
                }
            }
        )
    } else {
        Card(
            modifier = Modifier.fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright
            ),
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = spacedBy(16.dp)
            ) {
                toolbarMainButtons(RoundedCornerShape(8.dp))
                toolbarSecondaryButtons()
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
                selectedBranch = selectedBranch,
                onDismiss = { activeDialog = WorkspaceToolbarDialogState.None },
                onBranchSelected = { branchName ->
                    activeDialog = WorkspaceToolbarDialogState.None
                    onBranchSelected(branchName)
                },
                onDeleteBranch = { branchName ->
                    activeDialog = WorkspaceToolbarDialogState.BranchDeletion(branchName)
                },
                onCreateBranch = {
                    activeDialog = WorkspaceToolbarDialogState.BranchCreation
                }
            )
        }

        is WorkspaceToolbarDialogState.BranchCreation -> {
            BranchCreationConfirmDialog(
                branches = branches,
                initialSourceBranch = branches.find { it.name == selectedBranch },
                onDismiss = {
                    activeDialog = WorkspaceToolbarDialogState.BranchSelection
                },
                onConfirm = { branchName, sourceBranch, overwriteExisting ->
                    activeDialog = WorkspaceToolbarDialogState.BranchSelection
                    onBranchCreated(branchName, sourceBranch, overwriteExisting)
                }
            )
        }

        is WorkspaceToolbarDialogState.BranchDeletion -> {
            val branchToDelete = (activeDialog as WorkspaceToolbarDialogState.BranchDeletion).branch
            BranchDeletionConfirmDialog(
                branch = branchToDelete,
                onDismiss = {
                    activeDialog = WorkspaceToolbarDialogState.BranchSelection
                },
                onConfirm = { deleteRemote, forceDelete ->
                    activeDialog = WorkspaceToolbarDialogState.None
                    onBranchDeleted(branchToDelete, deleteRemote, forceDelete)
                }
            )
        }

        else -> {}
    }
}