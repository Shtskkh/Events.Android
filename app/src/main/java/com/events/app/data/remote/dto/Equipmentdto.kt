package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EquipmentDto(
    val id: Int,
    val title: String?,
    val inventoryNumber: String?,
    val type: String?,
    val placeId: Int?
)

@Serializable
data class EquipmentTypeDto(
    val id: Int,
    val title: String?
)