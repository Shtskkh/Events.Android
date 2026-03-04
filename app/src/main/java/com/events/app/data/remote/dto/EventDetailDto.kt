package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventDetailDto(
    val id: String,
    val title: String?,
    val description: String?,
    val type: String?,
    val format: String?,
    val startDateTime: String,
    val endDateTime: String,
    val previewInfo: PreviewInfoDto?   // ← было previewDownloadLink: String?
)