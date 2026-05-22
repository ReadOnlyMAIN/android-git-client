package fr.readonlymain.gitclient.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.data.model.Repository
import fr.readonlymain.gitclient.data.preferences.CredentialsPreferences
import fr.readonlymain.gitclient.data.preferences.RepositoriesPreferences
import fr.readonlymain.gitclient.ui.components.ObserveUiEvents
import fr.readonlymain.gitclient.ui.components.repositories.CloneRepositoryDialog
import fr.readonlymain.gitclient.ui.components.repositories.EditRepositoryDialog
import fr.readonlymain.gitclient.ui.components.repositories.ImportRepositoryDialog
import fr.readonlymain.gitclient.ui.components.repositories.ManageStoragePermissionWarning
import fr.readonlymain.gitclient.ui.components.repositories.RepositoryCard
import fr.readonlymain.gitclient.ui.viewmodel.RepositoriesViewModel
import kotlinx.coroutines.launch

/**
 * Represents the different UI states for repository-related dialogs in the [RepositoriesScreen].
 */
sealed class RepositoryDialogState {
    data object None : RepositoryDialogState()
    data object Clone : RepositoryDialogState()
    data object Import : RepositoryDialogState()
    data class Edit(val repository: Repository) : RepositoryDialogState()
}

/**
 * Composable screen that displays and manages the list of Git repositories.
 *
 * This screen provides functionality to:
 * - List all saved repositories from [RepositoriesPreferences].
 * - Clone a new repository from a remote URL.
 * - Import an existing local Git repository.
 * - Manage storage permissions required for file system access (especially for Android 11+).
 * - Monitor and display real-time cloning progress.
 * - Delete repository references from the local list.
 *
 * @param viewModel The [RepositoriesViewModel] handling the business logic for cloning and importing.
 */
@SuppressLint("ObsoleteSdkInt") // For compatibility if target SDK is set to an older
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RepositoriesScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: RepositoriesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val credentialsPreferences = remember { CredentialsPreferences(context) }
    val credentials by credentialsPreferences.credentialsFlow.collectAsState(initial = emptyList())
    val repositoriesPreferences = remember { RepositoriesPreferences(context) }
    val repositories by repositoriesPreferences.repositoriesFlow.collectAsState(initial = emptyList())

    // MANAGE_EXTERNAL_STORAGE permission verification
    var hasManageStoragePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasManageStoragePermission = Environment.isExternalStorageManager()
        }
    }

    var activeDialog by remember { mutableStateOf<RepositoryDialogState>(RepositoryDialogState.None) }

    val focusRequester = remember { FocusRequester() }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    ObserveUiEvents(viewModel.uiEvent, snackbarHostState)

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        modifier =
                            Modifier
                                .semantics {
                                    traversalIndex = -1f
                                    stateDescription =
                                        if (fabMenuExpanded) "Expanded" else "Collapsed"
                                    contentDescription = "Add menu"
                                }
                                .animateFloatingActionButton(
                                    visible = !viewModel.isCloning && hasManageStoragePermission || fabMenuExpanded,
                                    alignment = Alignment.BottomEnd,
                                )
                                .focusRequester(focusRequester),
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
                    ) {
                        val iconId =
                            if (checkedProgress > 0.5f) R.drawable.ic_filled_close else R.drawable.ic_filled_add
                        Icon(
                            painter = painterResource(id = iconId),
                            contentDescription = null,
                            modifier = Modifier.animateIcon({ checkedProgress }),
                        )
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        activeDialog = RepositoryDialogState.Import
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filled_folder_open),
                            contentDescription = null
                        )
                    },
                    text = { Text("Import") }
                )

                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        activeDialog = RepositoryDialogState.Clone
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filled_cloud_download),
                            contentDescription = null
                        )
                    },
                    text = { Text("Clone") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (!hasManageStoragePermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ManageStoragePermissionWarning(
                    onAskPermission = {
                        val intent =
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                        permissionLauncher.launch(intent)
                    }
                )

                return@Column
            }

            if (viewModel.isCloning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.progressTask,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (viewModel.progress >= 0f) {
                            LinearProgressIndicator(
                                progress = { viewModel.progress },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            if (repositories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Repositories list (empty)",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = spacedBy(16.dp)
                ) {
                    repositories.forEach { repo ->
                        RepositoryCard(
                            modifier = Modifier.fillMaxWidth(),
                            repositoryData = repo,
                            onEdit = {
                                activeDialog = RepositoryDialogState.Edit(repo)
                            },
                            onDelete = {
                                scope.launch {
                                    repositoriesPreferences.deleteRepository(repo.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        when (activeDialog) {
            is RepositoryDialogState.Clone -> {
                CloneRepositoryDialog(
                    onDismiss = { activeDialog = RepositoryDialogState.None },
                    onConfirm = { typedUrl, selectedUri ->
                        activeDialog = RepositoryDialogState.None
                        if (selectedUri != null) {
                            viewModel.startClone(typedUrl, credentials, selectedUri)
                        }
                    }
                )
            }

            is RepositoryDialogState.Import -> {
                ImportRepositoryDialog(
                    onDismiss = { activeDialog = RepositoryDialogState.None },
                    onConfirm = { selectedUri ->
                        activeDialog = RepositoryDialogState.None
                        if (selectedUri != null) {
                            viewModel.startImport(selectedUri)
                        }
                    }
                )
            }

            is RepositoryDialogState.Edit -> {
                EditRepositoryDialog(
                    repository = (activeDialog as RepositoryDialogState.Edit).repository,
                    onDismiss = { activeDialog = RepositoryDialogState.None },
                    onConfirm = { updatedRepo ->
                        activeDialog = RepositoryDialogState.None
                        viewModel.editRepository(updatedRepo)
                    }
                )
            }

            else -> {}
        }
    }
}