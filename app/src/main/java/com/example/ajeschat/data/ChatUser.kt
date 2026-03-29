package com.example.ajeschat.data

import com.google.gson.annotations.SerializedName

/**
 * getChatUserList() JSON: id, name, role, has_chat (optional).
 * Optional for richer rows: last_message, last_message_at, pinned, active_status (e.g. "30m").
 */
data class ChatUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String?,
    @SerializedName("has_chat") val hasChat: Boolean = false,
    @SerializedName("last_message") val lastMessagePreview: String? = null,
    @SerializedName("last_message_at") val lastMessageAt: String? = null,
    @SerializedName("pinned") val pinned: Boolean = false,
    @SerializedName("active_status") val activeStatus: String? = null
)

data class ChatUsersResponse(
    @SerializedName("users") val users: List<ChatUser>
)
