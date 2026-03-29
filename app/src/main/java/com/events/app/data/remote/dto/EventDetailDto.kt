package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventDetailDto(
    val id: String,
    val title: String?,
    val description: String?,
    val type: String?,
    val format: String?,
    val needsRegistration: Boolean? = null,
    val startDateTime: String,
    val endDateTime: String,
    val previewInfo: PreviewInfoDto?,
    val placeInfo: PlaceInfoDto? = null,
    val announcement: String? = null,
    val locationTitle: String? = null,
    val locationAddress: String? = null,
    val maxParticipants: Int? = null,
    val isPublic: Boolean? = null,
    val organizerName: String? = null,
    val userId: String? = null,
)