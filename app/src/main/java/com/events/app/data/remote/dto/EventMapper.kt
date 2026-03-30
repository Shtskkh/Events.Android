package com.events.app.data.remote.dto

import com.events.app.domain.models.events.Event
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private fun parseDateTime(raw: String): LocalDateTime {
    return try {
        OffsetDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
    } catch (e: DateTimeParseException) {
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
    val key    = previewInfo.key    ?: return null
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
        // API не возвращает locationTitle в ShortEventDto —
        // location будет подставлен из EventLocationCache в EventsViewModel
        location          = "",
        locationAddress   = null,
        placeNumber       = null,
        placeTitle        = null,
        placeCapacity     = null,
        placeId           = null,
        locationId        = null,
        link              = null,
        type              = type ?: "",
        isPublic          = true,
        isFinished        = LocalDateTime.now().isAfter(parsedEnd),
        previewUrl        = buildPreviewUrl(previewInfo),
        needsRegistration = false,
        maxParticipants   = null,
        organizerName     = null,
        userId            = userId,
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
        location          = "",       // заполняется в EventDetailsViewModel после дозапроса
        locationAddress   = null,     // заполняется в EventDetailsViewModel после дозапроса
        placeNumber       = placeInfo?.number,
        placeTitle        = null,     // заполняется в EventDetailsViewModel после дозапроса
        placeCapacity     = null,     // заполняется в EventDetailsViewModel после дозапроса
        placeId           = placeInfo?.placeId,
        locationId        = null,     // заполняется в EventDetailsViewModel после дозапроса
        link              = null,
        isPublic          = isPublic ?: true,
        isFinished        = LocalDateTime.now().isAfter(parsedEnd),
        previewUrl        = buildPreviewUrl(previewInfo),
        needsRegistration = needsRegistration ?: false,
        maxParticipants   = maxParticipants,
        organizerName     = organizerName,
        userId            = userId,
        participantsCount = null,
        viewsCount        = null,
    )
}