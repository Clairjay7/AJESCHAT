package com.example.ajeschat.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProfileApi {
    @GET("api/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @POST("api/profile")
    suspend fun updateProfile(@Body body: ProfileUpdateRequest): Response<ProfileUpdateResponse>
}

data class ProfileResponse(
    val status: String? = null,
    val data: ProfileData? = null,
    val message: String? = null
)

data class ProfileData(
    val id: Int,
    val username: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val contact_number: String? = null,
    val bio: String? = null,
    val profile_photo_url: String? = null,
    val privileges: List<String>? = null
)

data class ProfileUpdateRequest(
    val name: String,
    val email: String,
    val contact_number: String? = null,
    val bio: String? = null,
    val old_password: String? = null,
    val new_password: String? = null,
    val confirm_password: String? = null,
    val remove_photo: Boolean? = null
)

data class ProfileUpdateResponse(
    val status: String? = null,
    val message: String? = null
)
