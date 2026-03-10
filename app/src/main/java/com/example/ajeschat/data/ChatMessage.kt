package com.example.ajeschat.data

import com.google.gson.annotations.SerializedName

/** Same as web: id, sender_id, receiver_id, content, created_at, is_mine, unsent_for_all */
data class ChatMessage(
    @SerializedName("id") val id: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("receiver_id") val receiverId: Int,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_mine") val isMine: Boolean,
    @SerializedName("unsent_for_all") val unsentForAll: Boolean = false
)

data class ChatMessagesResponse(
    @SerializedName("messages") val messages: List<ChatMessage>
)
