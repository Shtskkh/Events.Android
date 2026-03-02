package com.events.app.data.remote.dto

import com.events.app.domain.models.events.Event
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private fun parseDateTime(raw: String): LocalDateTime {
    return try {
        LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: DateTimeParseException) {
        LocalDateTime.now()
    }
}

private fun fixPreviewUrl(url: String?): String? {
    val fixed = url?.replace("localhost", "10.0.2.2")
    android.util.Log.d("EventMapper", "Preview URL: $fixed")
    return fixed
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
        location = "",
        link = null,
        isPublic = true,
        isFinished = LocalDateTime.now().isAfter(parseDateTime(endDateTime)),
        previewUrl = fixPreviewUrl(previewDownloadLink)  // ← исправляем URL
    )
}

fun EventDetailDto.toDomain(): Event {
    return Event(
        id = id,
        title = title ?: "",
        announcement = "",
        description = description ?: "",
        startDate = parseDateTime(startDateTime),
        endDate = parseDateTime(endDateTime),
        format = format ?: "",
        places = 0,
        location = "",
        link = null,
        isPublic = true,
        isFinished = LocalDateTime.now().isAfter(parseDateTime(endDateTime)),
        previewUrl = fixPreviewUrl(previewDownloadLink)  // ← исправляем URL
    )
}