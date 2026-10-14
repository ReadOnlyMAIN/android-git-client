package fr.readonlymain.gitclient.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreCredentialsPreferences @Inject constructor(
    @ApplicationContext context: Context,
    cipher: AndroidKeystoreCredentialCipher
) : CredentialsPreferences {
    private val persistence = CredentialPersistence(context.dataStore, cipher::encrypt, cipher::decrypt)
    override val credentialsFlow = persistence.credentials.flowOn(Dispatchers.IO)

    override suspend fun addCredential(credential: GitCredential) = withContext(Dispatchers.IO) {
        persistence.update { it + credential }
    }

    override suspend fun deleteCredential(id: String) = withContext(Dispatchers.IO) {
        persistence.update { list -> list.filter { it.id != id } }
    }
}
