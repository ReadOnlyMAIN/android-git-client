package fr.readonlymain.gitclient.data.preferences

import fr.readonlymain.gitclient.data.model.GitConfig
import kotlinx.coroutines.flow.Flow

interface GitConfigPreferences {
    val gitConfigurationFlow: Flow<GitConfig>

    suspend fun setGitConfig(config: GitConfig)
}