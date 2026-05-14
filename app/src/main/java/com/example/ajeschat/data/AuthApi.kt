package com.example.ajeschat.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Uses AJES token API: POST api/login (JSON) returns token; POST api/logout with Bearer.
 * Same accounts as web (users table); chat endpoints accept Authorization: Bearer.
 */
interface AuthApi {

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/logout")
    suspend fun logout(): Response<LogoutResponse>
}

data class LoginRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String
)

data class LoginResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("data") val data: LoginData?,
    @SerializedName("message") val message: String?
)

data class LoginData(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("token") val token: String?
)

data class LogoutResponse(
    val status: String?,
    val data: Any?,
    val message: String?
)
