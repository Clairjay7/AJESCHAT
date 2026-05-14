package com.example.ajeschat.data

import retrofit2.HttpException

class NotificationsRepository(private val api: NotificationsApi) {
    suspend fun list(): Result<List<NotificationItem>> = runCatching {
        val res = api.list()
        if (!res.isSuccessful) throw HttpException(res)
        res.body()?.items ?: emptyList()
    }

    suspend fun markRead(id: Int): Result<Unit> = runCatching {
        val res = api.markRead(NotificationMarkReadRequest(id))
        if (!res.isSuccessful) throw HttpException(res)
    }

    suspend fun markAllRead(): Result<Unit> = runCatching {
        val res = api.markAllRead()
        if (!res.isSuccessful) throw HttpException(res)
    }
}

class MobileRepository(private val api: MobileApi) {
    suspend fun summary(): Result<MobileSummaryData> = runCatching {
        val res = api.summary()
        if (!res.isSuccessful) throw HttpException(res)
        res.body()?.data ?: throw IllegalStateException("No summary")
    }
}

class StaffRepository(private val api: StaffApi) {
    suspend fun adminUsers(): Result<List<AdminUserRow>> = runCatching {
        val r = api.adminUsers()
        if (!r.isSuccessful) throw HttpException(r)
        r.body()?.items ?: emptyList()
    }

    suspend fun adminSections(): Result<List<AdminSectionRow>> = runCatching {
        val r = api.adminSections()
        if (!r.isSuccessful) throw HttpException(r)
        r.body()?.items ?: emptyList()
    }

    suspend fun teacherHub(): Result<TeacherHubResponse> = runCatching {
        val r = api.teacherHub()
        if (!r.isSuccessful) throw HttpException(r)
        r.body() ?: TeacherHubResponse()
    }

    suspend fun records(page: Int): Result<RecordsListResponse> = runCatching {
        val r = api.records(page)
        if (!r.isSuccessful) throw HttpException(r)
        r.body() ?: RecordsListResponse()
    }

    suspend fun chatLogs(page: Int): Result<ChatLogsResponse> = runCatching {
        val r = api.chatLogs(page = page)
        if (!r.isSuccessful) throw HttpException(r)
        r.body() ?: ChatLogsResponse()
    }

    suspend fun sysadminModules(): Result<List<SysadminModule>> = runCatching {
        val r = api.sysadminModules()
        if (!r.isSuccessful) throw HttpException(r)
        r.body()?.modules ?: emptyList()
    }
}
