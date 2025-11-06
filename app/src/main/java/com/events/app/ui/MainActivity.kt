package com.events.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.events.app.ui.viewmodels.EventsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Убрали EventsAppTheme, чтобы избежать ошибок
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background  // Оставьте, если MaterialTheme импортирован, или удалите color для простоты
            ) {
                EventsScreen()  // Вывод главной страницы с мероприятиями
            }
        }
    }
}