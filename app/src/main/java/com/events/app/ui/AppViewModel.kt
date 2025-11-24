package com.events.app.ui

import androidx.lifecycle.ViewModel
import com.events.app.domain.repositories.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel()