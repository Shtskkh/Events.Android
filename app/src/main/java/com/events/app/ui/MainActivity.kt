package com.events.app.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.events.app.ui.components.AppTopBar
import com.events.app.ui.components.DrawerContent
import com.events.app.ui.navigation.NavigationGraph
import com.events.app.ui.navigation.getTitleForRoute
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
            MaterialTheme {
                val navController = rememberNavController()
                val currentBackStackEntry = navController.currentBackStackEntryAsState().value
                val currentRoute = currentBackStackEntry?.destination?.route ?: "main"

                // Название передаётся в AppTopBar динамически
                val routeTitle = getTitleForRoute(currentRoute)

                // Топ бар скрывается, пока пользователь не потянет вверх
                val scrollBehavior = TopAppBarDefaults
                    .enterAlwaysScrollBehavior(rememberTopAppBarState())

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(
                            drawerState = drawerState,
                            navController = navController,
                            currentRoute = routeTitle,
                        )
                    }
                ) {
                    Scaffold(
                        topBar = { AppTopBar(routeTitle, scrollBehavior, scope, drawerState) },
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                    ) { innerPadding ->
                        NavigationGraph(
                            navController,
                            innerPadding
                        )
                    }
                }
            }
        }
    }
}