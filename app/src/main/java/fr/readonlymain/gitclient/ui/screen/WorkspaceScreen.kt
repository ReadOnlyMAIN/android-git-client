package fr.readonlymain.gitclient.ui.screen

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload

@Composable
fun WorkspaceScreen(
    innerPadding: PaddingValues
) {
    Row(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                Modifier.padding(8.dp),
                verticalArrangement = spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { /* doSomething() */ },
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors()
                ) {
                    Icon(Icons.Outlined.Inventory2, contentDescription = "Select repository")
                }
                IconButton(
                    onClick = { /* doSomething() */ },
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors()
                ) {
                    Icon(Icons.Outlined.AccountTree, contentDescription = "Select branch")
                }
                IconButton(
                    onClick = { /* doSomething() */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Outlined.Sync, contentDescription = "Synchronize")
                }
                IconButton(
                    onClick = { /* doSomething() */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = "Pull")
                }
                IconButton(
                    onClick = { /* doSomething() */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Outlined.Upload, contentDescription = "Push")
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 8.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Panel 1")
            }
        }

        Card(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(horizontal = 8.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Panel 2")
            }
        }
    }
}