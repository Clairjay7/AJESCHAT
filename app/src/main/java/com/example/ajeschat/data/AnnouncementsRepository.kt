package com.example.ajeschat.data

data class AnnouncementsPage(
    val announcements: List<AnnouncementItem>,
    val canManage: Boolean,
    val canCreate: Boolean,
    val role: String,
    val audienceOptions: Map<String, String>,
    val teacherSections: List<TeacherSectionOption>
)

class AnnouncementsRepository(
    private val api: AnnouncementApi,
    private val staffApi: StaffApi = ApiModule.getStaffApi()
) {
    suspend fun loadPage(
        dateFrom: String? = null,
        dateTo: String? = null,
        search: String? = null,
        period: String? = null,
        date: String? = null
    ): Result<AnnouncementsPage> {
        return runCatching {
            val response = api.getAnnouncements(
                dateFrom = dateFrom?.takeIf { it.isNotBlank() },
                dateTo = dateTo?.takeIf { it.isNotBlank() },
                search = search?.trim()?.takeIf { it.isNotBlank() },
                period = period?.trim()?.takeIf { it.isNotBlank() && it != "all" },
                date = date?.trim()?.takeIf { it.isNotBlank() }
            )
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to load announcements (${response.code()})")
            }
            val body = response.body()
            val canManage = body?.canManage == true
            val role = body?.role?.trim().orEmpty()
            var teacherSections = body?.teacherSections ?: emptyList()
            if (role.uppercase() == "TEACHER" && teacherSections.isEmpty()) {
                teacherSections = loadTeacherSectionsFromHub()
            }
            val canCreate = body?.canCreate == true
                || (canManage && (role.uppercase() != "TEACHER" || teacherSections.isNotEmpty()))
            AnnouncementsPage(
                announcements = body?.announcements ?: emptyList(),
                canManage = canManage,
                canCreate = canCreate,
                role = role,
                audienceOptions = body?.audienceOptions ?: emptyMap(),
                teacherSections = teacherSections
            )
        }
    }

    private suspend fun loadTeacherSectionsFromHub(): List<TeacherSectionOption> {
        val hub = staffApi.teacherHub()
        if (!hub.isSuccessful) {
            return emptyList()
        }
        val rows = hub.body()?.sections.orEmpty()
        return rows
            .filter { it.sectionId > 0 }
            .distinctBy { it.sectionId }
            .map { row ->
                val name = row.sectionName?.trim().orEmpty()
                val grade = row.gradeLevel?.trim().orEmpty()
                val subject = row.subjectName?.trim().orEmpty()
                val label = buildString {
                    if (grade.isNotEmpty() && name.isNotEmpty()) {
                        append("$grade - $name")
                    } else if (name.isNotEmpty()) {
                        append(name)
                    } else {
                        append("Section #${row.sectionId}")
                    }
                    if (subject.isNotEmpty()) {
                        append(" ($subject)")
                    }
                }
                TeacherSectionOption(id = row.sectionId, display_label = label)
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
