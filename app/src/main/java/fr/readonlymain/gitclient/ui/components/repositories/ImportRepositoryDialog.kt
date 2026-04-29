package fr.readonlymain.gitclient.ui.components.repositories

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.readonlymain.gitclient.R


/**
 * A dialog component that allows the user to select a local directory to import as a Git repository.
 *
 * It utilizes the system's directory picker to let the user navigate and choose a folder.
 * The "Import" button remains disabled until a valid directory is selected.
 *
 * @param onConfirm Callback invoked when the user confirms the selection, providing the [Uri] of the chosen directory.
 */
@Composable
fun ImportRepositoryDialog(
    onConfirm: (Uri?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        selectedUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import local repository") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { directoryPickerLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filled_folder),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Directory")
                }

                selectedUri?.let {
                    Text(
                        text = "Selected: ${it.path?.split(":")?.lastOrNull()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = (selectedUri != null),
                onClick = { onConfirm(selectedUri) }
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}