package fr.readonlymain.gitclient.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Repository(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val remoteUrl: String = "",
    val localPath: String = "",
    val defaultBranch: String = "main",
    val username: String? = null,
    val lastSyncTimestamp: Long? = null
)