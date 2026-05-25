package fr.readonlymain.gitclient.ui.components.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceViewModel

@Composable
fun CompactCommitPanel(
    modifier: Modifier,
    viewModel: WorkspaceViewModel,
    commitMessageState: TextFieldState,
    onManageFiles: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright
            )
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Staged",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = { onManageFiles() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_outlined_edit),
                            contentDescription = "Edit"
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    )
                ) {
                    val staged = viewModel.stagedFiles.value

                    if (staged.isEmpty()) {
                        Text("No changes staged", modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn {
                            items(staged.toList()) { filePath ->
                                Text(
                                    text = filePath,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.toggleStagedFileSelection(
                                                filePath
                                            )
                                        }
                                        .background(Color.Transparent)
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            state = commitMessageState,
            label = { Text("Commit message") },
        )
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                viewModel.onCommit(commitMessageState.text.toString()) {
                    commitMessageState.edit { delete(0, length) }
                }
            }
        ) {
            Text(
                "Commit",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}