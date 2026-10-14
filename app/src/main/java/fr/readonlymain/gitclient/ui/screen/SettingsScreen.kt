package fr.readonlymain.gitclient.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.ui.components.settings.CredentialListItem
import fr.readonlymain.gitclient.ui.components.settings.GitConfigDialog
import fr.readonlymain.gitclient.ui.components.settings.GitCredentialDialog
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import fr.readonlymain.gitclient.ui.viewmodel.SettingsUiState

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    state: SettingsUiState,
    onAddCredential: (String, String, String, () -> Unit) -> Unit,
    onDeleteCredential: (String) -> Unit,
    onSaveGitConfig: (String, String, () -> Unit) -> Unit
) {
    val scrollState = rememberScrollState()
    val credentials = state.credentials
    val gitConfig = state.gitConfig

    val cardsContainerColor = MaterialTheme.colorScheme.surfaceBright

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showGitConfigDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .fillMaxSize()
    ) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = cardsContainerColor
            )
        ) {
            Column {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    ListItem(
                        modifier = Modifier
                            .selectable(
                                selected = (themeMode == mode),
                                onClick = { onThemeChange(mode) },
                                role = Role.RadioButton
                            ),
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        headlineContent = {
                            Text(
                                text = when (mode) {
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.DARK -> "Dark"
                                    ThemeMode.SYSTEM -> "System"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingContent = {
                            RadioButton(
                                selected = (themeMode == mode),
                                onClick = null
                            )
                        }
                    )
                    if (index < ThemeMode.entries.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Git configuration",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = cardsContainerColor
            )
        ) {
            Column {
                ListItem(
                    modifier = Modifier.selectable(
                        selected = false,
                        onClick = { showGitConfigDialog = true },
                        role = Role.Button
                    ),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = {
                        Text(
                            when {
                                gitConfig.name.isNotBlank() -> gitConfig.name
                                else -> "No configuration"
                            }
                        )
                    },
                    supportingContent = {
                        Text(
                            gitConfig.email,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Git credentials (HTTP)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = cardsContainerColor
            )
        ) {
            Column {
                credentials.forEach { credential ->
                    CredentialListItem(
                        username = credential.username,
                        accountName = credential.accountName,
                        onEdit = { /* TODO: implement editing */ },
                        onDelete = {
                            onDeleteCredential(credential.id)
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                ListItem(
                    modifier = Modifier.selectable(
                        selected = false,
                        onClick = { showAddDialog = true },
                        role = Role.Button
                    ),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = {
                        Text(
                            "Add Git credential",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filled_add),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        GitCredentialDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, user, tok ->
                onAddCredential(name, user, tok) { showAddDialog = false }
            }
        )
    }

    if (showGitConfigDialog) {
        GitConfigDialog(
            previousConfig = gitConfig,
            onDismiss = { showGitConfigDialog = false },
            onConfirm = { name, email ->
                onSaveGitConfig(name, email) { showGitConfigDialog = false }
            }
        )
    }
}
