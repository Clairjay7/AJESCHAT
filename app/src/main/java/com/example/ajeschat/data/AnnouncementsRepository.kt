package com.example.ajeschat.data

data class AnnouncementsPage(
    val announcements: List<AnnouncementItem>,
    val canManage: Boolean,
    val role: String,
    val audienceOptions: Map<String, String>,
    val teacherSections: List<TeacherSectionOption>
)

class AnnouncementsRepository(
    private val api: AnnouncementApi
) {
    suspend fun loadPage(
        dateFrom: String? = null,
        dateTo: String? = null
    ): Result<AnnouncementsPage> {
        return runCatching {
            val response = api.getAnnouncements(
                dateFrom = dateFrom?.takeIf { it.isNotBlank() },
                dateTo = dateTo?.takeIf { it.isNotBlank() }
            )
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to load announcements (${response.code()})")
            }
            val body = response.body()
            AnnouncementsPage(
                announcements = body?.announcements ?: emptyList(),
                canManage = body?.canManage == true,
                role = body?.role?.trim().orEmpty(),
                audienceOptions = body?.audienceOptions ?: emptyMap(),
                teacherSections = body?.teacherSections ?: emptyList()
            )
        }
    }

    suspend fun create(
        title: String,
        body: String,
        sectionId: Int?,
        audienceType: String?
    ): Result<String> {
        return runCatching {
            val res = api.createAnnouncement(
                AnnouncementCreateRequest(
                    title = title.trim(),
                    body = body.trim(),
                    section_id = sectionId,
                    audience_type = audienceType
                )
            )
            if (!res.isSuccessful) {
                throw IllegalStateException(res.body()?.message ?: "Failed to publish (${res.code()})")
            }
            res.body()?.message ?: "Announcement published."
        }
    }

    suspend fun update(id: Int, title: String, body: String): Result<String> {
        return runCatching {
            val res = api.updateAnnouncement(id, AnnouncementUpdateBody(title.trim(), body.trim()))
            if (!res.isSuccessful) {
                throw IllegalStateException(res.body()?.message ?: "Update failed (${res.code()})")
            }
            res.body()?.message ?: "Updated."
        }
    }

    suspend fun delete(id: Int): Result<String> {
        return runCatching {
            val res = api.deleteAnnouncement(id)
            if (!res.isSuccessful) {
                throw IllegalStateException(res.body()?.message ?: "Delete failed (${res.code()})")
            }
            res.body()?.message ?: "Deleted."
        }
    }
}
