package com.example.ajeschat.data

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Same endpoints as web: api/chat/users (recommended backend add-on), chat/messages, chat/send, chat/unsend.
 */
interface ChatApi {

    @GET("api/chat/users")
    suspend fun getUsers(): Response<ChatUsersResponse>

    @GET("chat/messages")
    suspend fun getMessages(@Query("with") withUserId: Int): Response<ChatMessagesResponse>

    @FormUrlEncoded
    @POST("chat/send")
    suspend fun send(
        @Field("receiver_id") receiverId: Int,
        @Field("content") content: String
    ): Response<okhttp3.ResponseBody>

    @FormUrlEncoded
    @POST("chat/unsend")
    suspend fun unsend(
        @Field("message_id") messageId: Int,
        @Field("scope") scope: String,
        @Field("with_id") withId: Int
    ): Response<okhttp3.ResponseBody>
}
