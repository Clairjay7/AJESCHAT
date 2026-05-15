package com.example.ajeschat.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AnnouncementApi {
    @GET("api/announcements")
    suspend fun getAnnouncements(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<AnnouncementsResponse>

    @POST("api/announcements")
    suspend fun createAnnouncement(@Body body: AnnouncementCreateRequest): Response<AnnouncementCreateResponse>

    @POST("api/announcements/update/{id}")
    suspend fun updateAnnouncement(
        @Path("id") id: Int,
        @Body body: AnnouncementUpdateBody
    ): Response<AnnouncementCreateResponse>

    @POST("api/announcements/delete/{id}")
    suspend fun deleteAnnouncement(@Path("id") id: Int): Response<AnnouncementCreateResponse>
}

data class AnnouncementsResponse(
    val announcements: List<AnnouncementItem> = emptyList(),
    @SerializedName("can_manage") val canManage: Boolean = false,
    val role: String? = null,
    @SerializedName("audience_options") val audienceOptions: Map<String, String>? = null,
    @SerializedName("teacher_sections") val teacherSections: List<TeacherSectionOption>? = null
)

data class AnnouncementItem(
    val id: Int,
    val title: String,
    val body: String,
    val created_at: String? = null,
    val audience_type: String? = null,
    val created_by_name: String? = null
)

data class TeacherSectionOption(
    val id: Int,
    val display_label: String
)

data class AnnouncementCreateRequest(
    val title: String,
    val body: String,
    val section_id: Int? = null,
    val audience_type: String? = null
)

data class AnnouncementCreateResponse(
    val status: String? = null,
    val message: String? = null
)

data class AnnouncementUpdateBody(
    val title: String,
    val body: String
)
