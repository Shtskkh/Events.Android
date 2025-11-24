package com.events.app.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.events.app.domain.models.users.User
import com.events.app.ui.components.drawers.AppDrawer
import com.events.app.ui.views.events.EventsScreen
import com.events.app.ui.views.main.MainScreen
import com.events.app.ui.views.main.MainViewModel
import com.events.app.ui.views.settings.SettingsScreen
import com.events.app.ui.views.user.AccountScreen

/*
* Навигация приложения.
*/
@Composable
fun AppGraph(
    user: User
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    rememberCoroutineScope()

    AppDrawer(
        drawerState = drawerState,
        navController = navController,
    ) {
        NavHost(
            navController = navController,
            startDestination = NavigationRoute.Main
        ) {
            // Главная страница
            composable<NavigationRoute.Main> {
                val viewModel = hiltViewModel<MainViewModel>()
                MainScreen(viewModel)
            }

            // Все мероприятия
            composable<NavigationRoute.Events> {
                EventsScreen()
            }

            // Настройки
            composable<NavigationRoute.Settings> {
                SettingsScreen()
            }

            // Аккаунт пользователя
            composable<NavigationRoute.Account> {
                AccountScreen()
            }
        }
    }
}

// Функция получения названия экрана по его пути
@Composable
fun getTitleForRoute(currentRoute: NavigationRoute?): String {
    return when (currentRoute) {
        is NavigationRoute.Main -> "Главная"
        is NavigationRoute.Events -> "Мероприятия"
        is NavigationRoute.Settings -> "Настройки"
        is NavigationRoute.Account -> "Аккаунт"
        else -> "Приложение"
    }
}