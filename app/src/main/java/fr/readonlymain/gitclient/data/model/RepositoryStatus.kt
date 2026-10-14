package fr.readonlymain.gitclient.data.model

/** Conflicts remain distinct from ordinary staged and unstaged changes. */
data class RepositoryStatus(
    val unstaged: Set<String> = emptySet(),
    val staged: Set<String> = emptySet(),
    val conflicts: Set<String> = emptySet()
) {
    fun asLegacyMap(): Map<String, Set<String>> = mapOf(
        "unstaged" to unstaged, "staged" to staged, "conflicts" to conflicts
    )
}
