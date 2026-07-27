package fr.readonlymain.gitclient.ui.components.workspace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import fr.readonlymain.gitclient.data.model.Branch

@Composable
fun BranchDeletionConfirmDialog(
    branch: Branch,
    onDismiss: () -> Unit,
    onConfirm: (Boolean, Boolean) -> Unit
) {
    var deleteRemote by remember { mutableStateOf<Boolean>(!branch.isLocal) }
    var forceDelete by remember { mutableStateOf<Boolean>(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Delete ${
                    if (branch.isLocal) {
                        "local"
                    } else {
                        "remote"
                    }
                } branch (" + branch.name + ")"
            )
        },
        text = {
            if (branch.isLocal) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = forceDelete,
                            onCheckedChange = { forceDelete = it }
                        )
                        Text(
                            text = "Force delete (--force)",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            enabled = branch.isRemote,
                            checked = deleteRemote,
                            onCheckedChange = { deleteRemote = it }
                        )
                        Text(
                            text = "Also delete remote branch",
                            color = if (branch.isRemote) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(deleteRemote, forceDelete) }
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}