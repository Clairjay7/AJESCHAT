package com.example.ajeschat.ui.main

import androidx.compose.foundation.layout.Arrangement
import com.example.ajeschat.ui.theme.ajesScreenBackground
import com.example.ajeschat.ui.theme.ajesTextButtonColors
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.data.MobileRepository
import com.example.ajeschat.data.MobileSummaryData

@Composable
fun HomeTab(
    navController: NavHostController,
    onRefreshNotificationBadge: () -> Unit,
    onOpenChats: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onOpenAlerts: () -> Unit = {}
) {
    val repo = remember { MobileRepository(ApiModule.getMobileApi()) }
    var nonce by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var summary by remember { mutableStateOf<MobileSummaryData?>(null) }

    LaunchedEffect(nonce) {
        loading = true
        error = null
        repo.summary()
            .onSuccess {
                summary = it
                error = null
            }
            .onFailure { e ->
                error = e.message ?: "Failed to load home"
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
        Text(
            "Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
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

                if (d != null) {
                    DashboardWelcomeCard(s.name ?: "User", d.welcomeLine)
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
                            DashboardMessagesCallout(onOpenChats)
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
                            if (roleUpper == "VICE_PRINCIPAL" || roleUpper == "HEAD_TEACHER") {
                                DashboardVicePrincipalQuickAccess(
                                    onOpenNews = onOpenNews,
                                    onOpenChats = onOpenChats,
                                    onOpenRecordsTool = {
                                        navController.navigate("tools/records")
                                        onRefreshNotificationBadge()
                                    },
                                    onOpenChatLogsTool = {
                                        navController.navigate("tools/chat_logs")
                                        onRefreshNotificationBadge()
                                    },
                                    showRecords = s.records,
                                    showChatLogs = s.chatLogs
                                )
                            }
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
                            DashboardAnnouncerQuickAction(onOpenNews)
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
                                    "📁" to "Manage counseling-related records; open Records from shortcuts below when available.",
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

                    Spacer(Modifier.height(4.dp))
                    Text("Go to", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(onClick = onOpenChats, modifier = Modifier.weight(1f)) { Text("Chats") }
                        FilledTonalButton(onClick = onOpenNews, modifier = Modifier.weight(1f)) { Text("News") }
                        FilledTonalButton(onClick = onOpenAlerts, modifier = Modifier.weight(1f)) { Text("Alerts") }
                    }
                } else {
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

                Text("Shortcuts", style = MaterialTheme.typography.titleMedium)
                if (s.userManagementRead) {
                    ToolNavButton("Users (admin)", "tools/admin_users", navController, onRefreshNotificationBadge)
                }
                if (s.sectionsRead) {
                    ToolNavButton("Sections (admin)", "tools/admin_sections", navController, onRefreshNotificationBadge)
                }
                if (s.teacherSections) {
                    ToolNavButton("My teaching", "tools/teacher", navController, onRefreshNotificationBadge)
                }
                if (s.records) {
                    ToolNavButton("Records", "tools/records", navController, onRefreshNotificationBadge)
                }
                if (s.chatLogs) {
                    ToolNavButton("Chat logs", "tools/chat_logs", navController, onRefreshNotificationBadge)
                }
                if (s.systemSettings || s.chatbotManagement || s.backupRestore || s.securityLogs) {
                    ToolNavButton("System admin (links)", "tools/sysadmin", navController, onRefreshNotificationBadge)
                }
                Text(
                    "Dangerous or full workflows stay in the browser; this app opens read-only lists and links where noted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ToolNavButton(
    label: String,
    route: String,
    navController: NavHostController,
    onRefreshNotificationBadge: () -> Unit
) {
    Button(
        onClick = {
            navController.navigate(route)
            onRefreshNotificationBadge()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label)
    }
}
