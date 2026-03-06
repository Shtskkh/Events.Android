package com.events.app.domain.models.events

import java.time.LocalDateTime

data class Event(
    val id: String,
    // Название события
    val title: String,
    // Анонс или краткое описание события
    val announcement: String,
    // Полное описание события
    val description: String,
    // Дата и время начала события
    val startDate: LocalDateTime,
    // Дата и время окончания события
    val endDate: LocalDateTime,

    val type: String,
    // Формат события
    val format: String,
    // Количество мест
    val places: Int,
    // Место проведения события
    val location: String,
    // Ссылка на событие
    val link: String?,
    // Флаг публичности события. True, если событие открыто для всех; false, если оно приватное (требует приглашения или доступа).
    val isPublic: Boolean,
    // Флаг завершения события. True, если событие уже прошло или завершено; false, если оно предстоящее или в процессе.
    val isFinished: Boolean,
    val previewUrl: String?
)