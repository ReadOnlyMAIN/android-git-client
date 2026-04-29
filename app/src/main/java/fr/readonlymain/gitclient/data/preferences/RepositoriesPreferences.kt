package fr.readonlymain.gitclient.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.readonlymain.gitclient.data.model.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Manages the persistent storage and retrieval of [Repository] objects using Android DataStore.
 *
 * This class handles the serialization of the repository list into a JSON string for storage
 * and provides a reactive [Flow] for observing changes. It allows for asynchronous
 * operations to add or remove repositories from the local preferences.
 *
 * @property context The application context used to access the DataStore instance.
 */
class RepositoriesPreferences(private val context: Context) {
    companion object {
        private val REPOSITORIES_KEY = stringPreferencesKey("repositories_json")
        private val SELECTED_REPO_KEY = stringPreferencesKey("selected_repo")
        private val SELECTED_BRANCH_KEY = stringPreferencesKey("selected_branch")
    }

    /**
     * A [Flow] that emits the list of saved [Repository] objects from the DataStore.
     *
     * The data is stored as a JSON string and decoded into a list. If no repositories
     * are found or if the JSON decoding fails, an empty list is returned.
     */
    val repositoriesFlow: Flow<List<Repository>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[REPOSITORIES_KEY] ?: return@map emptyList()
            try {
                Json.decodeFromString<List<Repository>>(json)
            } catch (_: Exception) {
                emptyList()
            }
        }

    /**
     * Adds a new repository to the persistent storage.
     *
     * This function retrieves the current list of repositories from the DataStore,
     * appends the new [repository] to the collection, and saves the serialized
     * updated list back to preferences.
     *
     * @param repository The [Repository] object to be added.
     */
    suspend fun addRepository(repository: Repository) {
        context.dataStore.edit { preferences ->
            val currentList = repositoriesFlow.first()
            val newList = currentList + repository
            preferences[REPOSITORIES_KEY] = Json.encodeToString(newList)
        }
    }

    /**
     * Updates an existing repository in the persistent storage.
     *
     * @param repository The [Repository] object with updated information.
     */
    suspend fun updateRepository(repository: Repository) {
        context.dataStore.edit { preferences ->
            val currentList = repositoriesFlow.first()
            val newList = currentList.map {
                if (it.id == repository.id) repository else it
            }
            preferences[REPOSITORIES_KEY] = Json.encodeToString(newList)
        }
    }

    /**
     * Deletes a repository from the persistent storage by its unique identifier.
     *
     * This function retrieves the current list of repositories from the DataStore,
     * filters out the repository that matches the provided [id], and saves the
     * updated list back to preferences.
     *
     * @param id The unique identifier of the [Repository] to be removed.
     */
    suspend fun deleteRepository(id: String) {
        context.dataStore.edit { preferences ->
            val currentList = repositoriesFlow.first()
            val newList = currentList.filter { it.id != id }
            preferences[REPOSITORIES_KEY] = Json.encodeToString(newList)
        }
    }

    val selectedRepoFlow: Flow<String?> = context.dataStore.data.map { it[SELECTED_REPO_KEY] }
    val selectedBranchFlow: Flow<String?> = context.dataStore.data.map { it[SELECTED_BRANCH_KEY] }

    suspend fun saveSelectedRepo(path: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_REPO_KEY] = path
        }
    }

    suspend fun saveSelectedBranch(branchName: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_BRANCH_KEY] = branchName
        }
    }

    suspend fun resetSelectedBranch() {
        context.dataStore.edit { preferences ->
            preferences.remove(SELECTED_BRANCH_KEY)
        }
    }
}