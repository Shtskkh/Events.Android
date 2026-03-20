package com.events.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreference: ThemePreference
) : ViewModel() {

    // Eagerly — чтобы тема была доступна сразу при старте без мерцания
    val theme = themePreference.theme.stateIn(
        scope          = viewModelScope,
        started        = SharingStarted.Eagerly,
        initialValue   = AppTheme.SYSTEM   // дефолт = системная
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { themePreference.setTheme(theme) }
    }
}