package com.example.ajeschat.data

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Compatible with existing AJES Auth. POST auth/login with form body; session via cookies.
 */
interface AuthApi {

    @FormUrlEncoded
    @POST("auth/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("csrf_test_name") csrfToken: String? = null
    ): Call<ResponseBody>

    @GET("auth/logout")
    fun logout(): Call<ResponseBody>
}
