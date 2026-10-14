package fr.readonlymain.gitclient.data.preferences

import fr.readonlymain.gitclient.data.model.GitConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeGitConfigPreferences : GitConfigPreferences {
    private val _config = MutableStateFlow(GitConfig())
    override val gitConfigurationFlow: Flow<GitConfig> = _config.asStateFlow()

    override suspend fun setGitConfig(config: GitConfig) {
        _config.value = config
    }
}
