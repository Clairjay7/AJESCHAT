package com.example.ajeschat.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ajeschat.data.MobileDashboardAnnRow
import com.example.ajeschat.data.MobileDashboardBarDay
import com.example.ajeschat.data.MobileDashboardBundle
import com.example.ajeschat.data.MobileDashboardKpi
import com.example.ajeschat.data.MobileDashboardMsgRow
import com.example.ajeschat.data.MobileDashboardSectionRow

internal data class DashboardHighlight(
    val emoji: String,
    val title: String,
    val subtitle: String
)

internal fun dashboardHighlights(variant: String?): List<DashboardHighlight> {
    return when (variant) {
        "admin" -> listOf(
            DashboardHighlight("📊", "Overview", "User counts and announcements mirror the AJES admin dashboard."),
            DashboardHighlight("📢", "Announcements", "Use News to create or review posts; filters match period cards on the web."),
            DashboardHighlight("✓", "Modules", "Chat, records, and users stay in sync with the main AJES site.")
        )
        "teacher" -> listOf(
            DashboardHighlight("👩‍🏫", "Sections", "Open My teaching to see classes and invitations like on the web."),
            DashboardHighlight("📢", "Announcements", "Posts for your scope appear in News and in the table below."),
            DashboardHighlight("💬", "Messages", "Unread counts match Chat; open Chats to reply.")
        )
        "student" -> listOf(
            DashboardHighlight("📢", "Announcements", "Section and school-wide posts show in News."),
            DashboardHighlight("💬", "Messages", "Teachers and guidance reach you in Chats."),
            DashboardHighlight("📅", "Stay updated", "Alerts tab shows your notification center.")
        )
        "leadership" -> listOf(
            DashboardHighlight("📢", "School pulse", "Weekly announcement totals mirror the principal-style overview."),
            DashboardHighlight("👥", "Staff", "Use Chat logs and Records tools when your role allows."),
            DashboardHighlight("🔔", "Alerts", "Unread alerts reflect the notifications bell on AJES.")
        )
        "announcer" -> listOf(
            DashboardHighlight("📢", "Publishing", "Draft and post flow matches the announcer dashboard on the web."),
            DashboardHighlight("📝", "Drafts", "Finish drafts in News before sending to the school."),
            DashboardHighlight("✉️", "Reach", "Target school-wide or sections like on desktop AJES.")
        )
        "guidance" -> listOf(
            DashboardHighlight("📁", "Records", "Records activity counts mirror the guidance KPIs on the web."),
            DashboardHighlight("📢", "Announcements", "School posts relevant to students appear in News."),
            DashboardHighlight("💬", "Coordination", "Chat with teachers from the Chats tab.")
        )
        else -> listOf(
            DashboardHighlight("🏠", "AJES Chat", "Home shows a compact view of your AJES dashboard."),
            DashboardHighlight("📢", "News", "School announcements are in the News tab."),
            DashboardHighlight("🔔", "Alerts", "System messages are in Alerts.")
        )
    }
}

