package fr.readonlymain.gitclient.ui.components.workspace

import android.os.Environment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.data.model.Repository


@Composable
fun RepositorySelectorDialog(
    repositories: List<Repository>,
    selectedRepository: String,
    onDismiss: () -> Unit,
    onRepositorySelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Switch repository",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(verticalArrangement = spacedBy(8.dp)) {
                    items(repositories) { repo ->
                        val isSelected = repo.name == selectedRepository
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary
                            ) else null,
                            onClick = { onRepositorySelected(repo.localPath) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_outlined_folder),
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = repo.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.basicMarquee(),
                                        maxLines = 1
                                    )
                                    val internalStoragePath =
                                        Environment.getExternalStorageDirectory().absolutePath
                                    Text(
                                        text = repo.localPath.removePrefix(internalStoragePath)
                                            .trimStart('/'),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.basicMarquee(),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
                if (repositories.isEmpty()) {
                    Text("No repositories found.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}