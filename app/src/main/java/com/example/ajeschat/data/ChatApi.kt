package com.example.ajeschat.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * Same endpoints as web: api/chat/users, chat/messages, chat/send, chat/unsend, api/chat/typing.
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

    @Multipart
    @POST("chat/send")
    suspend fun sendMultipart(
        @Part("receiver_id") receiverId: RequestBody,
        @Part("content") content: RequestBody,
        @Part attachment: MultipartBody.Part?
    ): Response<okhttp3.ResponseBody>

    @FormUrlEncoded
    @POST("chat/unsend")
    suspend fun unsend(
        @Field("message_id") messageId: Int,
        @Field("scope") scope: String,
        @Field("with_id") withId: Int
    ): Response<okhttp3.ResponseBody>

    @FormUrlEncoded
    @POST("chat/delete_conversation")
    suspend fun deleteConversation(
        @Field("with_id") withUserId: Int
    ): Response<okhttp3.ResponseBody>

    @POST("api/chat/typing")
    suspend fun setTyping(@Body body: TypingRequest): Response<okhttp3.ResponseBody>

    @GET("api/chat/typing")
    suspend fun getTyping(@Query("with") withUserId: Int): Response<TypingResponse>
}
