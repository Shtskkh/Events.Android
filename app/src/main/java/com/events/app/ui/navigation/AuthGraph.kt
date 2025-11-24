package com.events.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.events.app.ui.views.auth.AuthViewModel
import com.events.app.ui.views.auth.LoginScreen

/**/
@Composable
fun AuthGraph() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = NavigationRoute.Login) {
        composable<NavigationRoute.Login> {
            val authViewModel = hiltViewModel<AuthViewModel>()
            LoginScreen(
                viewModel = authViewModel
            )
        }
    }
}