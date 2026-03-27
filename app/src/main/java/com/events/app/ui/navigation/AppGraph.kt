package com.events.app.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.events.app.domain.models.users.User
import com.events.app.domain.models.users.UserRole
import com.events.app.ui.AppViewModel
import com.events.app.ui.components.appbar.AppTopBar
import com.events.app.ui.components.appbar.EventDetailsTopBar
import com.events.app.ui.components.appbar.EventsTopBar
import com.events.app.ui.components.appbar.FiltersTopBar
import com.events.app.ui.components.drawers.AppDrawer
import com.events.app.ui.views.admin.AdminScreen
import com.events.app.ui.views.createevent.CreateEventScreen
import com.events.app.ui.views.createevent.CreateEventStep2Screen
import com.events.app.ui.views.createevent.CreateEventViewModel
import com.events.app.ui.views.editevent.EditEventScreen
import com.events.app.ui.views.eventdetail.EventDetailsScreen
import com.events.app.ui.views.eventdetail.EventDetailsViewModel
import com.events.app.ui.views.events.EventsScreen
import com.events.app.ui.views.eventsfilter.FiltersScreen
import com.events.app.ui.views.locations.LocationsScreen
import com.events.app.ui.views.main.MainScreen
import com.events.app.ui.views.settings.SettingsScreen
import com.events.app.ui.views.statistics.StatisticsScreen
import com.events.app.ui.views.user.AccountScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGraph(
    user: User
) {
    val navController = rememberNavController()
    val drawerState   = rememberDrawerState(initialValue = DrawerValue.Closed)

    val isAdmin = user.role == UserRole.ADMIN

    fun navToAccount()                = navController.navigate(NavigationRoute.Account)
    fun navToEventDetails(id: String) = navController.navigate(NavigationRoute.EventDetails(id))
    fun navToFilters()                = navController.navigate(NavigationRoute.Filters)
    fun navToEditEvent(id: String)    = navController.navigate(NavigationRoute.EditEvent(id))
    fun navBack()                     = navController.popBackStack()

    NavHost(
        navController    = navController,
        startDestination = NavigationRoute.Main
    ) {
        // ── Главная ───────────────────────────────────────────────
        composable<NavigationRoute.Main> {
            AppDrawer(drawerState = drawerState, navController = navController) {
                AppTopBar(
                    title                 = "Главная",
                    drawerState           = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    MainScreen(onEventClick = { id -> navToEventDetails(id) })
                }
            }
        }

        // ── Все мероприятия ───────────────────────────────────────
        composable<NavigationRoute.Events> {
            AppDrawer(drawerState = drawerState, navController = navController) {
                EventsTopBar(
                    drawerState           = drawerState,
                    onNavigationToAccount = { navToAccount() },
                    onFiltersClick        = { navToFilters() },
                ) {
                    EventsScreen(onEventClick = { id -> navToEventDetails(id) })
                }
            }
        }

        // ── Создание мероприятия ──────────────────────────────────
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
                        title                 = "Создать мероприятие",
                        drawerState           = drawerState,
                        onNavigationToAccount = { navToAccount() },
                    ) {
                        CreateEventScreen(
                            viewModel = viewModel,
                            onNext    = { navController.navigate(NavigationRoute.CreateEventStep2) }
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
                        title                 = "Создать мероприятие",
                        onBack                = { navBack() },
                        onNavigationToAccount = { navToAccount() }
                    ) {
                        CreateEventStep2Screen(
                            viewModel = viewModel,
                            onSuccess = {
                                navController.popBackStack(
                                    route     = NavigationRoute.CreateEventGraph,
                                    inclusive = true
                                )
                            }
                        )
                    }
                }
            }
        }

        // ── Статистика ────────────────────────────────────────────
        composable<NavigationRoute.Statistics> {
            AppDrawer(drawerState = drawerState, navController = navController) {
                AppTopBar(
                    title                 = "Статистика",
                    drawerState           = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    StatisticsScreen()
                }
            }
        }

        // ── Локации ───────────────────────────────────────────────
        composable<NavigationRoute.Locations> {
            AppDrawer(drawerState = drawerState, navController = navController) {
                AppTopBar(
                    title                 = "Локации",
                    drawerState           = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    LocationsScreen()
                }
            }
        }

        // ── Настройки ─────────────────────────────────────────────
        composable<NavigationRoute.Settings> {
            AppDrawer(drawerState = drawerState, navController = navController) {
                AppTopBar(
                    title                 = "Настройки",
                    drawerState           = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    SettingsScreen()
                }
            }
        }

        // ── Аккаунт ───────────────────────────────────────────────
        composable<NavigationRoute.Account> {
            AppDrawer(drawerState = drawerState, navController = navController) {
                AppTopBar(
                    title                 = "Аккаунт",
                    drawerState           = drawerState,
                    onNavigationToAccount = { navToAccount() },
                ) {
                    AccountScreen()
                }
            }
        }

        // ── Детали мероприятия ────────────────────────────────────
        composable<NavigationRoute.EventDetails> { backStackEntry ->
            val details: NavigationRoute.EventDetails = backStackEntry.toRoute()
            EventDetailsTopBar(
                onNavigationToAccount = { navToAccount() },
                onBack                = { navBack() }
            ) {
                EventDetailsScreen(
                    eventId = details.id,
                    onBack  = { navBack() },
                    onEdit  = if (isAdmin) { { id -> navToEditEvent(id) } } else null
                )
            }
        }

        // ── Редактирование мероприятия (только ADMIN) ─────────────
        if (isAdmin) {
            composable<NavigationRoute.EditEvent> { backStackEntry ->
                val route: NavigationRoute.EditEvent = backStackEntry.toRoute()

                val detailsEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<NavigationRoute.EventDetails>()
                }
                val detailsViewModel: EventDetailsViewModel = hiltViewModel(detailsEntry)
                val event by detailsViewModel.event.collectAsState()

                EventDetailsTopBar(
                    title                 = "Редактировать",
                    onBack                = { navBack() },
                    onNavigationToAccount = { navToAccount() }
                ) {
                    event?.let {
                        EditEventScreen(
                            event     = it,
                            onSuccess = { navBack() },
                            onBack    = { navBack() }
                        )
                    }
                }
            }
        }

        // ── Фильтры ───────────────────────────────────────────────
        composable<NavigationRoute.Filters> {
            FiltersTopBar(
                onBack     = { navBack() },
                onResetAll = { }
            ) {
                FiltersScreen()
            }
        }

        // ── Администрирование (только ADMIN) ──────────────────────
        if (isAdmin) {
            composable<NavigationRoute.Admin> {
                AppDrawer(drawerState = drawerState, navController = navController) {
                    AppTopBar(
                        title                 = "Администрирование",
                        drawerState           = drawerState,
                        onNavigationToAccount = { navToAccount() },
                    ) {
                        AdminScreen(
                            onEventClick = { id -> navToEventDetails(id) }
                        )
                    }
                }
            }
        }
    }
}