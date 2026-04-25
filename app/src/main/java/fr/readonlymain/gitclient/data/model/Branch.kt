package fr.readonlymain.gitclient.data.model

data class Branch(
    val name: String,
    val isLocal: Boolean,
    val isRemote: Boolean,
    val localRef: String? = null,  // "refs/heads/main"
    val remoteRef: String? = null  // "refs/remotes/origin/main"
)
