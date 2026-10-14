package fr.readonlymain.gitclient.data.preferences

import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeCredentialsPreferences : CredentialsPreferences {
    private val _credentials = MutableStateFlow<List<GitCredential>>(emptyList())
    override val credentialsFlow: Flow<List<GitCredential>> = _credentials.asStateFlow()

    override suspend fun addCredential(credential: GitCredential) {
        _credentials.update { it + credential }
    }

    override suspend fun deleteCredential(id: String) {
        _credentials.update { list -> list.filter { it.id != id } }
    }
}
