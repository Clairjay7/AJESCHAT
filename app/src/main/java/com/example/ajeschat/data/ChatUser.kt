package com.example.ajeschat.data

import com.google.gson.annotations.SerializedName

/** Same as getChatUserList(): id, name, role, has_chat (optional). */
data class ChatUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String?,
    @SerializedName("has_chat") val hasChat: Boolean = false
)

data class ChatUsersResponse(
    @SerializedName("users") val users: List<ChatUser>
)
