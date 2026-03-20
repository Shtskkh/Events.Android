package com.events.app.data.remote.dto

import com.events.app.domain.models.events.Event
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private fun parseDateTime(raw: String): LocalDateTime {
    return try {
        LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: DateTimeParseException) {
        try {
            OffsetDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .toLocalDateTime()
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
    return Event(
        id = id,
        title = title ?: "",
        announcement = announcement ?: "",
        description = "",
        startDate = parseDateTime(startDateTime),
        endDate = parseDateTime(endDateTime),
        format = format ?: "",
        places = 0,
        location = locationTitle ?: "",
        placeNumber = null,
        link = null,
        type = type ?: "",
        isPublic = true,
        isFinished = LocalDateTime.now().isAfter(parseDateTime(endDateTime)),
        previewUrl = buildPreviewUrl(previewInfo),
        needsRegistration = false,
        maxParticipants = null,
        organizerName = null,
    )
}

fun EventDetailDto.toDomain(): Event {
    return Event(
        id = id,
        title = title ?: "",
        announcement = announcement ?: "",
        type = type ?: "",
        description = description ?: "",
        startDate = parseDateTime(startDateTime),
        endDate = parseDateTime(endDateTime),
        format = format ?: "",
        places = maxParticipants ?: 0,
        location = locationTitle ?: "",
        // placeInfo.number — номер помещения
        placeNumber = placeInfo?.number,
        link = null,
        isPublic = isPublic ?: true,
        isFinished = LocalDateTime.now().isAfter(parseDateTime(endDateTime)),
        previewUrl = buildPreviewUrl(previewInfo),
        needsRegistration = needsRegistration ?: false,
        maxParticipants = maxParticipants,
        organizerName = organizerName,
    )
}