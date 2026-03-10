package com.example.ajeschat.data

import com.example.ajeschat.session.SessionHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ChatRepository(
    private val chatApi: ChatApi,
    private val csrfFetcher: com.example.ajeschat.network.CsrfFetcher? = null
) {

    suspend fun getUsers(): Result<List<ChatUser>> = withContext(Dispatchers.IO) {
        runCatching {
            val res = chatApi.getUsers()
            if (!res.isSuccessful) throw HttpException(res)
            (res.body()?.users ?: emptyList())
        }
    }

    suspend fun getMessages(withUserId: Int): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        runCatching {
            val res = chatApi.getMessages(withUserId)
            if (!res.isSuccessful) throw HttpException(res)
            res.body()?.messages ?: emptyList()
        }
    }

    suspend fun send(receiverId: Int, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        val session = SessionHolder.session ?: return@withContext Result.failure(IllegalStateException("Not logged in"))
        if (receiverId == session.id) return@withContext Result.failure(IllegalArgumentException("Cannot send to self"))
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return@withContext Result.failure(IllegalArgumentException("Message is empty"))
        runCatching {
            val res = chatApi.send(receiverId, trimmed)
            if (!res.isSuccessful) throw HttpException(res)
        }
    }

    suspend fun unsend(messageId: Int, scope: String, withId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val res = chatApi.unsend(messageId, scope, withId)
            if (!res.isSuccessful) throw HttpException(res)
        }
    }
}
