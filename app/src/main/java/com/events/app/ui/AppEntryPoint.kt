package com.events.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.events.app.ui.navigation.AppGraph
import com.events.app.ui.navigation.AuthGraph

@Composable
fun AppEntryPoint(
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val user by appViewModel.authRepository.currentUser.collectAsState()

    if (user == null) {
        AuthGraph()
    } else {
        AppGraph(user!!)
    }
}