package com.example.ajeschat

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.data.AuthRepository
import com.example.ajeschat.data.ChatRepository
import com.example.ajeschat.session.SessionHolder
import com.example.ajeschat.session.SessionStore
import okhttp3.OkHttpClient

class AjesChatApp : Application(), ImageLoaderFactory {

    lateinit var authRepository: AuthRepository
        private set
    lateinit var chatRepository: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val sessionStore = SessionStore(this)
        sessionStore.load()?.let { s ->
            if (s.token.isNotBlank()) SessionHolder.updateSession(s) else sessionStore.clear()
        }
        ApiModule.init(this)
        authRepository = AuthRepository(
            ApiModule.getAuthApi(),
            sessionStore
        )
        chatRepository = ChatRepository(
            ApiModule.getChatApi(),
            this
        )
    }

    override fun newImageLoader(): ImageLoader {
        val ok: OkHttpClient = ApiModule.getOkHttpClient() ?: OkHttpClient()
        return ImageLoader.Builder(this)
            // OkHttpClient implements Call.Factory — uses same auth interceptors as Retrofit (Coil 2.x, no extra artifact).
            .callFactory(ok)
            .crossfade(true)
            .build()
    }
}
