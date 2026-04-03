package com.events.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.RemoteDataSource
import com.events.app.domain.repositories.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    val authRepository: AuthRepository,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    // ── Смена пароля ──────────────────────────────────────────────

    private val _passwordChangeLoading = MutableStateFlow(false)
    val passwordChangeLoading = _passwordChangeLoading.asStateFlow()

    private val _passwordChangeError = MutableStateFlow<String?>(null)
    val passwordChangeError = _passwordChangeError.asStateFlow()

    private val _passwordChangeSuccess = MutableStateFlow(false)
    val passwordChangeSuccess = _passwordChangeSuccess.asStateFlow()


    fun changePassword(oldPassword: String, newPassword: String) {
        val userId = authRepository.currentUser.value?.id ?: return
        viewModelScope.launch {
            _passwordChangeLoading.value = true
            _passwordChangeError.value   = null
            try {
                remoteDataSource.changePassword(userId, oldPassword, newPassword)
                _passwordChangeSuccess.value = true
            } catch (e: retrofit2.HttpException) {
                _passwordChangeError.value = when (e.code()) {
                    400  -> "Неверный текущий пароль"
                    404  -> "Пользователь не найден"
                    else -> "Ошибка сервера: ${e.code()}"
                }
            } catch (e: Exception) {
                _passwordChangeError.value = "Нет соединения с сервером"
            } finally {
                _passwordChangeLoading.value = false
            }
        }
    }

    fun clearPasswordChangeState() {
        _passwordChangeError.value   = null
        _passwordChangeSuccess.value = false
    }
}