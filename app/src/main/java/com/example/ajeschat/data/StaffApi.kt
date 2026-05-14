package com.example.ajeschat.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StaffApi {
    @GET("api/admin/users")
    suspend fun adminUsers(): Response<AdminUsersResponse>

    @GET("api/admin/sections")
    suspend fun adminSections(): Response<AdminSectionsResponse>

    @GET("api/teacher/hub")
    suspend fun teacherHub(): Response<TeacherHubResponse>

    @GET("api/records/list")
    suspend fun records(@Query("page") page: Int): Response<RecordsListResponse>

    @GET("api/chat/logs")
    suspend fun chatLogs(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20
    ): Response<ChatLogsResponse>

    @GET("api/sysadmin/modules")
    suspend fun sysadminModules(): Response<SysadminModulesResponse>
}

data class AdminUsersResponse(@SerializedName("items") val items: List<AdminUserRow> = emptyList())
data class AdminUserRow(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String? = null,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?
)

data class AdminSectionsResponse(@SerializedName("items") val items: List<AdminSectionRow> = emptyList())
data class AdminSectionRow(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("grade_level") val gradeLevel: String?
)

data class TeacherHubResponse(
    @SerializedName("invites") val invites: List<TeacherHubRow> = emptyList(),
    @SerializedName("sections") val sections: List<TeacherHubRow> = emptyList()
)

data class TeacherHubRow(
    @SerializedName("assignment_id") val assignmentId: Int = 0,
    @SerializedName("section_id") val sectionId: Int = 0,
    @SerializedName("section_name") val sectionName: String? = null,
    @SerializedName("grade_level") val gradeLevel: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("assignment_role") val assignmentRole: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null
)

data class RecordsListResponse(
    @SerializedName("items") val items: List<RecordRow> = emptyList(),
    @SerializedName("page") val page: Int = 1,
    @SerializedName("total_pages") val totalPages: Int = 1
)

data class RecordRow(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String?,
    @SerializedName("details") val details: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class ChatLogsResponse(
    @SerializedName("items") val items: List<ChatLogRow> = emptyList(),
    @SerializedName("page") val page: Int = 1,
    @SerializedName("total_pages") val totalPages: Int = 1,
    @SerializedName("message") val message: String? = null
)

data class ChatLogRow(
    @SerializedName("id") val id: Int,
    @SerializedName("sender_name") val senderName: String?,
    @SerializedName("receiver_name") val receiverName: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class SysadminModulesResponse(
    @SerializedName("modules") val modules: List<SysadminModule> = emptyList()
)

data class SysadminModule(
    @SerializedName("key") val key: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("web_url") val webUrl: String?
)
