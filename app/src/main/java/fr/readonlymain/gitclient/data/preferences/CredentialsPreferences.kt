package fr.readonlymain.gitclient.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manages the persistent storage and retrieval of [GitCredential] objects using Android DataStore.
 *
 * This class handles the serialization and deserialization of a list of credentials to and from
 * a JSON string stored in the "settings" preferences.
 *
 * @property context The application context used to access the DataStore instance.
 */
class CredentialsPreferences(private val context: Context) {

    companion object {
        private val CREDENTIALS_KEY = stringPreferencesKey("git_credentials_json")
    }

    /**
     * A [Flow] that emits the current list of [GitCredential] stored in the preferences.
     *
     * This stream maps the raw JSON string from DataStore into a list of objects.
     * If no credentials are found or an error occurs during deserialization, it emits an empty list.
     */
    val credentialsFlow: Flow<List<GitCredential>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[CREDENTIALS_KEY] ?: return@map emptyList()
            try {
                Json.decodeFromString<List<GitCredential>>(json)
            } catch (_: Exception) {
                emptyList()
            }
        }

    /**
     * Adds a new [GitCredential] to the persistent storage.
     *
     * This function retrieves the current list of credentials, appends the new [credential],
     * and saves the updated list as a serialized JSON string in the DataStore.
     *
     * @param credential The [GitCredential] to be added to the storage.
     */
    suspend fun addCredential(credential: GitCredential) {
        context.dataStore.edit { preferences ->
            val currentList = credentialsFlow.first()
            val newList = currentList + credential
            preferences[CREDENTIALS_KEY] = Json.encodeToString(newList)
        }
    }

    /**
     * Deletes a specific credential from the persistent storage.
     *
     * This function retrieves the current list of credentials, filters out the entry
     * matching the provided [id], and saves the updated list back to the DataStore.
     *
     * @param id The unique identifier of the [GitCredential] to be removed.
     */
    suspend fun deleteCredential(id: String) {
        context.dataStore.edit { preferences ->
            val currentList = credentialsFlow.first()
            val newList = currentList.filter { it.id != id }
            preferences[CREDENTIALS_KEY] = Json.encodeToString(newList)
        }
    }
}