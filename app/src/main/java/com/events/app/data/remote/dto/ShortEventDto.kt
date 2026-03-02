package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShortEventDto(
    val id: String,           // ← добавили это поле!
    val title: String?,
    val announcement: String?,
    val type: String?,
    val format: String?,
    val startDateTime: String,
    val endDateTime: String,
    val previewDownloadLink: String?
)