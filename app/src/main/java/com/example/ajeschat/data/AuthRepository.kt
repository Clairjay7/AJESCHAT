package com.example.ajeschat.data

import com.example.ajeschat.network.PersistentCookieJar
import com.example.ajeschat.session.Session
import com.example.ajeschat.session.SessionHolder
import com.example.ajeschat.session.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val authApi: AuthApi,
    private val csrfFetcher: com.example.ajeschat.network.CsrfFetcher?,
    private val sessionStore: SessionStore,
    private val cookieJar: PersistentCookieJar?
) {

    /**
     * POST auth/login with form; use cookies. Success = 302 to dashboard + session cookie.
     * Errors: empty -> "Username and password are required."; invalid -> "Invalid credentials."; locked -> "Account locked. Please contact administrator."
     */
    suspend fun login(username: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        val user = username.trim()
        val pass = password
        if (user.isEmpty() || pass.isEmpty()) {
            return@withContext LoginResult.Failure("Username and password are required.")
        }
        val csrf = csrfFetcher?.fetchCsrfToken()
        val call = authApi.login(user, pass, csrf)
        val response = call.execute()
        val raw = response.raw()
        when (raw.code) {
            302 -> {
                val location = raw.header("Location") ?: ""
                val hasSessionCookie = raw.headers("Set-Cookie").any { it.contains("ci_session") || it.contains("session") }
                if (location.contains("dashboard") && hasSessionCookie) {
                    val session = Session(id = -1, name = user, role = "")
                    SessionHolder.updateSession(session)
                    sessionStore.save(session)
                    LoginResult.Success
                } else {
                    LoginResult.Failure("Invalid credentials.")
                }
            }
            200 -> {
                val body = response.body()?.string() ?: ""
                when {
                    body.contains("Account locked", ignoreCase = true) -> LoginResult.Failure("Account locked. Please contact administrator.")
                    body.contains("Invalid credentials", ignoreCase = true) -> LoginResult.Failure("Invalid credentials.")
                    body.contains("Username and password are required", ignoreCase = true) -> LoginResult.Failure("Username and password are required.")
                    else -> LoginResult.Failure("Invalid credentials.")
                }
            }
            else -> LoginResult.Failure("Invalid credentials.")
        }
    }

    fun logout() {
        SessionHolder.clearSession()
        sessionStore.clear()
        cookieJar?.clear()
        try {
            authApi.logout().execute()
        } catch (_: Exception) { }
    }

    sealed class LoginResult {
        data object Success : LoginResult()
        data class Failure(val message: String) : LoginResult()
    }
}
