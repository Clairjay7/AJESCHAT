package com.example.ajeschat.data

import android.content.Context
import com.example.ajeschat.BuildConfig
import com.example.ajeschat.network.CsrfFetcher
import com.example.ajeschat.network.PersistentCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiModule {

    val baseUrl: String get() = BuildConfig.BASE_URL

    private var cookieJar: PersistentCookieJar? = null
    private var okHttp: OkHttpClient? = null
    private var _chatApi: ChatApi? = null
    private var _authApi: AuthApi? = null

    fun init(context: Context) {
        if (cookieJar != null) return
        cookieJar = PersistentCookieJar(context.applicationContext)
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        okHttp = OkHttpClient.Builder()
            .cookieJar(cookieJar!!)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
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

    fun getCookieJar(): PersistentCookieJar? = cookieJar
    fun getOkHttpClient(): OkHttpClient? = okHttp
    fun getCsrfFetcher(): CsrfFetcher? = okHttp?.let { CsrfFetcher(it, baseUrl) }
    fun getChatApi(): ChatApi = _chatApi!!
    fun getAuthApi(): AuthApi = _authApi!!
}
