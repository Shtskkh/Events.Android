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
    val preview: PlacePreviewDto? = null
) {

    fun buildPreviewUrl(): String? {
        val bucket = preview?.bucket?.takeIf { it.isNotBlank() } ?: return null
        val key    = preview.key?.takeIf { it.isNotBlank() } ?: return null
        val base   = BuildConfig.BASE_URL.trimEnd('/')
        return "$base/api/v/1/files/$bucket/$key"
    }
}

@Serializable
data class PlacePreviewDto(
    val bucket: String?,
    val key: String?
)