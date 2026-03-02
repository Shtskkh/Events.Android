package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventsPageDto(
    val items: List<ShortEventDto>
)