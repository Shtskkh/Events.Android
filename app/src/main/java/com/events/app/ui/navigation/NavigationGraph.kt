package com.events.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.events.app.ui.views.events.EventsScreen
import com.events.app.ui.views.main.MainScreen
import com.events.app.ui.views.main.MainViewModel
import com.events.app.ui.views.settings.SettingsScreen

// Функция, позволяющая динамически переходить между
// экранами приложения
@Composable
fun NavigationGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Main.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        // Главная страница
        composable(NavigationRoute.Main.route) {
            val viewModel = hiltViewModel<MainViewModel>()
            MainScreen(viewModel)
        }

        // Все мероприятия
        composable(NavigationRoute.Events.route) {
            EventsScreen()
        }

        // Настройки
        composable(NavigationRoute.Settings.route) {
            SettingsScreen()
        }
    }
}