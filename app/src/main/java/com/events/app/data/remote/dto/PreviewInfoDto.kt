package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PreviewInfoDto(
    val bucket: String,
    val key: String?   // ← обязательно nullable!
)