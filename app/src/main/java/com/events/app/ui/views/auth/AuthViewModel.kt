package com.events.app.ui.views.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.domain.models.users.User
import com.events.app.domain.repositories.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _isLoading = mutableStateOf(false)
    private val _error = mutableStateOf<String?>(null)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val fakeUser = User(id = "123", name = "Иван", email = email)
                authRepository.login(fakeUser)
            } catch (e: Exception) {
                _error.value = "Неверный логин или пароль."
            } finally {
                _isLoading.value = false
            }
        }
    }
}