package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaceDto(
    val id: Int,
    val number: String?,
    val capacity: Int,
    val type: String?,
    val title: String?
)