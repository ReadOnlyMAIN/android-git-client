package fr.readonlymain.gitclient.data.preferences

import fr.readonlymain.gitclient.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemePreferences {
    fun getTheme(): Flow<ThemeMode>
    suspend fun setTheme(mode: ThemeMode)
}