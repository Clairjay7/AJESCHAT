package com.example.ajeschat.data

import com.example.ajeschat.session.Session
import com.example.ajeschat.session.SessionHolder
import com.example.ajeschat.session.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore
) {

    /**
     * POST api/login with JSON (username or email + password).
     * On success stores token, user_id, name, role and returns Success.
     */
    suspend fun login(username: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        val user = username.trim()
        val pass = password
        if (user.isEmpty() || pass.isEmpty()) {
            return@withContext LoginResult.Failure("Username and password are required.")
        }
        val body = LoginRequest(username = user, password = pass)
        val response = try {
            authApi.login(body)
        } catch (e: IOException) {
            val msg = "Network error. Check BASE_URL and that AJES is running. ${e.message ?: ""}"
            return@withContext LoginResult.Failure(msg.trim())
        } catch (e: HttpException) {
            return@withContext LoginResult.Failure(e.message() ?: "Login failed.")
        }
        when (response.code()) {
            200 -> {
                val resp = response.body()
                if (resp?.status == "success" && resp.data != null) {
                    val d = resp.data
                    val session = Session(
                        id = d.user_id,
                        name = d.name ?: d.username ?: user,
                        role = d.role ?: "",
                        token = d.token ?: ""
                    )
                    SessionHolder.updateSession(session)
                    sessionStore.save(session)
                    LoginResult.Success
                } else {
                    LoginResult.Failure(resp?.message ?: "Invalid credentials.")
                }
            }
            400, 401 -> {
                val msg = response.body()?.message ?: "Invalid credentials."
                LoginResult.Failure(msg)
            }
            403 -> {
                LoginResult.Failure("Account locked. Please contact administrator.")
            }
            else -> {
                val msg = response.body()?.message ?: "Invalid credentials."
                LoginResult.Failure(msg)
            }
        }
    }

    fun logout() {
        try {
            runBlocking(Dispatchers.IO) { authApi.logout() }
        } catch (_: Exception) { }
        SessionHolder.clearSession()
        sessionStore.clear()
    }

    sealed class LoginResult {
        data object Success : LoginResult()
        data class Failure(val message: String) : LoginResult()
    }
}
