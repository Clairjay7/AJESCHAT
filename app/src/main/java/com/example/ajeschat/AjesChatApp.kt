package com.example.ajeschat

import android.app.Application
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.data.AuthRepository
import com.example.ajeschat.data.ChatRepository
import com.example.ajeschat.session.SessionHolder
import com.example.ajeschat.session.SessionStore

class AjesChatApp : Application() {
    lateinit var authRepository: AuthRepository
        private set
    lateinit var chatRepository: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        ApiModule.init(this)
        val sessionStore = SessionStore(this)
        sessionStore.load()?.let { SessionHolder.updateSession(it) }
        authRepository = AuthRepository(
            ApiModule.getAuthApi(),
            ApiModule.getCsrfFetcher(),
            sessionStore,
            ApiModule.getCookieJar()
        )
        chatRepository = ChatRepository(
            ApiModule.getChatApi(),
            ApiModule.getCsrfFetcher()
        )
    }
}
