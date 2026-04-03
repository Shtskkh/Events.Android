package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaceAvailabilityDto(
    val id: Int,
    val number: String?,
    val capacity: Int,
    val type: String?,
    val isAvailable: Boolean,
    val title: String?
)