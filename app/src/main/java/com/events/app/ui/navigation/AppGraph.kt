package com.events.app.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.events.app.domain.models.users.User
import com.events.app.ui.components.appbar.AppTopBar
import com.events.app.ui.components.appbar.EventDetailsTopBar
import com.events.app.ui.components.appbar.EventsTopBar
import com.events.app.ui.components.appbar.FiltersTopBar
import com.events.app.ui.components.drawers.AppDrawer
import com.events.app.ui.views.createevent.CreateEventScreen
import com.events.app.ui.views.createevent.CreateEventStep2Screen
import com.events.app.ui.views.createevent.CreateEventViewModel
import com.events.app.ui.views.eventdetail.EventDetailsScreen
import com.events.app.ui.views.events.EventsScreen
import com.events.app.ui.views.eventsfilter.FiltersScreen
import com.events.app.ui.views.main.MainScreen
import com.events.app.ui.views.settings.SettingsScreen
import com.events.app.ui.views.user.AccountScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGraph(
    user: User
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    fun navToAccount() {
        navController.navigate(NavigationRoute.Account)
    }

    fun navToEventDetails(id: String) {
        navController.navigate(NavigationRoute.EventDetails(id))
    }

    fun navToFilters() {
        navController.navigate(NavigationRoute.Filters)
    }

    fun navBack() {
        navController.popBackStack()
    }

    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Main
    ) {
        // Главная страница
        composable<NavigationRoute.Main> {
            AppDrawer(drawerState = drawerState, navController = navController) {
                AppTopBar(
                    title = "Главная",
                    drawerState = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    MainScreen(onEventClick = { id -> navToEventDetails(id.toString()) })
                }
            }
        }

        // Все мероприятия
        composable<NavigationRoute.Events> {
            AppDrawer(drawerState = drawerState, navController = navController) {
                EventsTopBar(
                    drawerState = drawerState,
                    onNavigationToAccount = { navToAccount() },
                    onFiltersClick = { navToFilters() },
                ) {
                    EventsScreen(onEventClick = { id -> navToEventDetails(id.toString()) })
                }
            }
        }

        // Создание мероприятия — вложенный граф, чтобы шаги 1 и 2
        // делили один и тот же CreateEventViewModel
        navigation<NavigationRoute.CreateEventGraph>(
            startDestination = NavigationRoute.CreateEvent
        ) {
            composable<NavigationRoute.CreateEvent> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<NavigationRoute.CreateEventGraph>()
                }
                val viewModel: CreateEventViewModel = hiltViewModel(parentEntry)

                AppDrawer(drawerState = drawerState, navController = navController) {
                    AppTopBar(
                        title = "Создать мероприятие",
                        drawerState = drawerState,
                        onNavigationToAccount = { navToAccount() },
                    ) {
                        CreateEventScreen(
                            viewModel = viewModel,
                            onNext = { navController.navigate(NavigationRoute.CreateEventStep2) }
                        )
                    }
                }
            }

            composable<NavigationRoute.CreateEventStep2> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<NavigationRoute.CreateEventGraph>()
                }
                val viewModel: CreateEventViewModel = hiltViewModel(parentEntry)

                AppDrawer(drawerState = drawerState, navController = navController) {
                    EventDetailsTopBar(
                        title = "Создать мероприятие",
                        onBack = { navBack() },
                        onNavigationToAccount = { navToAccount() }
                    ) {
                        CreateEventStep2Screen(
                            viewModel = viewModel,
                            onSuccess = {
                                navController.popBackStack(
                                    route = NavigationRoute.CreateEventGraph,
                                    inclusive = true
                                )
                            }
                        )
                    }
                }
            }
        }

        // Настройки
        composable<NavigationRoute.Settings> {
            AppDrawer(drawerState = drawerState, navController = navController) {
                AppTopBar(
                    title = "Настройки",
                    drawerState = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    SettingsScreen()
                }
            }
        }

        // Аккаунт пользователя
        composable<NavigationRoute.Account> {
            AppDrawer(drawerState = drawerState, navController = navController) {
                AppTopBar(
                    title = "Аккаунт",
                    drawerState = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    AccountScreen()
                }
            }
        }

        // Детали мероприятия
        composable<NavigationRoute.EventDetails> { backStackEntry ->
            val details: NavigationRoute.EventDetails = backStackEntry.toRoute()
            EventDetailsTopBar(
                onNavigationToAccount = { navToAccount() },
                onBack = { navBack() }
            ) {
                EventDetailsScreen(eventId = details.id)
            }
        }

        // Фильтры
        composable<NavigationRoute.Filters> {
            FiltersTopBar(
                onBack = { navBack() },
                onResetAll = { }
            ) {
                FiltersScreen()
            }
        }
    }
}