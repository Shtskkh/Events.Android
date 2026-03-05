package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventFormatDto(
    val id: Int,
    val title: String?
)