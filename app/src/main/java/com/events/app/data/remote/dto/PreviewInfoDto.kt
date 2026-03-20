package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

// ВАЖНО: оба поля nullable — сервер может вернуть null для bucket и key
@Serializable
data class PreviewInfoDto(
    val bucket: String?,
    val key: String?
)