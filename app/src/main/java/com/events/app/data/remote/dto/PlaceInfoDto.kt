package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaceInfoDto(
    val placeId: Int,
    val number: String?
)