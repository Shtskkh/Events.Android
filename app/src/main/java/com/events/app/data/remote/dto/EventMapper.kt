package com.events.app.data.remote.dto

import com.events.app.domain.models.events.Event
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.OffsetDateTime

private fun parseDateTime(raw: String): LocalDateTime {
    return try {
        // Сначала пробуем LocalDateTime
        LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: DateTimeParseException) {
        try {
            // Если есть смещение +00:00 — парсим как OffsetDateTime
            OffsetDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .toLocalDateTime()
        } catch (e2: DateTimeParseException) {
            LocalDateTime.now()
        }
    }
}

private fun buildPreviewUrl(previewInfo: PreviewInfoDto?): String? {
    if (previewInfo == null || previewInfo.key.isNullOrEmpty()) return null
    val url = "http://10.0.2.2:8080/api/v/1/files/${previewInfo.bucket}/${previewInfo.key}"
    android.util.Log.d("EventMapper", "Preview URL: $url")
    return url
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
        type = type ?: "",   // ← добавить в оба маппера
        isPublic = true,
        isFinished = LocalDateTime.now().isAfter(parseDateTime(endDateTime)),
        previewUrl = buildPreviewUrl(previewInfo)
    )
}

fun EventDetailDto.toDomain(): Event {
    return Event(
        id = id,
        title = title ?: "",
        announcement = "",
        type = type ?: "",   // ← добавить в оба маппера
        description = description ?: "",
        startDate = parseDateTime(startDateTime),
        endDate = parseDateTime(endDateTime),
        format = format ?: "",
        places = 0,
        location = "",
        link = null,
        isPublic = true,
        isFinished = LocalDateTime.now().isAfter(parseDateTime(endDateTime)),
        previewUrl = buildPreviewUrl(previewInfo)
    )
}