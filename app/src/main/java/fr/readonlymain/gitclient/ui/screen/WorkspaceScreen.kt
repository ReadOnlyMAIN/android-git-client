package fr.readonlymain.gitclient.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowUp
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowDown
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape

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
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = spacedBy(16.dp)
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

        Column(modifier = Modifier
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Synchronize")
                        }
                        IconButton(
                            onClick = { /* doSomething() */ },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Outlined.KeyboardDoubleArrowDown, contentDescription = "Synchronize")
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background,
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                            Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "Synchronize")
                        }
                        IconButton(
                            onClick = { /* doSomething() */ },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Outlined.KeyboardDoubleArrowUp, contentDescription = "Synchronize")
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        )
                    ) {

                    }
                }
            }
        }

        val row_height = 56.dp
        val shape_height = row_height - 16.dp

        Card(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = spacedBy(2.dp)
            ) {
                Row(
                    //modifier = Modifier.height(row_height),
                    horizontalArrangement = spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(shape_height)
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
                        lineLimits = TextFieldLineLimits.SingleLine,
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

                // Sample commits

                Box(
                    modifier = Modifier
                        .padding(start = (shape_height / 2) - 2.dp)
                        .width(4.dp)
                        .height(shape_height)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(
                                topStartPercent = 50,
                                bottomStartPercent = 50,
                                topEndPercent = 50,
                                bottomEndPercent = 50
                            )
                        )
                )

                Row(
                    modifier = Modifier.height(row_height),
                    horizontalArrangement = spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(shape_height)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                    Text(
                        "Fix layout issue",
                        Modifier
                            .weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .height(shape_height)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        modifier = Modifier
                            .width(128.dp),
                        text = "John Doe",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    SuggestionChip(
                        onClick = { /* onClick */ },
                        label = {
                            Text(
                                "a1b2c3d",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        shape = CircleShape
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(start = (shape_height / 2) - 2.dp)
                        .width(4.dp)
                        .height(shape_height)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(
                                topStartPercent = 50,
                                bottomStartPercent = 50,
                                topEndPercent = 50,
                                bottomEndPercent = 50
                            )
                        )
                )

                Row(
                    modifier = Modifier.height(row_height),
                    horizontalArrangement = spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(shape_height)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                    Text(
                        "Fix layout issue",
                        Modifier
                            .weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .height(shape_height)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        modifier = Modifier
                            .width(128.dp),
                        text = "enivort@enivort.enivort",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    SuggestionChip(
                        onClick = { /* onClick */ },
                        label = {
                            Text(
                                "a1b2c3d",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        shape = CircleShape
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(start = (shape_height / 2) - 2.dp)
                        .width(4.dp)
                        .height(shape_height)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(
                                topStartPercent = 50,
                                bottomStartPercent = 50,
                                topEndPercent = 50,
                                bottomEndPercent = 50
                            )
                        )
                )

                Row(
                    modifier = Modifier.height(row_height),
                    horizontalArrangement = spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(shape_height)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                    Text(
                        "Fix layout issue",
                        Modifier
                            .weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .height(shape_height)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        modifier = Modifier
                            .width(128.dp),
                        text = "ReadOnlyMAIN",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    SuggestionChip(
                        onClick = { /* onClick */ },
                        label = {
                            Text(
                                "a1b2c3d",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        shape = CircleShape
                    )
                }
            }
        }
    }
}