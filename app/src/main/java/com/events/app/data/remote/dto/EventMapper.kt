package com.events.app.data.remote.dto

import com.events.app.domain.models.events.Event
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private fun parseDateTime(raw: String): LocalDateTime {
    // Сначала пробуем OffsetDateTime (содержит timezone offset, например "2026-03-24T10:00:00Z")
    return try {
        OffsetDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
    } catch (e: DateTimeParseException) {
        // Fallback: строка без offset — парсим как LocalDateTime напрямую
        try {
            LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e2: DateTimeParseException) {
            LocalDateTime.now()
        }
    }
}

private fun buildPreviewUrl(previewInfo: PreviewInfoDto?): String? {
    if (previewInfo == null) return null
    val bucket = previewInfo.bucket ?: return null
    val key = previewInfo.key ?: return null
    if (bucket.isBlank() || key.isBlank()) return null
    return "http://10.0.2.2:8080/api/v/1/files/$bucket/$key"
}

fun ShortEventDto.toDomain(): Event {
    val parsedEnd = parseDateTime(endDateTime)
    return Event(
        id                = id,
        title             = title ?: "",
        announcement      = announcement ?: "",
        description       = "",
        startDate         = parseDateTime(startDateTime),
        endDate           = parsedEnd,
        format            = format ?: "",
        places            = 0,
        location          = locationTitle ?: "",
        placeNumber       = null,
        link              = null,
        type              = type ?: "",
        isPublic          = true,
        isFinished        = LocalDateTime.now().isAfter(parsedEnd),
        previewUrl        = buildPreviewUrl(previewInfo),
        needsRegistration = false,
        maxParticipants   = null,
        organizerName     = null,
        participantsCount = null,
        viewsCount        = null,
    )
}

fun EventDetailDto.toDomain(): Event {
    val parsedEnd = parseDateTime(endDateTime)
    return Event(
        id                = id,
        title             = title ?: "",
        announcement      = announcement ?: "",
        type              = type ?: "",
        description       = description ?: "",
        startDate         = parseDateTime(startDateTime),
        endDate           = parsedEnd,
        format            = format ?: "",
        places            = maxParticipants ?: 0,
        location          = locationTitle ?: "",
        placeNumber       = placeInfo?.number,
        link              = null,
        isPublic          = isPublic ?: true,
        isFinished        = LocalDateTime.now().isAfter(parsedEnd),
        previewUrl        = buildPreviewUrl(previewInfo),
        needsRegistration = needsRegistration ?: false,
        maxParticipants   = maxParticipants,
        organizerName     = organizerName,
        participantsCount = null,  // приходит из аналитики, не из detail
        viewsCount        = null,  // приходит из аналитики, не из detail
    )
}