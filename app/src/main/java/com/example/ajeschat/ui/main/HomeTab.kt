package com.example.ajeschat.ui.main

import androidx.compose.foundation.layout.Arrangement
import com.example.ajeschat.ui.theme.ajesScreenBackground
import com.example.ajeschat.ui.theme.ajesTextButtonColors
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.data.MobileRepository
import com.example.ajeschat.data.MobileSummaryData
import com.example.ajeschat.data.SectionInviteRow
import com.example.ajeschat.data.StaffRepository

@Composable
fun HomeTab(
    onRefreshNotificationBadge: () -> Unit
) {
    val repo = remember { MobileRepository(ApiModule.getMobileApi()) }
    val staffRepo = remember { StaffRepository(ApiModule.getStaffApi()) }
    val scope = rememberCoroutineScope()
    var nonce by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var summary by remember { mutableStateOf<MobileSummaryData?>(null) }
    var inviteActionError by remember { mutableStateOf<String?>(null) }
    var busyInviteId by remember { mutableIntStateOf(0) }

    LaunchedEffect(nonce) {
        loading = true
        error = null
        repo.summary()
            .onSuccess {
                summary = it
                error = null
            }
            .onFailure { e ->
                val raw = e.message.orEmpty()
                error = when {
                    raw.contains("401", ignoreCase = true) ||
                        raw.contains("Unauthorized", ignoreCase = true) ||
                        raw.contains("Session expired", ignoreCase = true) ->
                        "Could not load dashboard. Please log out from Profile, then sign in again."
                    else -> raw.ifBlank { "Failed to load home" }
                }
            }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .ajesScreenBackground()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            error != null -> {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { nonce++ }, colors = ajesTextButtonColors()) { Text("Retry") }
            }
            summary != null -> {
                val s = summary!!
                val d = s.dashboard
                val roleUpper = s.role?.uppercase().orEmpty()
                val sectionInvites: List<SectionInviteRow> = s.sectionInvites.ifEmpty {
                    d?.sectionInvites.orEmpty()
                }

                fun reloadHome() {
                    nonce++
                    onRefreshNotificationBadge()
                }

                fun acceptInvite(assignmentId: Int) {
                    if (assignmentId < 1) return
                    busyInviteId = assignmentId
                    inviteActionError = null
                    scope.launch {
                        staffRepo.acceptSectionInvite(assignmentId)
                            .onSuccess { reloadHome() }
                            .onFailure { inviteActionError = it.message ?: "Could not accept invite" }
                        busyInviteId = 0
                    }
                }

                fun declineInvite(assignmentId: Int) {
                    if (assignmentId < 1) return
                    busyInviteId = assignmentId
                    inviteActionError = null
                    scope.launch {
                        staffRepo.declineSectionInvite(assignmentId)
                            .onSuccess { reloadHome() }
                            .onFailure { inviteActionError = it.message ?: "Could not decline invite" }
                        busyInviteId = 0
                    }
                }

                if (d != null) {
                    DashboardWelcomeCard(s.name ?: "User", d.welcomeLine)
                    if (roleUpper == "TEACHER" && sectionInvites.isNotEmpty()) {
                        DashboardSectionInvitesCard(
                            invites = sectionInvites,
                            actionError = inviteActionError,
                            busyAssignmentId = busyInviteId.takeIf { it > 0 },
                            onAccept = { acceptInvite(it) },
                            onDecline = { declineInvite(it) }
                        )
                    }
                    d.kpis?.takeIf { it.isNotEmpty() }?.let { DashboardKpiStack(it) }
                    DashboardQuickHighlightsCard(dashboardHighlights(d.variant))

                    when (d.variant) {
                        "admin" -> {
                            d.announcementPeriodCounts?.takeIf { it.isNotEmpty() }?.let {
                                DashboardAnnouncementPeriodRow(it)
                            }
                            d.activityChart?.takeIf { it.isNotEmpty() }?.let {
                                DashboardActivityBars(it)
                            }
                            DashboardRecentAnnouncementsCard(
                                title = "Recent announcements",
                                rows = d.recentAnnouncements.orEmpty(),
                                colAuthor = "Author",
                                colDate = "Date",
                                showStatus = true
                            )
                            DashboardUpdatesCard(
                                "Updates",
                                listOf(
                                    "📢" to "Recent posts and activity overview match the AJES admin dashboard.",
                                    "📊" to "Use News for filters and edits; full charts stay on the website."
                                )
                            )
                        }
                        "teacher" -> {
                            val sectionsN = d.kpis?.getOrNull(0)?.value?.toIntOrNull()
                            val unreadN = d.kpis?.getOrNull(1)?.value?.toIntOrNull()
                            val activeN = d.kpis?.getOrNull(2)?.value?.toIntOrNull()
                            DashboardTeacherQuickStats(
                                activeAnn = activeN,
                                unread = unreadN,
                                sections = sectionsN,
                                recordsToday = d.recordsUpdatedToday
                            )
                            DashboardRecentAnnouncementsCard(
                                title = "Recent announcements (for your sections)",
                                rows = d.recentAnnouncements.orEmpty(),
                                colAuthor = "Section",
                                colDate = "Date",
                                showStatus = true
                            )
                            DashboardTeacherMessagesCard(d.recentMessages.orEmpty())
                            DashboardSectionActivityCard(d.sectionActivity.orEmpty())
                        }
                        "student" -> {
                            DashboardStudentSectionCard(d.section)
                            if (d.hasSection == true) {
                                DashboardStudentTeachersCard(d.teachersBySubject)
                                DashboardStudentClassmatesCard(d.classmates)
                            }
                            DashboardMessagesCallout()
                            d.kpis?.takeIf { it.isNotEmpty() }?.let { DashboardKpiStack(it) }
                            DashboardStudentUpdates()
                            DashboardRecentAnnouncementsCard(
                                title = "Recent announcements (your grade & section)",
                                rows = d.recentAnnouncements.orEmpty(),
                                colAuthor = "From",
                                colDate = "Date",
                                showStatus = false
                            )
                            DashboardGraphPlaceholder(
                                "Your activity",
                                "Announcements read this week — full analytics on AJES web."
                            )
                        }
                        "leadership" -> {
                            DashboardLeadershipUpdates(d)
                            DashboardRecentAnnouncementsCard(
                                title = "Recent announcements",
                                rows = d.recentAnnouncements.orEmpty(),
                                colAuthor = "From",
                                colDate = "When",
                                showStatus = true
                            )
                            DashboardGraphPlaceholder(
                                "Announcement activity",
                                "Charts for announcements per day stay on the AJES website."
                            )
                        }
                        "announcer" -> {
                            DashboardRecentAnnouncementsCard(
                                title = "Recent announcements",
                                rows = d.recentAnnouncements.orEmpty(),
                                colAuthor = "Target",
                                colDate = "When",
                                showStatus = true
                            )
                            DashboardUpdatesCard(
                                "Updates",
                                listOf(
                                    "📢" to "Delivery and read-rate details are on the AJES announcer dashboard.",
                                    "📅" to "Scheduled sends are managed in News on the website."
                                )
                            )
                            DashboardGraphPlaceholder(
                                "Delivery overview",
                                "Chart: announcements sent vs read — connect on AJES web."
                            )
                        }
                        "guidance" -> {
                            DashboardUpdatesCard(
                                "Guidance overview",
                                listOf(
                                    "📁" to "Manage counseling-related records on the AJES website.",
                                    "📢" to "Student-facing announcements appear in News.",
                                    "💬" to "Coordinate with teachers using Chats."
                                )
                            )
                            DashboardGraphPlaceholder(
                                "Records overview",
                                "Chart: sessions & records over time — full view on AJES web."
                            )
                        }
                        else -> {
                            Text(
                                "Use the tabs below for Chats, News, and Alerts.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                } else {
                    if (roleUpper == "TEACHER" && sectionInvites.isNotEmpty()) {
                        DashboardSectionInvitesCard(
                            invites = sectionInvites,
                            actionError = inviteActionError,
                            busyAssignmentId = busyInviteId.takeIf { it > 0 },
                            onAccept = { acceptInvite(it) },
                            onDecline = { declineInvite(it) }
                        )
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(s.name ?: "User", style = MaterialTheme.typography.titleMedium)
                            Text("Role: ${s.role ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Unread notifications: ${s.unreadNotifications}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Update the AJES server for api/mobile/summary with dashboard data to see the same home layout as the web.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

            }
        }
    }
}
