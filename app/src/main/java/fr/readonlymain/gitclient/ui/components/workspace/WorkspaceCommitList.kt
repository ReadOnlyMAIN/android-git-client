package fr.readonlymain.gitclient.ui.components.workspace

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
    commitMessageState: TextFieldState,
    isCompact: Boolean,
    onManageFiles: () -> Unit,
    onNewCommit: () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright
        )
    ) {
        Column(
            Modifier
                .padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = spacedBy(2.dp)
        ) {
            val repoName = viewModel.repoName.value
            val branchName = viewModel.branchName.value

            if (repoName.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Select a repository to see the history",
                        modifier = Modifier
                            .align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                return@Column
            }

            Text(
                "$repoName ($branchName)",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            CommitAction(
                viewModel,
                commitMessageState,
                isCompact = isCompact,
                onManageFiles = onManageFiles,
                onNewCommit = onNewCommit
            )

            val commits = viewModel.commitsByRepo.value

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = spacedBy(4.dp)
            ) {
                items(commits) { commit ->
                    CommitItem(
                        commit = commit,
                        isCompact = isCompact
                    )
                }
                if (isCompact) {
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}