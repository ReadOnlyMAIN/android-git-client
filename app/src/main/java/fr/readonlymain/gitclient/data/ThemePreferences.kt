package fr.readonlymain.gitclient.data

import android.content.Context
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
        private val OLED_KEY = booleanPreferencesKey("oled_mode")
    }

    fun getTheme(): Flow<ThemeMode> {
        return context.dataStore.data.map { prefs ->
            when (prefs[THEME_KEY]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }

    fun getOledMode(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[OLED_KEY] ?: false
        }
    }

    suspend fun setOledMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[OLED_KEY] = enabled
        }
    }

}
