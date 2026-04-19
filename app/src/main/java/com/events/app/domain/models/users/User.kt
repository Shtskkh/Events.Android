package com.events.app.domain.models.users

import kotlinx.serialization.Serializable

enum class UserRole { ADMIN, USER, GUEST }

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.GUEST,
    val accessToken: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val patronymic: String? = null
) {
    val fullName: String get() {
        val parts = listOfNotNull(
            lastName?.trim(),
            firstName?.trim(),
            patronymic?.trim()
        ).filter { it.isNotEmpty() }
        return if (parts.isEmpty()) name else parts.joinToString(" ")
    }
}