package fr.readonlymain.gitclient.ui.screen

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.ui.components.ObserveUiEvents
import fr.readonlymain.gitclient.ui.components.workspace.CompactCommitPanel
import fr.readonlymain.gitclient.ui.components.workspace.WorkspaceCommitLists
import fr.readonlymain.gitclient.ui.components.workspace.WorkspaceFileLists
import fr.readonlymain.gitclient.ui.components.workspace.WorkspaceToolbar
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WorkspaceScreen(
    snackbarHostState: SnackbarHostState,
    isCompact: Boolean = false,
    viewModel: WorkspaceViewModel = hiltViewModel()
) {
    val commitMessageState = rememberTextFieldState()

    ObserveUiEvents(viewModel.uiEvent, snackbarHostState)

    var showFileSheet by remember { mutableStateOf(false) }
    var showCommitSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalArrangement = spacedBy(16.dp)
        ) {
            if (isCompact) {
                if (showFileSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showFileSheet = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        WorkspaceFileLists(
                            viewModel = viewModel,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxSize()
                        )
                    }
                }
            } else {
                WorkspaceToolbar(
                    modifier = Modifier,
                    isCompact = isCompact,
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
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            WorkspaceCommitLists(
                viewModel,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                commitMessageState = commitMessageState,
                isCompact = isCompact,
                onManageFiles = { showFileSheet = true },
                onNewCommit = { showCommitSheet = true }
            )
        }

        if (isCompact) {
            WorkspaceToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                isCompact = isCompact,
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
        }
    }

    if (isCompact && showCommitSheet) {
        BasicAlertDialog(
            onDismissRequest = { showCommitSheet = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                topBar = {
                    TopAppBar(
                        title = { Text("Review commit") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                        navigationIcon = {
                            IconButton(onClick = { showCommitSheet = false }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_filled_arrow_back),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                CompactCommitPanel(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize(),
                    viewModel = viewModel,
                    commitMessageState = commitMessageState,
                    onManageFiles = {
                        showFileSheet = true
                    }
                )
            }
        }
    }
}