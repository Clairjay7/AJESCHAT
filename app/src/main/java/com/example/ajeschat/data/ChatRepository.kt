package com.example.ajeschat.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.ajeschat.session.SessionHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class ChatRepository(
    private val chatApi: ChatApi,
    private val appContext: Context?
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
            val me = SessionHolder.session?.id ?: 0
            (res.body()?.messages ?: emptyList()).map { m ->
                val mine = me > 0 && m.senderId == me
                if (mine == m.isMine) m else m.copy(isMine = mine)
            }
        }
    }

    suspend fun send(receiverId: Int, content: String, attachmentUri: Uri? = null): Result<Unit> =
        withContext(Dispatchers.IO) {
            val session = SessionHolder.session
                ?: return@withContext Result.failure(IllegalStateException("Not logged in"))
            if (receiverId == session.id) {
                return@withContext Result.failure(IllegalArgumentException("Cannot send to self"))
            }
            val ctx = appContext ?: return@withContext Result.failure(IllegalStateException("No context"))
            val trimmed = content.trim()
            if (trimmed.isEmpty() && attachmentUri == null) {
                return@withContext Result.failure(IllegalArgumentException("Message is empty"))
            }
            runCatching {
                if (attachmentUri == null) {
                    val res = chatApi.send(receiverId, trimmed)
                    if (!res.isSuccessful) throw HttpException(res)
                } else {
                    val mime = ctx.contentResolver.getType(attachmentUri) ?: "application/octet-stream"
                    val name = queryDisplayName(ctx, attachmentUri) ?: "attachment"
                    val bytes = ctx.contentResolver.openInputStream(attachmentUri)?.use { it.readBytes() }
                        ?: throw IllegalStateException("Cannot read attachment")
                    val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("attachment", name, body)
                    val recv = receiverId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val textBody = trimmed.toRequestBody("text/plain".toMediaTypeOrNull())
                    val res = chatApi.sendMultipart(recv, textBody, part)
                    if (!res.isSuccessful) throw HttpException(res)
                }
            }
        }

    suspend fun setTyping(toUserId: Int, typing: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val res = chatApi.setTyping(TypingRequest(to = toUserId, typing = if (typing) 1 else 0))
            if (!res.isSuccessful) throw HttpException(res)
        }
    }

    suspend fun getPartnerTyping(withUserId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val res = chatApi.getTyping(withUserId)
            if (!res.isSuccessful) throw HttpException(res)
            res.body()?.typing == true
        }
    }

    suspend fun unsend(messageId: Int, scope: String, withId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val res = chatApi.unsend(messageId, scope, withId)
            if (!res.isSuccessful) throw HttpException(res)
        }
    }

    suspend fun deleteConversation(withUserId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val res = chatApi.deleteConversation(withUserId)
            if (!res.isSuccessful) throw HttpException(res)
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c ->
                    if (c.moveToFirst()) {
                        val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (i >= 0) return c.getString(i)
                    }
                }
        }
        return uri.lastPathSegment
    }
}
