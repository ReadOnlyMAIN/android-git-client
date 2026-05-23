package fr.readonlymain.gitclient.ui.screen

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import fr.readonlymain.gitclient.ui.components.ObserveUiEvents
import fr.readonlymain.gitclient.ui.components.workspace.WorkspaceCommitLists
import fr.readonlymain.gitclient.ui.components.workspace.WorkspaceFileLists
import fr.readonlymain.gitclient.ui.components.workspace.WorkspaceToolbar
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceViewModel

@Composable
fun WorkspaceScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: WorkspaceViewModel = hiltViewModel()
) {
    val commitMessageState = rememberTextFieldState()

    ObserveUiEvents(viewModel.uiEvent, snackbarHostState)

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        horizontalArrangement = spacedBy(16.dp)
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

        WorkspaceFileLists(
            viewModel,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        WorkspaceCommitLists(
            viewModel,
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
            commitMessageState = commitMessageState
        )
    }
}