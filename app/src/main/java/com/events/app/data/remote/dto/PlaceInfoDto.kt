package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaceInfoDto(
    val placeId: Int,
    val number: String?,
    val title: String? = null  // ← ДОБАВИТЬ: сервер может вернуть title помещения
)