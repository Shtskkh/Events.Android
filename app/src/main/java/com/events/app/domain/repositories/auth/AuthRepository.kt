package com.events.app.domain.repositories.auth

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.events.app.domain.models.users.User
import com.events.app.domain.models.users.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class AuthRepository @Inject constructor(
    private val context: Context
) {
    private val USER_KEY = stringPreferencesKey("user_json")
    private val httpClient = OkHttpClient()
    private val BASE_URL = "http://10.0.2.2:8080"

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
                try { Json.decodeFromString<User>(userJson) } catch (e: Exception) { null }
            } else null
        }
        _currentUserFlow.value = cachedUser

        repositoryScope.launch {
            context.dataStore.data.collect { preferences ->
                val json = preferences[USER_KEY]
                _currentUserFlow.value = if (json != null) {
                    try { Json.decodeFromString<User>(json) } catch (e: Exception) { null }
                } else null
            }
        }
    }

    suspend fun loginWithCredentials(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("Email", email)
                    .addFormDataPart("Password", password)
                    .build()

                val request = Request.Builder()
                    .url("$BASE_URL/api/v/1/users/login")
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Неверный логин или пароль"))
                }

                val json = JSONObject(responseBody)
                val accessToken = json.optString("accessToken", "")

                if (accessToken.isBlank()) {
                    return@withContext Result.failure(Exception("Токен не получен"))
                }

                // Декодируем JWT payload
                val payload = decodeJwtPayload(accessToken)
                val userId = payload?.optString("sub", "") ?: ""
                val roleStr = payload?.optString("role", "") ?: ""

                val role = when (roleStr) {
                    "Администратор" -> UserRole.ADMIN
                    "Пользователь"  -> UserRole.USER
                    else            -> UserRole.GUEST
                }

                val user = User(
                    id = userId,
                    name = email,
                    email = email,
                    role = role,
                    accessToken = accessToken
                )

                // Сохраняем в DataStore
                context.dataStore.edit {
                    it[USER_KEY] = Json.encodeToString(User.serializer(), user)
                }

                Result.success(user)

            } catch (e: Exception) {
                Result.failure(Exception("Ошибка подключения: ${e.message}"))
            }
        }
    }

    private fun decodeJwtPayload(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val decoded = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING),
                Charsets.UTF_8
            )
            JSONObject(decoded)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun login(user: User) {
        context.dataStore.edit {
            it[USER_KEY] = Json.encodeToString(User.serializer(), user)
        }
    }

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }
}