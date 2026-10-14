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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceUiState

@Composable
fun WorkspaceFileLists(
    state: WorkspaceUiState,
    modifier: Modifier,
    onDiscard: () -> Unit,
    onStageSelection: () -> Unit,
    onStageAll: () -> Unit,
    onUnstageSelection: () -> Unit,
    onUnstageAll: () -> Unit,
    onToggleUnstaged: (String) -> Unit,
    onToggleStaged: (String) -> Unit
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
                        "Unstaged",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = onDiscard,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_outlined_delete),
                            contentDescription = "Discard"
                        )
                    }
                    IconButton(
                        onClick = onStageSelection,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_filled_keyboard_arrow_down),
                            contentDescription = "Stage selected files"
                        )
                    }
                    IconButton(
                        onClick = onStageAll,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_filled_keyboard_double_arrow_down),
                            contentDescription = "Stage all files"
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
                    // Unstage modified files.
                    val unstaged = state.status.unstaged

                    if (unstaged.isEmpty()) {
                        Text("No changes detected", modifier = Modifier.padding(16.dp))
                    } else {
                        val selectedFiles = state.selectedUnstaged

                        LazyColumn {
                            items(unstaged.toList()) { filePath ->
                                val isSelected = selectedFiles.contains(filePath)

                                Text(
                                    text = filePath,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onToggleUnstaged(
                                                filePath
                                            )
                                        }
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent
                                        )
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
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
                        onClick = onUnstageSelection,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_filled_keyboard_arrow_up),
                            contentDescription = "Unstage selected files"
                        )
                    }
                    IconButton(
                        onClick = onUnstageAll,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_filled_keyboard_double_arrow_up),
                            contentDescription = "Unstage all files"
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
                    val staged = state.status.staged

                    if (staged.isEmpty()) {
                        Text("No changes staged", modifier = Modifier.padding(16.dp))
                    } else {
                        val selectedFiles = state.selectedStaged

                        LazyColumn {
                            items(staged.toList()) { filePath ->
                                val isSelected = selectedFiles.contains(filePath)

                                Text(
                                    text = filePath,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onToggleStaged(
                                                filePath
                                            )
                                        }
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent
                                        )
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
