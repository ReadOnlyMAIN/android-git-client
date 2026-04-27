package fr.readonlymain.gitclient.data.model

/**
 * Enum representing the synchronization status of a commit.
 */
enum class CommitStatus {
    LOCAL_ONLY,
    REMOTE_ONLY,
    SYNCED,
    UNKNOWN
}

/**
 * Data class representing detailed information about a Git commit.
 *
 * @property commitHash The unique SHA-1 hash identifier for the commit.
 * @property authorName The name of the person who created the commit.
 * @property authorEmail The email address of the commit's author.
 * @property status The synchronization status of the commit.
 */
data class CommitInfo(
    val commitHash: String,
    val authorName: String,
    val authorEmail: String,
    val commitMessage: String,
    val commitDate: String,
    val status: CommitStatus = CommitStatus.UNKNOWN
)
