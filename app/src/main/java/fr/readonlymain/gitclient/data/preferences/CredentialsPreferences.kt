package fr.readonlymain.gitclient.data.preferences

import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.flow.Flow

interface CredentialsPreferences {
    val credentialsFlow: Flow<List<GitCredential>>

    suspend fun addCredential(credential: GitCredential)
    suspend fun deleteCredential(id: String)
}