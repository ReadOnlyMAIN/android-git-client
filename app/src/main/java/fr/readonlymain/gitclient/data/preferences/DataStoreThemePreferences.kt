package fr.readonlymain.gitclient.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.readonlymain.gitclient.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Manages the persistence and retrieval of user theme preferences using Android DataStore.
 *
 * This class provides a way to store the selected [ThemeMode] and observe changes
 * to the theme setting throughout the application via a [Flow].
 *
 * @property context The application context used to access the DataStore instance.
 */
class DataStoreThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : ThemePreferences {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    /**
     * Retrieves the current theme preference as a [Flow] of [ThemeMode].
     *
     * This function reads the theme setting from the DataStore and maps the stored string value
     * to the corresponding [ThemeMode] enum. If no preference is set, it defaults to [ThemeMode.SYSTEM].
     *
     * @return A [Flow] that emits the current [ThemeMode] whenever the preference is updated.
     */
    override fun getTheme(): Flow<ThemeMode> {
        return context.dataStore.data.map { prefs ->
            when (prefs[THEME_KEY]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
    }

    /**
     * Updates and persists the user's preferred theme mode in the application settings.
     *
     * @param mode The [ThemeMode] to be saved (LIGHT, DARK, or SYSTEM).
     */
    override suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }

}
