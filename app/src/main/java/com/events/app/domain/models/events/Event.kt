package com.events.app.domain.models.events

import java.time.LocalDateTime

data class Tag(val id: Int, val value: String)

data class Event(
    val id: String,
    val title: String,
    val announcement: String,
    val description: String,
    val type: String,
    val format: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val places: Int,
    val location: String,
    val locationAddress: String? = null,
    val placeNumber: String?,
    val placeTitle: String? = null,
    val placeCapacity: Int? = null,
    val placeId: Int? = null,
    val locationId: Int? = null,
    val link: String?,
    val isPublic: Boolean,
    val isFinished: Boolean,
    val previewUrl: String?,
    val needsRegistration: Boolean,
    val maxParticipants: Int?,
    val organizerName: String?,
    val userId: String? = null,
    val participantsCount: Int? = null,
    val viewsCount: Int? = null,
    val finalParticipantsCount: Int? = null,
    val tags: List<Tag> = emptyList()
)