package fr.readonlymain.gitclient.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.readonlymain.gitclient.data.model.GitConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class GitConfigPreferences(private val context: Context) {
    companion object {
        private val GIT_CONFIG_KEY = stringPreferencesKey("git_configuration")
    }

    val gitConfigurationFlow: Flow<GitConfig> = context.dataStore.data
        .map { preferences ->
            val json = preferences[GIT_CONFIG_KEY] ?: return@map GitConfig()
            try {
                Json.decodeFromString<GitConfig>(json)
            } catch (_: Exception) {
                GitConfig()
            }
        }

    suspend fun setGitConfig(config: GitConfig) {
        context.dataStore.edit { preferences ->
            preferences[GIT_CONFIG_KEY] = Json.encodeToString(config)
        }
    }
}