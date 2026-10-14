package fr.readonlymain.gitclient.data.model

/** Keep remote identity even when several remotes expose the same branch name. */
fun branchesFromRefs(refs: List<String>, upstreams: Map<String, String?> = emptyMap()): List<Branch> {
    val locals = refs.filter { it.startsWith("refs/heads/") }
    val remotes = refs.filter { it.startsWith("refs/remotes/") && !it.endsWith("/HEAD") }.toMutableSet()
    val localNames = locals.mapTo(mutableSetOf()) { it.removePrefix("refs/heads/") }
    val remoteNameCounts = remotes.groupingBy { it.removePrefix("refs/remotes/").substringAfter('/') }.eachCount()
    val result = locals.map { ref ->
        val name = ref.removePrefix("refs/heads/")
        val tracking = upstreams[name]?.takeIf { it in remotes }
        if (tracking != null) remotes.remove(tracking)
        Branch(name, true, tracking != null, ref, tracking)
    }.toMutableList()
    remotes.forEach { ref ->
        val qualified = ref.removePrefix("refs/remotes/")
        val short = qualified.substringAfter('/')
        val ambiguous = short in localNames || remoteNameCounts.getValue(short) > 1
        result += Branch(if (ambiguous) qualified else short, false, true, remoteRef = ref)
    }
    return result.sortedBy { it.name }
}
