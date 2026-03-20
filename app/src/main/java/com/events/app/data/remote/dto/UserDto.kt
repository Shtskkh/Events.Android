package com.events.app.data.remote.dto

import kotlinx.serialization.Serializable

// GET /api/v/1/users — список
@Serializable
data class UserDto(
    val id: String,
    val lastName: String?,
    val firstName: String?,
    val patronymic: String?,
    val avatarInfo: AvatarInfoDto? = null
) {
    // Удобное полное имя для отображения
    val displayName: String get() {
        val parts = listOfNotNull(
            lastName?.trim(),
            firstName?.trim(),
            patronymic?.trim()
        ).filter { it.isNotEmpty() }
        return if (parts.isEmpty()) "—" else parts.joinToString(" ")
    }
}

// GET /api/v/1/users/{id} — детали (содержит email)
@Serializable
data class UserDetailDto(
    val id: String,
    val lastName: String?,
    val firstName: String?,
    val patronymic: String?,
    val email: String?,
    val avatarInfo: AvatarInfoDto? = null
)

@Serializable
data class AvatarInfoDto(
    val bucket: String?,
    val key: String?
)