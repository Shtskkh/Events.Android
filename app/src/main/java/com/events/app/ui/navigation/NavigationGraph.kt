package com.events.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.events.app.ui.views.auth.AuthViewModel
import com.events.app.ui.views.auth.LoginScreen
import com.events.app.ui.views.events.EventsScreen
import com.events.app.ui.views.main.MainScreen
import com.events.app.ui.views.main.MainViewModel
import com.events.app.ui.views.settings.SettingsScreen
import com.events.app.ui.views.user.AccountScreen

// Функция, позволяющая динамически переходить между
// экранами приложения
@Composable
fun NavigationGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(NavItem.Login.route) {
            val viewModel = hiltViewModel<AuthViewModel>()
            LoginScreen(navController, viewModel)
        }

        // Главная страница
        composable(NavItem.Main.route) {
            val viewModel = hiltViewModel<MainViewModel>()
            MainScreen(viewModel)
        }

        // Все мероприятия
        composable(NavItem.Events.route) {
            EventsScreen()
        }

        // Настройки
        composable(NavItem.Settings.route) {
            SettingsScreen()
        }

        // Аккаунт пользователя
        composable(NavItem.Account.route) {
            AccountScreen()
        }
    }
}