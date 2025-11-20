package com.events.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val IS_AUTH_KEY = booleanPreferencesKey("is_user_auth")
    }

    val isAuthenticated: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[IS_AUTH_KEY] ?: false
    }

    suspend fun setAuthenticated(isAuth: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_AUTH_KEY] = isAuth
        }
    }
}