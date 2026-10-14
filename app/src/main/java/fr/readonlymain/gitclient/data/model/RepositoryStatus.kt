package fr.readonlymain.gitclient.data.model

/** Conflicts remain distinct from ordinary staged and unstaged changes. */
data class RepositoryStatus(
    val unstaged: Set<String> = emptySet(),
    val staged: Set<String> = emptySet(),
    val conflicts: Set<String> = emptySet()
) {
    companion object {
        fun fromMap(status: Map<String, Set<String>>) = RepositoryStatus(
            status["unstaged"].orEmpty(), status["staged"].orEmpty(), status["conflicts"].orEmpty()
        )
    }
}
