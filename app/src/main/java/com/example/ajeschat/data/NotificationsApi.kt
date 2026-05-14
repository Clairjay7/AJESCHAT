package com.example.ajeschat.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NotificationsApi {
    @GET("api/notifications")
    suspend fun list(): Response<NotificationsListResponse>

    @POST("api/notifications/mark-read")
    suspend fun markRead(@Body body: NotificationMarkReadRequest): Response<NotificationOkResponse>

    @POST("api/notifications/mark-all-read")
    suspend fun markAllRead(): Response<NotificationOkResponse>
}

data class NotificationsListResponse(
    @SerializedName("items") val items: List<NotificationItem> = emptyList()
)

data class NotificationItem(
    @SerializedName("id") val id: Int,
    @SerializedName("message") val message: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("type") val type: String? = null
)

data class NotificationMarkReadRequest(
    @SerializedName("id") val id: Int
)

data class NotificationOkResponse(
    @SerializedName("ok") val ok: Boolean? = null
)
