package fr.readonlymain.gitclient.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.ui.components.ObserveUiEvents
import fr.readonlymain.gitclient.ui.components.workspace.CommitItem
import fr.readonlymain.gitclient.ui.components.workspace.WorkspaceToolbar
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceViewModel

@Composable
fun WorkspaceScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: WorkspaceViewModel = hiltViewModel()
) {
    val viewModel: WorkspaceViewModel = hiltViewModel()

    val commitMessageState = rememberTextFieldState()

    ObserveUiEvents(viewModel.uiEvent, snackbarHostState)

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        WorkspaceToolbar(
            repositories = viewModel.repositories.value,
            branches = viewModel.branches.value,
            onRepositorySelected = { path ->
                viewModel.onRepositorySelected(path)
            },
            onBranchSelected = { branch ->
                viewModel.onBranchSelected(branch)
            },
            onSynchronize = { viewModel.onSynchronize() },
            needPull = viewModel.needPull.value,
            onPull = { viewModel.onPull() },
            needPush = viewModel.needPush.value,
            onPush = { viewModel.onPush() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            verticalArrangement = spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
                            style = MaterialTheme.typography.headlineMedium
                        )
                        IconButton(
                            onClick = { viewModel.discardSelection() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_outlined_delete),
                                contentDescription = "Discard"
                            )
                        }
                        IconButton(
                            onClick = { viewModel.stageSelection() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_filled_keyboard_arrow_down),
                                contentDescription = "Synchronize"
                            )
                        }
                        IconButton(
                            onClick = { viewModel.stageAll() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_filled_keyboard_double_arrow_down),
                                contentDescription = "Synchronize"
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
                        val unstaged = viewModel.unstagedFiles.value

                        if (unstaged.isEmpty()) {
                            Text("No changes detected", modifier = Modifier.padding(16.dp))
                        } else {
                            val selectedFiles = viewModel.selectedUnstagedFiles.value

                            LazyColumn {
                                items(unstaged.toList()) { filePath ->
                                    val isSelected = selectedFiles.contains(filePath)

                                    Text(
                                        text = filePath,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.toggleUnstagedFileSelection(
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
                    containerColor = MaterialTheme.colorScheme.surface
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
                            style = MaterialTheme.typography.headlineMedium
                        )
                        IconButton(
                            onClick = { viewModel.unstageSelection() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_filled_keyboard_arrow_up),
                                contentDescription = "Synchronize"
                            )
                        }
                        IconButton(
                            onClick = { viewModel.unstageAll() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_filled_keyboard_double_arrow_up),
                                contentDescription = "Synchronize"
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
                            val selectedFiles = viewModel.selectedStagedFiles.value

                            LazyColumn {
                                items(staged.toList()) { filePath ->
                                    val isSelected = selectedFiles.contains(filePath)

                                    Text(
                                        text = filePath,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.toggleStagedFileSelection(
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

        Card(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(start = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = spacedBy(2.dp)
            ) {
                val repoName = viewModel.repoName.value
                val branchName = viewModel.branchName.value
                Text(
                    "$repoName ($branchName)",
                    style = MaterialTheme.typography.headlineSmall
                )

                Row(
                    horizontalArrangement = spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.extraSmall.copy(
                                    topStart = CornerSize(50),
                                    bottomStart = CornerSize(50)
                                )
                            )
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f),
                        state = commitMessageState,
                        label = { Text("Commit message") },
                    )
                    Button(
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

                val commits = viewModel.commitsByRepo.value

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = spacedBy(4.dp)
                ) {
                    items(commits) { commit ->
                        CommitItem(commit = commit)
                    }
                }
            }
        }
    }
}