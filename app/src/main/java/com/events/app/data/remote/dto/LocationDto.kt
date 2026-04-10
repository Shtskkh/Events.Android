package com.events.app.data.remote.dto

import com.events.app.BuildConfig
import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val id: Int,
    val title: String?,
    val address: String?,
    val preview: LocationPreviewDto? = null,
    val photosBucket: String? = null,
    val photos: List<LocationPhotoDto>? = null
) {
    fun buildPreviewUrl(): String? {
        val base = BuildConfig.BASE_URL.trimEnd('/')
        if (!photosBucket.isNullOrBlank() && !photos.isNullOrEmpty()) {
            val firstPhoto = photos.firstOrNull { !it.filename.isNullOrBlank() }
            if (firstPhoto != null) {
                return "$base/api/v/1/files/$photosBucket/${firstPhoto.filename}"
            }
        }

        // Старый формат: preview {bucket, key} — для обратной совместимости
        val bucket = preview?.bucket?.takeIf { it.isNotBlank() } ?: return null
        val key    = preview.key?.takeIf { it.isNotBlank() } ?: return null
        return "$base/api/v/1/files/$bucket/$key"
    }
}

@Serializable
data class LocationPreviewDto(
    val bucket: String?,
    val key: String?
)

@Serializable
data class LocationPhotoDto(
    val filename: String?,
    val order: Int
)