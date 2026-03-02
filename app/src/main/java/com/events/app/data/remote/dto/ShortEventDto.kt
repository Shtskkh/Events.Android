package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShortEventDto(
    val title: String?,
    val announcement: String?,
    val type: String?,        // теперь просто строка
    val format: String?,      // теперь просто строка
    val startDateTime: String,
    val endDateTime: String,
    val previewDownloadLink: String?
)