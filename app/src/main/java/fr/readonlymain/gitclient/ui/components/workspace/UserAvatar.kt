package fr.readonlymain.gitclient.ui.components.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.SubcomposeAsyncImage
import java.security.MessageDigest

@Composable
fun UserAvatar(
    email: String,
    name: String,
    modifier: Modifier = Modifier
) {
    val avatarUrl = remember(email) {
        val trimmedEmail = email.trim().lowercase()

        if (trimmedEmail.endsWith("@users.noreply.github.com")) {
            val handle = trimmedEmail.substringBefore("@")
            val login = handle.substringAfter("+")
            "https://github.com/$login.png"
        } else {
            // Fallback to Gravatar
            val md = MessageDigest.getInstance("MD5")
            val hash = md.digest(trimmedEmail.toByteArray())
                .joinToString("") { "%02x".format(it) }
            "https://www.gravatar.com/avatar/$hash?d=404"
        }
    }

    // Generate an avatar color based on username (unique but same for each commit).
    val backgroundColor = remember(name) {
        val hashName = name.hashCode()
        Color(
            red = (hashName and 0xFF0000 shr 16) / 255f,
            green = (hashName and 0x00FF00 shr 8) / 255f,
            blue = (hashName and 0x0000FF) / 255f,
            alpha = 1f
        ).copy(alpha = 0.8f)
    }

    Box(
        modifier = modifier
            .background(backgroundColor, CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = avatarUrl,
            contentDescription = name,
            modifier = Modifier.fillMaxSize(),
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            },
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }
}