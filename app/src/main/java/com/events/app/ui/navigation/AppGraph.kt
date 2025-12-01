package com.events.app.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.events.app.domain.models.users.User
import com.events.app.ui.components.appbar.AppTopBar
import com.events.app.ui.components.appbar.EventDetailsTopBar
import com.events.app.ui.components.drawers.AppDrawer
import com.events.app.ui.views.events.EventDetailsScreen
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

    fun navToEventDetails(id: Int) {
        navController.navigate(NavigationRoute.EventDetails(id).route)
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
                    onNavigationToAccount = { navToAccount() },
                    onEventClick = { id -> navToEventDetails(id) }
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

            // Детали мероприятия
            composable(
                route = NavigationRoute.EventDetails.routeTemplate,
                arguments = listOf(navArgument(NavigationRoute.EventDetails.ARG_ID) { type = NavType.IntType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getInt(NavigationRoute.EventDetails.ARG_ID) ?: 0
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                EventDetailsTopBar(
                    title = "Детали мероприятия",
                    scrollBehavior = scrollBehavior,
                    drawerState = drawerState,
                    onNavigationToAccount = { navToAccount() },
                    onBack = { navController.popBackStack() }  // Кнопка "Назад" возвращает к списку
                ) {
                    EventDetailsScreen(eventId = eventId)
                }
            }
        }
    }
}