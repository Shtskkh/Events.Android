package com.events.app.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.BuildConfig
import com.events.app.data.RemoteDataSource
import com.events.app.data.remote.dto.UserDetailDto
import com.events.app.domain.repositories.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    val authRepository: AuthRepository,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    // ── Детали текущего пользователя (ФИО + аватар) ─────────────
    private val _userDetail = MutableStateFlow<UserDetailDto?>(null)
    val userDetail = _userDetail.asStateFlow()

    /**
     * Итоговый URI/URL аватара: сначала локальный файл (если есть и существует),
     * иначе серверный URL из avatarInfo bucket/key.
     */
    val avatarUri = combine(
        authRepository.localAvatarPath,
        _userDetail
    ) { localPath, detail ->
        // Локальный файл — приоритет
        if (!localPath.isNullOrBlank()) {
            val f = File(localPath)
            if (f.exists()) return@combine Uri.fromFile(f)
        }
        // Серверный аватар из avatarInfo
        val ai = detail?.avatarInfo
        if (ai?.bucket != null && ai.key != null &&
            ai.bucket.isNotBlank() && ai.key.isNotBlank()) {
            val base = BuildConfig.BASE_URL.trimEnd('/')
            return@combine Uri.parse("$base/api/v/1/files/${ai.bucket}/${ai.key}")
        }
        null
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                val id = user?.id
                if (!id.isNullOrBlank()) {
                    try {
                        _userDetail.value = remoteDataSource.getUserById(id)
                    } catch (_: Exception) {
                        _userDetail.value = null
                    }
                } else {
                    _userDetail.value = null
                }
            }
        }
    }

    fun saveAvatar(uri: Uri) {
        viewModelScope.launch {
            authRepository.saveLocalAvatar(uri)
        }
    }

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