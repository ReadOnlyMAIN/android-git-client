package fr.readonlymain.gitclient.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class GitCredential(
    val id: String = UUID.randomUUID().toString(),
    val accountName: String,
    val username: String,
    val token: String,
    val providerUrl: String = "https://github.com"
)