package com.events.app.domain.models.events

import java.time.LocalDateTime

data class Event(
    val id: String,
    val title: String,
    val announcement: String,
    val description: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val type: String,
    val format: String,
    val places: Int,
    val location: String,
    // Номер помещения из placeInfo.number
    val placeNumber: String?,
    val link: String?,
    val isPublic: Boolean,
    val isFinished: Boolean,
    val previewUrl: String?,
    val needsRegistration: Boolean,
    val maxParticipants: Int?,
    val organizerName: String?,
)