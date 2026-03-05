package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val id: Int,
    val title: String?,
    val address: String?
)