@Composable
internal fun DashboardWelcomeCard(name: String, welcomeLine: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Welcome back, $name!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                welcomeLine ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
internal fun DashboardKpiStack(kpis: List<MobileDashboardKpi>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        kpis.take(3).forEachIndexed { index, k ->
            val tint = when (index % 3) {
                0 -> MaterialTheme.colorScheme.primaryContainer
                1 -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.55f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(k.title ?: "", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(k.value ?: "—", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(k.meta ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val pct = (k.progressPct ?: 0).coerceIn(0, 100) / 100f
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    )
                }
            }
        }
    }
}

@Composable
internal fun DashboardQuickHighlightsCard(highlights: List<DashboardHighlight>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Quick highlights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            highlights.forEach { h ->
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                    Text(h.emoji, modifier = Modifier.padding(end = 10.dp))
                    Column {
                        Text(h.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(h.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
internal fun DashboardActivityBars(chart: List<MobileDashboardBarDay>) {
    if (chart.isEmpty()) return
    val maxC = chart.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Activity overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Announcements created per day (last 7 days)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                chart.forEach { d ->
                    val hFrac = d.count.toFloat() / maxC
                    val barH = (100f * hFrac).coerceIn(6f, 100f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(barH.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(d.label ?: "", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${d.count}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
internal fun DashboardAnnouncementPeriodRow(counts: Map<String, Number>?) {
    if (counts.isNullOrEmpty()) return
    val order = listOf("all", "today", "yesterday", "week", "month")
    val labels = mapOf(
        "all" to "All recent",
        "today" to "Today",
        "yesterday" to "Yesterday",
        "week" to "Last week",
        "month" to "Last month"
    )
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("Announcement history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Counts by period (same labels as AJES admin dashboard)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                order.forEach { key ->
                    val n = counts[key]?.toDouble()?.toInt() ?: return@forEach
                    val title = labels[key] ?: key
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        tonalElevation = 1.dp
                    ) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text("$n", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DashboardRecentAnnouncementsCard(
    title: String,
    rows: List<MobileDashboardAnnRow>,
    colAuthor: String,
    colDate: String,
    showStatus: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("Title", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(2f))
                Text(colAuthor, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(colDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (showStatus) {
                    Text("Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(64.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
            if (rows.isEmpty()) {
                Text("No announcements to show.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                rows.forEach { row ->
                    Column(Modifier.padding(vertical = 8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Text(
                                row.title ?: "—",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(2f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                row.subtitle ?: "—",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1.1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                row.meta ?: "—",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (showStatus) {
                                Text(
                                    row.status ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.width(64.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
internal fun DashboardUpdatesCard(title: String, lines: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            lines.forEach { (emoji, text) ->
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                    Text(emoji, modifier = Modifier.padding(end = 8.dp))
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
internal fun DashboardTeacherMessagesCard(messages: List<MobileDashboardMsgRow>) {
    val lines = if (messages.isEmpty()) {
        listOf("💬" to "No chat messages yet. Open Chat to start a conversation.")
    } else {
        messages.map { m -> (if ((m.text ?: "").startsWith("You →")) "📤" else "💬") to "${m.text ?: ""}\n${m.timeAgo ?: ""}" }
    }
    DashboardUpdatesCard("Recent messages", lines)
}

@Composable
internal fun DashboardSectionActivityCard(sections: List<MobileDashboardSectionRow>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("Section activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            if (sections.isEmpty()) {
                Text(
                    "No sections assigned yet. Accept invitations under My teaching when your admin adds you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sections.forEach { s ->
                    Column(Modifier.padding(vertical = 8.dp)) {
                        Row {
                            Text(s.sectionName ?: "Section", fontWeight = FontWeight.SemiBold)
                            if (!s.gradeLevel.isNullOrBlank()) {
                                Text(" · Grade ${s.gradeLevel}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        val n = s.studentCount
                        Text(
                            "$n student${if (n == 1) "" else "s"} in this section",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
internal fun DashboardTeacherQuickStats(
    activeAnn: Int?,
    unread: Int?,
    sections: Int?,
    recordsToday: Int?
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("Quick stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DashboardStatCell(Modifier.weight(1f), "Active ann.", "${activeAnn ?: "—"}")
                DashboardStatCell(Modifier.weight(1f), "Unread", "${unread ?: "—"}")
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DashboardStatCell(Modifier.weight(1f), "Sections", "${sections ?: "—"}")
                DashboardStatCell(Modifier.weight(1f), "Activity today", "${recordsToday ?: "—"}")
            }
        }
    }
}

@Composable
private fun RowScope.DashboardStatCell(modifier: Modifier, label: String, value: String) {
    Surface(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun DashboardLeadershipUpdates(bundle: MobileDashboardBundle) {
    val ann = bundle.recentAnnouncements?.firstOrNull()?.title ?: "School announcements"
    val lines = listOf(
        "📢" to "Latest: $ann",
        "👥" to "Use Records and Chat logs from shortcuts when available.",
        "💬" to "Open Chats to follow up with staff and students."
    )
    DashboardUpdatesCard("Updates", lines)
}

@Composable
internal fun DashboardStudentUpdates() {
    DashboardUpdatesCard(
        "Updates",
        listOf(
            "📢" to "New announcements appear in News and below.",
            "👩‍🏫" to "Teachers may message you about class work — check Chats.",
            "💬" to "Guidance posts schedules in announcements and Chat."
        )
    )
}

@Composable
internal fun DashboardMessagesCallout(onOpenChats: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("Messages", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Messages from your teachers and the guidance office appear in Chats.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            FilledTonalButton(onClick = onOpenChats) {
                Text("Open Chats")
            }
        }
    }
}

@Composable
internal fun DashboardAnnouncerQuickAction(onOpenNews: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("Quick action", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Create a new announcement to send to students, teachers, or the whole school.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
            FilledTonalButton(onClick = onOpenNews) {
                Text("Open News")
            }
        }
    }
}

@Composable
internal fun DashboardVicePrincipalQuickAccess(
    onOpenNews: () -> Unit,
    onOpenChats: () -> Unit,
    onOpenRecordsTool: () -> Unit,
    onOpenChatLogsTool: () -> Unit,
    showRecords: Boolean,
    showChatLogs: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("Quick access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Announcements, reports, chat monitoring, and messaging — same focus as the AJES vice-principal dashboard.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onOpenNews) { Text("News") }
                FilledTonalButton(onClick = onOpenChats) { Text("Chats") }
                if (showRecords) {
                    FilledTonalButton(onClick = onOpenRecordsTool) { Text("Records") }
                }
                if (showChatLogs) {
                    FilledTonalButton(onClick = onOpenChatLogsTool) { Text("Chat logs") }
                }
            }
        }
    }
}

@Composable
internal fun DashboardGraphPlaceholder(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
