package com.events.app.domain.models.events

import java.time.LocalDateTime

data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val date: LocalDateTime,
    val isFinished: Boolean
)