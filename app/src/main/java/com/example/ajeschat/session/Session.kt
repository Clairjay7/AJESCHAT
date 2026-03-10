package com.example.ajeschat.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Session(
    val id: Int,
    val name: String,
    val role: String,
    val token: String = ""
)

object SessionHolder {
    var session by mutableStateOf<Session?>(null)
        private set

    fun updateSession(s: Session) {
        session = s
    }

    fun clearSession() {
        session = null
    }
}
