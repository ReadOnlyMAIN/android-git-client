package fr.readonlymain.gitclient.data.model

/**
 * Data class representing detailed information about a Git commit.
 *
 * @property commitHash The unique SHA-1 hash identifier for the commit.
 * @property authorName The name of the person who created the commit.
 * @property authorEmail The email address of the commit's author.
 */
data class CommitInfo(
    val commitHash: String,
    val authorName: String,
    val authorEmail: String,
    val commitMessage: String,
    val commitDate: String
)
