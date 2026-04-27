package fr.readonlymain.gitclient.ui.screen

import android.content.ClipData
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowDown
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import fr.readonlymain.gitclient.data.model.UiEvent
import fr.readonlymain.gitclient.ui.components.workspace.CommitItem
import fr.readonlymain.gitclient.ui.components.workspace.WorkspaceToolbar
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceViewModel

@Composable
fun WorkspaceScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: WorkspaceViewModel = hiltViewModel()
) {
    val viewModel: WorkspaceViewModel = hiltViewModel()
    val clipboard = LocalClipboard.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            val result = when (event) {
                is UiEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Copy",
                        duration = SnackbarDuration.Long
                    )
                }

                is UiEvent.Success -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = null,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            if (result == SnackbarResult.ActionPerformed && event is UiEvent.Error) {
                clipboard.setClipEntry(
                    ClipEntry(ClipData.newPlainText("error_log", event.message))
                )
            }
        }
    }


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
            onPull = { /**/ },
            onPush = { /**/ }
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
                            onClick = { /* doSomething() */ },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Synchronize"
                            )
                        }
                        IconButton(
                            onClick = { /* doSomething() */ },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Outlined.KeyboardDoubleArrowDown,
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
                            onClick = { /* doSomething() */ },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Outlined.KeyboardArrowUp,
                                contentDescription = "Synchronize"
                            )
                        }
                        IconButton(
                            onClick = { /* doSomething() */ },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Outlined.KeyboardDoubleArrowUp,
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
                        state = rememberTextFieldState(),
                        label = { Text("Commit message") },
                    )
                    Button(
                        onClick = { /*TODO*/ }
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