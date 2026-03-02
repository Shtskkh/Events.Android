package com.events.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

@Composable
fun EventImage(
    url: String?,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp
) {
    if (url.isNullOrEmpty()) {
        // Серый квадрат если нет картинки
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = "Превью мероприятия",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            },
            error = {
                // Логируем ошибку
                android.util.Log.e("EventImage", "Failed to load: $url, error: ${it.result.throwable}")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        )
    }
}