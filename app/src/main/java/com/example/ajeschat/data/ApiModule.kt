package com.example.ajeschat.data

import android.content.Context
import com.example.ajeschat.BuildConfig
import com.example.ajeschat.session.SessionStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiModule {

    val baseUrl: String get() = BuildConfig.BASE_URL

    private var appContext: Context? = null
    private var okHttp: OkHttpClient? = null
    private var _chatApi: ChatApi? = null
    private var _authApi: AuthApi? = null
    private var _announcementApi: AnnouncementApi? = null
    private var _profileApi: ProfileApi? = null

    fun init(context: Context) {
        if (okHttp != null) return
        appContext = context.applicationContext
        val authInterceptor = Interceptor { chain ->
            val token = SessionStore(appContext!!).load()?.token
            val b = chain.request().newBuilder()
                // AJES AuthFilter returns 302 to login unless this looks like an API client (Bearer or Accept JSON).
                .header("Accept", "application/json")
            val request = if (!token.isNullOrBlank()) {
                b.addHeader("Authorization", "Bearer $token").build()
            } else {
                b.build()
            }
            chain.proceed(request)
        }
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        okHttp = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            // Follow 302/303 (e.g. canonical URL rewrites); avoids surfacing "HTTP 302 Found" when server redirects harmlessly.
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp!!)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        _chatApi = retrofit.create(ChatApi::class.java)
        _authApi = retrofit.create(AuthApi::class.java)
        _announcementApi = retrofit.create(AnnouncementApi::class.java)
        _profileApi = retrofit.create(ProfileApi::class.java)
    }

    fun getOkHttpClient(): OkHttpClient? = okHttp
    fun getChatApi(): ChatApi = _chatApi!!
    fun getAuthApi(): AuthApi = _authApi!!
    fun getAnnouncementApi(): AnnouncementApi = _announcementApi!!
    fun getProfileApi(): ProfileApi = _profileApi!!
}
