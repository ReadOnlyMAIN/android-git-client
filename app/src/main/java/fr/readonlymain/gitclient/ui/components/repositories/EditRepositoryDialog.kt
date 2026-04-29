package fr.readonlymain.gitclient.ui.components.repositories

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.readonlymain.gitclient.data.model.Repository

/**
 * A dialog to edit the properties of an existing repository.
 *
 * @param repository The repository to edit.
 * @param onConfirm Callback with the updated repository.
 * @param onDismiss Callback when the dialog is dismissed.
 */
@Composable
fun EditRepositoryDialog(
    repository: Repository,
    onConfirm: (Repository) -> Unit,
    onDismiss: () -> Unit
) {
    var repoUrl by remember { mutableStateOf(repository.remoteUrl) }
    var repoName by remember { mutableStateOf(repository.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Repository") },
        text = {
            Column {
                OutlinedTextField(
                    value = repoName,
                    onValueChange = { repoName = it },
                    label = { Text("Repository Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = { repoUrl = it },
                    label = { Text("Remote URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(repository.copy(remoteUrl = repoUrl, name = repoName))
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
