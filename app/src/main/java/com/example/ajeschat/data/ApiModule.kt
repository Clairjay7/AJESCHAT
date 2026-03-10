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

    fun init(context: Context) {
        if (okHttp != null) return
        appContext = context.applicationContext
        val authInterceptor = Interceptor { chain ->
            val token = SessionStore(appContext!!).load()?.token
            val request = if (!token.isNullOrBlank()) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        okHttp = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
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
    }

    fun getOkHttpClient(): OkHttpClient? = okHttp
    fun getChatApi(): ChatApi = _chatApi!!
    fun getAuthApi(): AuthApi = _authApi!!
}
