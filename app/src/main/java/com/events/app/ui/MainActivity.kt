package com.events.app.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.events.app.ui.components.AppTopBar
import com.events.app.ui.components.DrawerContent
import com.events.app.ui.navigation.NavigationGraph
import com.events.app.ui.navigation.NavItem
import com.events.app.ui.navigation.getTitleForRoute
import com.events.app.ui.theme.EventsTheme
import com.events.app.ui.views.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Чтобы иконки в статус-баре не были белыми
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true

        setContent {
            EventsTheme {
                val authViewModel = hiltViewModel<AuthViewModel>()
                val isAuthenticated by authViewModel.isAuthenticated.collectAsState(initial = false)

                val navController = rememberNavController()
                val currentBackStackEntry = navController.currentBackStackEntryAsState().value
                val currentRoute = currentBackStackEntry?.destination?.route ?: "login"

                val startDestination = if (isAuthenticated) {
                    NavItem.Main.route
                } else {
                    NavItem.Login.route
                }

                if (currentRoute == NavItem.Login.route) {
                    NavigationGraph(
                        navController = navController,
                        innerPadding = PaddingValues(0.dp),
                        startDestination = startDestination
                    )
                } else {
                    val routeTitle = getTitleForRoute(currentRoute)
                    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerContent(
                                drawerState = drawerState,
                                navController = navController,
                                currentRoute = currentRoute,
                            )
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                AppTopBar(
                                    routeTitle, scrollBehavior, drawerState,
                                    onNavigationToAccount = {
                                        navController.navigate(NavItem.Account.route)
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(scrollBehavior.nestedScrollConnection),
                        ) { innerPadding ->
                            NavigationGraph(
                                navController = navController,
                                innerPadding = innerPadding,
                                startDestination = startDestination
                            )
                        }
                    }
                }
            }
        }
    }
}