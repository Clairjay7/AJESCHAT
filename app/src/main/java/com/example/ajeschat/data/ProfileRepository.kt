package com.example.ajeschat.data

import android.content.Context
import android.net.Uri
import com.example.ajeschat.BuildConfig
import com.example.ajeschat.session.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileRepository(
    private val appContext: Context,
    private val api: ProfileApi
) {
    suspend fun getProfile(): Result<ProfileData> {
        return runCatching {
            val response = api.getProfile()
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to load profile (${response.code()})")
            }
            response.body()?.data ?: throw IllegalStateException("Profile data not available")
        }
    }

    suspend fun updateProfile(
        name: String,
        email: String,
        contactNumber: String,
        bio: String,
        oldPassword: String = "",
        newPassword: String = "",
        confirmPassword: String = "",
        photoUri: Uri? = null,
        removePhoto: Boolean = false
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val changingPw = newPassword.isNotBlank() || confirmPassword.isNotBlank()
                if (photoUri != null) {
                    uploadMultipart(
                        name = name,
                        email = email,
                        contactNumber = contactNumber,
                        bio = bio,
                        oldPassword = if (changingPw) oldPassword else "",
                        newPassword = if (changingPw) newPassword else "",
                        confirmPassword = if (changingPw) confirmPassword else "",
                        photoUri = photoUri
                    )
                } else {
                    val payload = ProfileUpdateRequest(
                        name = name.trim(),
                        email = email.trim(),
                        contact_number = contactNumber.trim().ifBlank { null },
                        bio = bio.trim().ifBlank { null },
                        old_password = if (changingPw) oldPassword else null,
                        new_password = if (changingPw) newPassword else null,
                        confirm_password = if (changingPw) confirmPassword else null,
                        remove_photo = if (removePhoto) true else null
                    )
                    val response = api.updateProfile(payload)
                    if (!response.isSuccessful) {
                        val msg = response.errorBody()?.string()?.let { parseJsonMessage(it) }
                            ?: response.body()?.message
                        throw IllegalStateException(msg ?: "Failed to update profile (${response.code()})")
                    }
                    response.body()?.message ?: "Profile updated."
                }
            }
        }
    }

    private fun uploadMultipart(
        name: String,
        email: String,
        contactNumber: String,
        bio: String,
        oldPassword: String,
        newPassword: String,
        confirmPassword: String,
        photoUri: Uri
    ): String {
        val token = SessionStore(appContext).load()?.token
            ?: throw IllegalStateException("Not logged in.")
        val client = ApiModule.getOkHttpClient()
            ?: throw IllegalStateException("HTTP client not ready.")
        val cr = appContext.contentResolver
        val mime = cr.getType(photoUri) ?: "image/jpeg"
        val bytes = cr.openInputStream(photoUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Could not read image.")
        if (bytes.size > 2 * 1024 * 1024) {
            throw IllegalStateException("Profile photo must be 2MB or less.")
        }
        val ext = when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            else -> "jpg"
        }
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name", name.trim())
            .addFormDataPart("email", email.trim())
            .addFormDataPart("contact_number", contactNumber.trim())
            .addFormDataPart("bio", bio.trim())
        if (oldPassword.isNotBlank()) body.addFormDataPart("old_password", oldPassword)
        if (newPassword.isNotBlank()) body.addFormDataPart("new_password", newPassword)
        if (confirmPassword.isNotBlank()) body.addFormDataPart("confirm_password", confirmPassword)
        val partBody = bytes.toRequestBody(mime.toMediaTypeOrNull())
        body.addFormDataPart("profile_photo", "upload.$ext", partBody)
        val url = BuildConfig.BASE_URL.trimEnd('/') + "/api/profile"
        val req = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .post(body.build())
            .build()
        val resp = client.newCall(req).execute()
        val raw = resp.body?.string().orEmpty()
        if (!resp.isSuccessful) {
            throw IllegalStateException(parseJsonMessage(raw) ?: "Upload failed (${resp.code})")
        }
        return parseJsonMessage(raw) ?: "Profile updated successfully."
    }

    private fun parseJsonMessage(raw: String): String? {
        return try {
            val key = "\"message\""
            val i = raw.indexOf(key)
            if (i < 0) return null
            val colon = raw.indexOf(':', i)
            val start = raw.indexOf('"', colon + 1)
            val end = if (start >= 0) raw.indexOf('"', start + 1) else -1
            if (start >= 0 && end > start) raw.substring(start + 1, end) else null
        } catch (_: Exception) {
            null
        }
    }
}
