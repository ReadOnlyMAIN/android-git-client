package fr.readonlymain.gitclient.ui.components.workspace

import androidx.compose.foundation.clickable
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
import fr.readonlymain.gitclient.data.model.Branch


@Composable
fun BranchSelectorDialog(
    branches: List<Branch>,
    onDismiss: () -> Unit,
    onBranchSelected: (Branch) -> Unit
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
                    text = "Switch branch",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(verticalArrangement = spacedBy(8.dp)) {
                    items(branches) { branch ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBranchSelected(branch) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            onClick = { onBranchSelected(branch) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = branch.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    painterResource(id = R.drawable.ic_outlined_computer),
                                    contentDescription = "local branch",
                                    tint = if (branch.isLocal) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    painterResource(id = R.drawable.ic_outlined_cloud),
                                    contentDescription = "remote branch",
                                    //tint = MaterialTheme.colorScheme.surfaceContainerHighest
                                    tint = if (branch.isRemote) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
                if (branches.isEmpty()) {
                    Text("No branch found.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}