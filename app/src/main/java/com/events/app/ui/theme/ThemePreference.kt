package com.events.app.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppTheme { LIGHT, DARK, SYSTEM }

private val Context.themeDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "theme_prefs")

@Singleton
class ThemePreference @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_KEY = stringPreferencesKey("app_theme")

    // По умолчанию SYSTEM — следует за устройством
    val theme: Flow<AppTheme> = context.themeDataStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            AppTheme.DARK.name  -> AppTheme.DARK
            AppTheme.LIGHT.name -> AppTheme.LIGHT
            else                -> AppTheme.SYSTEM   // null тоже = SYSTEM
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.themeDataStore.edit { it[THEME_KEY] = theme.name }
    }
}