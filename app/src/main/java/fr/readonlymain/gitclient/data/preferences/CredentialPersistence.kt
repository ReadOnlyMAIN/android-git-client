package fr.readonlymain.gitclient.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/** Read/decrypt/transform/encrypt/write runs inside one serialized DataStore transaction. */
internal class CredentialPersistence(
    private val store: DataStore<Preferences>,
    private val encrypt: (String) -> String,
    private val decrypt: (String) -> String
) {
    private val key = stringPreferencesKey("git_credentials_encrypted")
    val credentials = store.data.map { decode(it[key]) }

    suspend fun update(transform: (List<GitCredential>) -> List<GitCredential>) {
        store.edit { preferences ->
            val updated = transform(decode(preferences[key]))
            val payload = encrypt(Json.encodeToString(updated))
            check(payload.isNotEmpty()) { "Encryption returned an empty payload" }
            preferences[key] = payload
        }
    }

    private fun decode(payload: String?): List<GitCredential> {
        if (payload == null) return emptyList()
        return try {
            Json.decodeFromString<List<GitCredential>>(decrypt(payload))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            throw CredentialStorageException("Cannot read saved credentials", e)
        }
    }
}
