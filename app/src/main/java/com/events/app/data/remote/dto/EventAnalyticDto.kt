package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ViewsDto(
    val date: String,
    val views: Int
)

@Serializable
data class EventAnalyticDto(
    val id: String,
    val maxParticipantsCount: Int? = null,
    val participantsCount: Int? = null,
    val finalParticipantsCount: Int? = null,
    val views: List<ViewsDto> = emptyList(),
    val viewsCount: Int = 0
)