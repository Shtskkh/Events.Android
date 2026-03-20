package com.events.app.ui.views.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.events.app.ui.theme.AppTheme
import com.events.app.ui.theme.ThemeViewModel

@Composable
fun SettingsScreen(themeViewModel: ThemeViewModel = hiltViewModel()) {
    val currentTheme by themeViewModel.theme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)   // ← фикс
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text("Внешний вид", fontSize = 13.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp))

        Spacer(Modifier.height(8.dp))

        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = when (currentTheme) {
                                AppTheme.DARK   -> Icons.Outlined.DarkMode
                                AppTheme.LIGHT  -> Icons.Outlined.LightMode
                                AppTheme.SYSTEM -> Icons.Outlined.SettingsBrightness
                            }, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        }
                    }
                    Column {
                        Text("Тема приложения", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text(when (currentTheme) {
                            AppTheme.LIGHT  -> "Светлая"
                            AppTheme.DARK   -> "Тёмная"
                            AppTheme.SYSTEM -> "Как в системе"
                        }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeOptionButton(Icons.Outlined.LightMode, "Светлая",
                        currentTheme == AppTheme.LIGHT, { themeViewModel.setTheme(AppTheme.LIGHT) }, Modifier.weight(1f))
                    ThemeOptionButton(Icons.Outlined.DarkMode, "Тёмная",
                        currentTheme == AppTheme.DARK, { themeViewModel.setTheme(AppTheme.DARK) }, Modifier.weight(1f))
                    ThemeOptionButton(Icons.Outlined.SettingsBrightness, "Авто",
                        currentTheme == AppTheme.SYSTEM, { themeViewModel.setTheme(AppTheme.SYSTEM) }, Modifier.weight(1f))
                }

                if (currentTheme == AppTheme.SYSTEM) {
                    Spacer(Modifier.height(10.dp))
                    Text("Тема меняется вместе с настройками устройства",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 2.dp))
                }
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ThemeOptionButton(icon: ImageVector, label: String, selected: Boolean,
                              onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (selected) 0.dp else 1.dp) {
        Column(Modifier.padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp))
            Text(label, fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}