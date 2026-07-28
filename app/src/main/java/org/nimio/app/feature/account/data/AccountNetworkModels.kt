package org.nimio.app.feature.account.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorDto? = null
)

@Serializable
data class ApiFailureEnvelope(
    val success: Boolean = false,
    val error: ApiErrorDto? = null
)

@Serializable
data class ApiErrorDto(
    val code: String? = null,
    val message: String
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val username: String,
    @SerialName("display_name") val displayName: String
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class ResendVerificationRequestDto(
    val email: String
)

@Serializable
data class VerifyEmailRequestDto(
    val token: String
)

@Serializable
data class AvatarUploadResponseDto(
    @SerialName("avatar_url") val avatarUrl: String,
    val message: String = "Avatar uploaded successfully"
)

@Serializable
data class AuthPayloadDto(
    val user: UserDto,
    val profile: ProfileDto,
    val token: String
)

@Serializable
data class ProfilePayloadDto(
    val user: UserDto,
    val profile: ProfileDto
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    @SerialName("email_verified") val emailVerified: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class ProfileDto(
    @SerialName("user_id") val userId: String,
    val username: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

