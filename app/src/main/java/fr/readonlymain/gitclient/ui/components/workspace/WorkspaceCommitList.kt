package fr.readonlymain.gitclient.ui.components.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceViewModel

@Composable
fun WorkspaceCommitLists(
    viewModel: WorkspaceViewModel,
    modifier: Modifier,
    commitMessageState: TextFieldState
) {
    Card(
        modifier = modifier,
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