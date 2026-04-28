package fr.readonlymain.gitclient.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class GitConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val email: String = "",
)