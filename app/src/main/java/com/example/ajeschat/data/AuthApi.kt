package com.example.ajeschat.data

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
    val status: String?,
    val data: LoginData?,
    val message: String?
)

data class LoginData(
    val user_id: Int,
    val username: String?,
    val name: String?,
    val role: String?,
    val token: String?
)

data class LogoutResponse(
    val status: String?,
    val data: Any?,
    val message: String?
)
