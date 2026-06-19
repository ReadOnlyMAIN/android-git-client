package fr.readonlymain.gitclient.data.preferences

import fr.readonlymain.gitclient.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeThemePreferences : ThemePreferences {
    private val _theme = MutableStateFlow(ThemeMode.SYSTEM)

    override fun getTheme(): Flow<ThemeMode> = _theme.asStateFlow()

    override suspend fun setTheme(mode: ThemeMode) {
        _theme.value = mode
    }
}
