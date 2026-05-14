package com.example.ajeschat.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET

interface MobileApi {
    @GET("api/mobile/summary")
    suspend fun summary(): Response<MobileSummaryEnvelope>
}

data class MobileSummaryEnvelope(
    @SerializedName("status") val status: String? = null,
    @SerializedName("data") val data: MobileSummaryData? = null
)

data class MobileSummaryData(
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("role") val role: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("unread_notifications") val unreadNotifications: Int = 0,
    @SerializedName("can_manage_announcements") val canManageAnnouncements: Boolean = false,
    @SerializedName("teacher_sections") val teacherSections: Boolean = false,
    @SerializedName("records") val records: Boolean = false,
    @SerializedName("chat_logs") val chatLogs: Boolean = false,
    @SerializedName("user_management_read") val userManagementRead: Boolean = false,
    @SerializedName("sections_read") val sectionsRead: Boolean = false,
    @SerializedName("system_settings") val systemSettings: Boolean = false,
    @SerializedName("chatbot_management") val chatbotManagement: Boolean = false,
    @SerializedName("backup_restore") val backupRestore: Boolean = false,
    @SerializedName("security_logs") val securityLogs: Boolean = false,
    @SerializedName("dashboard") val dashboard: MobileDashboardBundle? = null
)

data class MobileDashboardBundle(
    @SerializedName("variant") val variant: String? = null,
    @SerializedName("welcome_line") val welcomeLine: String? = null,
    @SerializedName("kpis") val kpis: List<MobileDashboardKpi>? = null,
    @SerializedName("recent_announcements") val recentAnnouncements: List<MobileDashboardAnnRow>? = null,
    @SerializedName("recent_messages") val recentMessages: List<MobileDashboardMsgRow>? = null,
    @SerializedName("section_activity") val sectionActivity: List<MobileDashboardSectionRow>? = null,
    @SerializedName("activity_chart") val activityChart: List<MobileDashboardBarDay>? = null,
    @SerializedName("announcement_period_counts") val announcementPeriodCounts: Map<String, Number>? = null,
    @SerializedName("section_label") val sectionLabel: String? = null,
    @SerializedName("has_section") val hasSection: Boolean? = null,
    @SerializedName("records_updated_today") val recordsUpdatedToday: Int? = null,
    @SerializedName("active_mine") val activeMine: Int? = null
)

data class MobileDashboardKpi(
    @SerializedName("title") val title: String? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("meta") val meta: String? = null,
    @SerializedName("progress_pct") val progressPct: Int? = null
)

data class MobileDashboardAnnRow(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val title: String? = null,
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("meta") val meta: String? = null,
    @SerializedName("status") val status: String? = null
)

data class MobileDashboardMsgRow(
    @SerializedName("text") val text: String? = null,
    @SerializedName("time_ago") val timeAgo: String? = null
)

data class MobileDashboardSectionRow(
    @SerializedName("section_name") val sectionName: String? = null,
    @SerializedName("grade_level") val gradeLevel: String? = null,
    @SerializedName("student_count") val studentCount: Int = 0
)

data class MobileDashboardBarDay(
    @SerializedName("label") val label: String? = null,
    @SerializedName("count") val count: Int = 0
)
