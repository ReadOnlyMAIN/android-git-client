package fr.readonlymain.gitclient.ui.components.workspace

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import fr.readonlymain.gitclient.R
import fr.readonlymain.gitclient.ui.viewmodel.WorkspaceViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CommitAction(
    viewModel: WorkspaceViewModel,
    commitMessageState: TextFieldState,
    isCompact: Boolean,
    onManageFiles: () -> Unit = {},
    onNewCommit: () -> Unit = {}
) {
    var checked by remember { mutableStateOf(false) }
    val size = SplitButtonDefaults.SmallContainerHeight

    Row(
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCompact) {
            SplitButtonLayout(
                leadingButton = {
                    SplitButtonDefaults.LeadingButton(
                        onClick = { onNewCommit() },
                        modifier = Modifier.heightIn(size),
                        shapes = SplitButtonDefaults.leadingButtonShapesFor(size),
                        contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(size),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filled_add_circle),
                            modifier = Modifier.size(
                                SplitButtonDefaults.leadingButtonIconSizeFor(
                                    size
                                )
                            ),
                            contentDescription = "Localized description",
                        )
                        Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
                        Text("New Commit", style = ButtonDefaults.textStyleFor(size))
                    }
                },
                trailingButton = {
                    Box {
                        val description = "Toggle Button"
                        // Icon-only trailing button should have a tooltip for a11y.
                        TooltipBox(
                            positionProvider =
                                TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                            tooltip = {
                                PlainTooltip(
                                    modifier =
                                        Modifier.semantics {
                                            // TODO(b/496338253): Remove this modifier once bug where tooltip
                                            //  text is not announced by a11y screen readers is resolved.
                                            liveRegion = LiveRegionMode.Assertive
                                            paneTitle = description
                                        }
                                ) {
                                    Text(description)
                                }
                            },
                            state = rememberTooltipState(),
                        ) {
                            SplitButtonDefaults.TrailingButton(
                                checked = checked,
                                onCheckedChange = { checked = it },
                                modifier =
                                    Modifier
                                        .heightIn(size)
                                        .semantics {
                                            stateDescription =
                                                if (checked) "Expanded" else "Collapsed"
                                            contentDescription = description
                                        },
                                shapes = SplitButtonDefaults.trailingButtonShapesFor(size),
                                contentPadding = SplitButtonDefaults.trailingButtonContentPaddingFor(
                                    size
                                ),
                            ) {
                                val rotation: Float by
                                animateFloatAsState(
                                    targetValue = if (checked) 180f else 0f,
                                    label = "Trailing Icon Rotation",
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_filled_keyboard_arrow_down),
                                    modifier =
                                        Modifier
                                            .size(SplitButtonDefaults.trailingButtonIconSizeFor(size))
                                            .graphicsLayer {
                                                this.rotationZ = rotation
                                            },
                                    contentDescription = "Localized description",
                                )
                            }
                        }

                        DropdownMenuPopup(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            expanded = checked,
                            onDismissRequest = { checked = false }
                        ) {
                            DropdownMenuGroup(
                                shapes = MenuDefaults.groupShape(0, 1),
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Changes") },
                                    onClick = {
                                        onManageFiles()
                                        checked = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_outlined_difference),
                                            contentDescription = null
                                        )
                                    },
                                )
                            }
                        }
                    }
                },
            )
        } else {
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
    }
}