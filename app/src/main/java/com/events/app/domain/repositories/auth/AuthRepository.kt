package com.events.app.domain.repositories.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.events.app.domain.models.users.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class AuthRepository @Inject constructor(
    private val context: Context
) {
    private val USER_KEY = stringPreferencesKey("user_json")

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        loadUserFromDataStore()
    }

    private fun loadUserFromDataStore() {
        val cachedUser = runBlocking {
            val prefs = context.dataStore.data.first()
            val userJson = prefs[USER_KEY]

            if (userJson != null) {
                Json.decodeFromString<User>(userJson)
            } else {
                null
            }
        }

        _currentUserFlow.value = cachedUser

        repositoryScope.launch {
            context.dataStore.data.collect { preferences ->
                val json = preferences[USER_KEY]
                val user = if (json != null) {
                    Json.decodeFromString<User>(json)
                } else {
                    null
                }
                _currentUserFlow.value = user
            }
        }
    }

    suspend fun login(user: User) {
        context.dataStore.edit {
            it[USER_KEY] = Json.encodeToString(user)
        }
    }

    suspend fun logout() {
        context.dataStore.edit {
            it.clear()
        }
    }
}