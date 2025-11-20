package com.events.app.ui.views.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.app.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isAuthenticated: Flow<Boolean> = authRepository.isAuthenticated

    fun setAuthenticated(isAuth: Boolean) {
        viewModelScope.launch {
            authRepository.setAuthenticated(isAuth)
        }
    }
}