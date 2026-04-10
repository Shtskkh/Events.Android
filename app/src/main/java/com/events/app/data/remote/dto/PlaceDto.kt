package com.events.app.data.remote.dto

import com.events.app.BuildConfig
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDto(
    val id: Int,
    val number: String?,
    val capacity: Int,
    val type: String?,
    val title: String?,
    // Старый формат (список помещений)
    val preview: PlacePreviewDto? = null,
    // Новый формат (детальный запрос)
    val photosBucket: String? = null,
    val photos: List<PlacePhotoDto>? = null
) {
    fun buildPreviewUrl(): String? {
        val base = BuildConfig.BASE_URL.trimEnd('/')

        // Новый формат
        if (!photosBucket.isNullOrBlank() && !photos.isNullOrEmpty()) {
            val first = photos.firstOrNull { !it.filename.isNullOrBlank() }
            if (first != null) return "$base/api/v/1/files/$photosBucket/${first.filename}"
        }

        // Старый формат
        val bucket = preview?.bucket?.takeIf { it.isNotBlank() } ?: return null
        val key    = preview.key?.takeIf { it.isNotBlank() } ?: return null
        return "$base/api/v/1/files/$bucket/$key"
    }
}

@Serializable
data class PlacePreviewDto(
    val bucket: String?,
    val key: String?
)

@Serializable
data class PlacePhotoDto(
    val filename: String?,
    val order: Int
)