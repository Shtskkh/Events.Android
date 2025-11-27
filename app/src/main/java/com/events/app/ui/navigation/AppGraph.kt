package com.events.app.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.events.app.domain.models.users.User
import com.events.app.ui.components.appbar.AppTopBar
import com.events.app.ui.components.drawers.AppDrawer
import com.events.app.ui.views.events.EventsScreen
import com.events.app.ui.views.main.MainScreen
import com.events.app.ui.views.main.MainViewModel
import com.events.app.ui.views.settings.SettingsScreen
import com.events.app.ui.views.settings.SettingsViewModel
import com.events.app.ui.views.user.AccountScreen

/*
* Навигация приложения.
*/
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun AppGraph(
    user: User
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    fun navToAccount() {
        navController.navigate(NavigationRoute.Account)
    }

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
                AppTopBar(
                    title = "Главная",
                    drawerState = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    MainScreen(viewModel)
                }
            }

            // Все мероприятия
            composable<NavigationRoute.Events> {
                EventsScreen(
                    drawerState = drawerState,
                    onNavigationToAccount = { navToAccount() }
                )
            }

            // Настройки
            composable<NavigationRoute.Settings> {
                val viewModel = hiltViewModel<SettingsViewModel>()
                AppTopBar(
                    title = "Настройки",
                    drawerState = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    SettingsScreen(viewModel)
                }
            }

            // Аккаунт пользователя
            composable<NavigationRoute.Account> {
                AppTopBar(
                    title = "Аккаунт",
                    drawerState = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    AccountScreen()
                }
            }
        }
    }
}