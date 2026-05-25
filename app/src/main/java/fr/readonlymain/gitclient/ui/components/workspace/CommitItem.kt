package fr.readonlymain.gitclient.ui.components.workspace

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.readonlymain.gitclient.data.model.CommitInfo
import fr.readonlymain.gitclient.data.model.CommitStatus
import kotlinx.coroutines.launch

@Composable
fun CommitItem(
    commit: CommitInfo,
    isCompact: Boolean = false
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val rowHeight = 56.dp
    val shapeHeight = rowHeight - 16.dp

    val statusColor = when (commit.status) {
        CommitStatus.SYNCED -> MaterialTheme.colorScheme.primary
        CommitStatus.LOCAL_ONLY -> MaterialTheme.colorScheme.primary
        CommitStatus.REMOTE_ONLY -> MaterialTheme.colorScheme.outline
        CommitStatus.UNKNOWN -> MaterialTheme.colorScheme.error
    }

    val commitShapeModifier = when (commit.status) {
        CommitStatus.SYNCED -> Modifier
            .height(shapeHeight)
            .aspectRatio(1f)
            .background(
                color = statusColor,
                shape = CircleShape
            )

        CommitStatus.LOCAL_ONLY -> Modifier
            .height(shapeHeight)
            .aspectRatio(1f)
            .border(
                width = 4.dp,
                color = statusColor,
                shape = CircleShape
            )

        else -> Modifier
            .padding(horizontal = (shapeHeight / 2) - 2.dp)
            .width(4.dp)
            .height(shapeHeight)
            .background(
                color = statusColor,
                shape = RoundedCornerShape(
                    topStartPercent = 50,
                    bottomStartPercent = 50,
                    topEndPercent = 50,
                    bottomEndPercent = 50
                )
            )
    }

    Box(
        modifier = Modifier
            .padding(start = (shapeHeight / 2) - 2.dp)
            .width(4.dp)
            .height(shapeHeight / 2)
            .background(
                color = statusColor,
                shape = RoundedCornerShape(
                    topStartPercent = 50,
                    bottomStartPercent = 50,
                    topEndPercent = 50,
                    bottomEndPercent = 50
                )
            )
    )

    //TODO: maybe add commit.commitDate label ?
    Row(
        modifier = Modifier.height(rowHeight),
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isCompact || commit.status != CommitStatus.SYNCED) {
            Box(
                modifier = commitShapeModifier
            )
        } else {
            UserAvatar(
                email = commit.authorEmail,
                name = commit.authorName,
                modifier = Modifier
                    .height(shapeHeight)
                    .aspectRatio(1f)
            )
        }
        Text(
            commit.commitMessage,
            Modifier
                .weight(1f),
            style = MaterialTheme.typography.titleMedium
        )
        if (!isCompact) {
            UserAvatar(
                email = commit.authorEmail,
                name = commit.authorName,
                modifier = Modifier
                    .height(shapeHeight)
                    .aspectRatio(1f)
            )
            Text(
                modifier = Modifier.width(128.dp),
                text = commit.authorName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SuggestionChip(
                modifier = Modifier.width(80.dp),
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText("commit_hash", commit.commitHash)
                            )
                        )
                    }
                    android.widget.Toast.makeText(
                        context,
                        "Full hash copied to clipboard.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                label = {
                    Text(
                        text = commit.commitHash.take(7),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                },
                shape = CircleShape
            )
        }
    }
}