package fr.readonlymain.gitclient.data.model

data class CloneResult(
    val message: String,
    val success: Boolean,
    val folderPath: String? = null,
    val username: String? = null
)
