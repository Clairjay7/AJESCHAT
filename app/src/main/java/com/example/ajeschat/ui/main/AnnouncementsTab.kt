package com.example.ajeschat.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ajeschat.ui.theme.AjesGreen
import com.example.ajeschat.ui.theme.AjesOnGreen
import com.example.ajeschat.data.AnnouncementItem
import com.example.ajeschat.data.AnnouncementsPage
import com.example.ajeschat.data.AnnouncementsRepository
import com.example.ajeschat.data.TeacherSectionOption
import com.example.ajeschat.session.SessionHolder
import com.example.ajeschat.ui.theme.AjesCardShape
import com.example.ajeschat.ui.theme.ajesCardBorder
import com.example.ajeschat.ui.theme.AjesTextPrimary
import com.example.ajeschat.ui.theme.AjesTextSecondary
import com.example.ajeschat.ui.theme.ajesScreenBackground
import com.example.ajeschat.ui.theme.ajesTextButtonColors
import com.example.ajeschat.ui.theme.ajesTextFieldColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val DATE_PATTERN = Regex("^\\d{4}-\\d{2}-\\d{2}$")

private val ANNOUNCEMENT_PERIOD_OPTIONS = listOf(
    "all" to "All",
    "today" to "Today",
    "yesterday" to "Yesterday",
    "week" to "Last week",
    "month" to "Last month"
)

private enum class DatePickTarget { FROM, TO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsTab(
    repository: AnnouncementsRepository
) {
    var refreshKey by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf<AnnouncementsPage?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var selectedAnnouncement by remember { mutableStateOf<AnnouncementItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var periodFilter by remember { mutableStateOf("all") }
    var dateFrom by remember { mutableStateOf("") }
    var dateTo by remember { mutableStateOf("") }
    var datePickTarget by remember { mutableStateOf<DatePickTarget?>(null) }

    LaunchedEffect(refreshKey) {
        loading = true
        error = null
        val from = dateFrom.trim().takeIf { it.matches(DATE_PATTERN) }
        val to = dateTo.trim().takeIf { it.matches(DATE_PATTERN) }
        val hasDateRange = from != null || to != null
        val q = searchQuery.trim().takeIf { it.isNotBlank() }
        repository.loadPage(
            dateFrom = from,
            dateTo = to,
            search = q,
            period = if (!hasDateRange) periodFilter else null
        )
            .onSuccess {
                page = it
                error = null
            }
            .onFailure {
                error = it.message ?: "Failed to load announcements."
            }
        loading = false
    }

    val p = page
    val effectiveRole = p?.role?.takeIf { it.isNotBlank() } ?: SessionHolder.session?.role
    val roleUpper = effectiveRole?.trim()?.uppercase(Locale.US).orEmpty()
    val showAdd = p?.canCreate == true

    datePickTarget?.let { target ->
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { datePickTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = formatPickerDate(millis)
                        when (target) {
                            DatePickTarget.FROM -> dateFrom = picked
                            DatePickTarget.TO -> dateTo = picked
                        }
                        periodFilter = "all"
                    }
                    datePickTarget = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { datePickTarget = null }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Column(Modifier.fillMaxSize().ajesScreenBackground()) {
        AnnouncementsToolbar(
            showAdd = showAdd,
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            periodFilter = periodFilter,
            onPeriodChange = { key ->
                periodFilter = key
                dateFrom = ""
                dateTo = ""
                refreshKey++
            },
            dateFrom = dateFrom,
            dateTo = dateTo,
            onDateFromClick = { datePickTarget = DatePickTarget.FROM },
            onDateToClick = { datePickTarget = DatePickTarget.TO },
            onRefresh = { refreshKey++ },
            onAdd = { showCreate = true },
            onFilter = { refreshKey++ },
            onResetFilter = {
                searchQuery = ""
                dateFrom = ""
                dateTo = ""
                periodFilter = "all"
                refreshKey++
            }
        )
        HorizontalDivider(color = AjesTextSecondary.copy(alpha = 0.35f))
        when {
            loading -> {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AjesGreen)
                }
            }
            error != null -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error ?: "Error", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { refreshKey++ }, colors = ajesTextButtonColors()) {
                        Text("Retry", color = AjesTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            p != null && p.announcements.isEmpty() -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (searchQuery.isNotBlank() || dateFrom.isNotBlank() || dateTo.isNotBlank() || periodFilter != "all") {
                            "No announcements match your filter."
                        } else {
                            "No announcements yet."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = AjesTextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            p != null -> {
                val grouped = remember(p.announcements) { groupAnnouncementsByDate(p.announcements) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (dateHeader, items) ->
                        item(key = "hdr-$dateHeader") {
                            AnnouncementDateHeader(dateHeader)
                        }
                        items(items, key = { it.id }) { ann ->
                            AnnouncementCard(ann, onClick = { selectedAnnouncement = ann })
                        }
                    }
                }
            }
        }
    }

    if (showCreate && p != null) {
        CreateAnnouncementDialog(
            page = p,
            repository = repository,
            onDismiss = { showCreate = false },
            onPublished = { refreshKey++ }
        )
    }

    selectedAnnouncement?.let { ann ->
        AnnouncementDetailDialog(
            announcement = ann,
            canManage = p?.canManage == true,
            repository = repository,
            onDismiss = { selectedAnnouncement = null },
            onChanged = { refreshKey++ }
        )
    }
}

@Composable
private fun AnnouncementsToolbar(
    showAdd: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    periodFilter: String,
    onPeriodChange: (String) -> Unit,
    dateFrom: String,
    dateTo: String,
    onDateFromClick: () -> Unit,
    onDateToClick: () -> Unit,
    onRefresh: () -> Unit,
    onAdd: () -> Unit,
    onFilter: () -> Unit,
    onResetFilter: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onRefresh, colors = ajesTextButtonColors()) {
                Text("Refresh", color = AjesTextPrimary, fontWeight = FontWeight.SemiBold)
            }
            if (showAdd) {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AjesGreen,
                        contentColor = AjesOnGreen
                    )
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Add", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 8.dp)
                .ajesCardBorder(),
            shape = AjesCardShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = AjesTextPrimary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    "Filter",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AjesTextPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ANNOUNCEMENT_PERIOD_OPTIONS.forEach { (key, label) ->
                        FilterChip(
                            selected = periodFilter == key && dateFrom.isBlank() && dateTo.isBlank(),
                            onClick = { onPeriodChange(key) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AjesGreen,
                                selectedLabelColor = AjesOnGreen
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Search", color = AjesTextPrimary) },
                    placeholder = { Text("Title or message…", color = AjesTextSecondary) },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = AjesGreen)
                    },
                    colors = ajesTextFieldColors()
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DateFilterField(
                        label = "From",
                        value = dateFrom,
                        displayValue = dateFrom.ifBlank { "Tap to pick" },
                        onClick = onDateFromClick,
                        modifier = Modifier.weight(1f)
                    )
                    DateFilterField(
                        label = "To",
                        value = dateTo,
                        displayValue = dateTo.ifBlank { "Tap to pick" },
                        onClick = onDateToClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onFilter,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AjesGreen,
                            contentColor = AjesOnGreen
                        )
                    ) {
                        Text("Apply", fontWeight = FontWeight.SemiBold)
                    }
                    if (searchQuery.isNotBlank() || dateFrom.isNotBlank() || dateTo.isNotBlank() || periodFilter != "all") {
                        TextButton(onClick = onResetFilter, colors = ajesTextButtonColors()) {
                            Text("Reset", color = AjesTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementDateHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = AjesGreen,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp)
    )
}

private fun groupAnnouncementsByDate(items: List<AnnouncementItem>): List<Pair<String, List<AnnouncementItem>>> {
    return items
        .groupBy { formatAnnouncementDateHeader(it.created_at) }
        .entries
        .sortedByDescending { entry ->
            entry.value.firstOrNull()?.created_at?.let { announcementSortKey(it) }.orEmpty()
        }
        .map { it.key to it.value }
}

private fun announcementSortKey(createdAt: String): String {
    val raw = createdAt.trim()
    if (raw.length >= 10 && raw[4] == '-' && raw[7] == '-') {
        return raw.substring(0, 10)
    }
    return raw
}

private fun formatAnnouncementDateHeader(createdAt: String?): String {
    val key = createdAt?.trim()?.let { announcementSortKey(it) }.orEmpty()
    if (!key.matches(DATE_PATTERN)) {
        return "Other dates"
    }
    return runCatching {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key) ?: return "Other dates"
        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(parsed)
    }.getOrDefault(key)
}

private fun formatAnnouncementDateTime(createdAt: String?): String {
    val raw = createdAt?.trim().orEmpty()
    if (raw.isBlank()) return ""
    val key = announcementSortKey(raw)
    val datePart = if (key.matches(DATE_PATTERN)) {
        runCatching {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key)
            parsed?.let { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(it) } ?: key
        }.getOrDefault(key)
    } else {
        key
    }
    val timePart = if (raw.length >= 16) raw.substring(11, 16) else ""
    return if (timePart.isNotBlank()) "$datePart · $timePart" else datePart
}

@Composable
private fun DateFilterField(
    label: String,
    value: String,
    displayValue: String = value,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = AjesTextPrimary) },
            placeholder = { Text("All dates", color = AjesTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = ajesTextFieldColors()
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

private fun formatPickerDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }
    return sdf.format(Date(millis))
}

@Composable
private fun AnnouncementDetailDialog(
    announcement: AnnouncementItem,
    canManage: Boolean,
    repository: AnnouncementsRepository,
    onDismiss: () -> Unit,
    onChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf(false) }
    var title by remember(announcement.id) { mutableStateOf(announcement.title) }
    var body by remember(announcement.id) { mutableStateOf(announcement.body) }
    var busy by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    if (!confirmDelete) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                if (editing) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, singleLine = true, label = { Text("Title") })
                } else {
                    Text(announcement.title, style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (localError != null) {
                        Text(localError!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                    }
                    val meta = listOfNotNull(
                        announcement.created_by_name?.takeIf { it.isNotBlank() },
                        announcement.audience_type?.takeIf { it.isNotBlank() },
                        announcement.created_at?.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")
                    if (meta.isNotBlank() && !editing) {
                        Text(meta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                    }
                    if (editing) {
                        OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Body") }, minLines = 4)
                    } else {
                        Text(announcement.body, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            },
            confirmButton = {
                Row {
                    if (canManage) {
                        if (editing) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        busy = true
                                        localError = null
                                        repository.update(announcement.id, title, body)
                                            .onSuccess {
                                                editing = false
                                                onChanged()
                                            }
                                            .onFailure { localError = it.message }
                                        busy = false
                                    }
                                },
                                enabled = !busy
                            ) { Text("Save") }
                            TextButton(onClick = { editing = false; title = announcement.title; body = announcement.body }) {
                                Text("Cancel edit")
                            }
                        } else {
                            TextButton(onClick = { editing = true }) { Text("Edit") }
                            TextButton(onClick = { confirmDelete = true }, enabled = !busy) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete announcement?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            busy = true
                            repository.delete(announcement.id)
                                .onSuccess {
                                    confirmDelete = false
                                    onDismiss()
                                    onChanged()
                                }
                                .onFailure { localError = it.message }
                            busy = false
                        }
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AnnouncementCard(ann: AnnouncementItem, onClick: () -> Unit) {
    val dateLine = formatAnnouncementDateTime(ann.created_at)
    val meta = listOfNotNull(
        ann.created_by_name?.takeIf { it.isNotBlank() },
        ann.audience_type?.takeIf { it.isNotBlank() }
    ).joinToString(" • ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .ajesCardBorder()
            .clickable(onClick = onClick),
        shape = AjesCardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = AjesTextPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            if (dateLine.isNotBlank()) {
                Text(
                    text = dateLine,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AjesGreen
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = ann.title.ifBlank { "Announcement" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AjesTextPrimary
            )
            if (ann.body.isNotBlank()) {
                Text(
                    text = ann.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AjesTextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = AjesTextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateAnnouncementDialog(
    page: AnnouncementsPage,
    repository: AnnouncementsRepository,
    onDismiss: () -> Unit,
    onPublished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val roleUpper = page.role.trim().uppercase(Locale.US)
    val isTeacher = roleUpper == "TEACHER"
    val teacherSections = page.teacherSections
    val audienceOrder = listOf("school-wide", "staff-only", "students-only", "teachers-only")
    val audienceOptions = remember(page.audienceOptions) {
        if (page.audienceOptions.isNotEmpty()) page.audienceOptions else emptyMap()
    }
    val audienceKeys = remember(audienceOptions) {
        audienceOrder.filter { audienceOptions.containsKey(it) }.ifEmpty { audienceOptions.keys.toList() }
    }
    var selectedAudience by remember {
        mutableStateOf(audienceKeys.firstOrNull() ?: "school-wide")
    }
    var selectedSectionId by remember(teacherSections) {
        mutableIntStateOf(teacherSections.firstOrNull()?.id ?: 0)
    }
    var localError by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("New announcement") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (localError != null) {
                    Text(localError!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; localError = null },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ajesTextFieldColors()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it; localError = null },
                    label = { Text("Message") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ajesTextFieldColors()
                )

                if (isTeacher && teacherSections.isEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "You have no accepted section yet. On the web, open My Sections and accept your invite, or ask the admin to assign you to a class.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (isTeacher && teacherSections.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Section",
                        style = MaterialTheme.typography.labelLarge,
                        color = AjesTextPrimary
                    )
                    Text(
                        "Students in the selected section will receive this announcement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AjesTextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    teacherSections.forEach { section ->
                        val label = section.display_label.ifBlank { "Section #${section.id}" }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !submitting) {
                                    selectedSectionId = section.id
                                    localError = null
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSectionId == section.id,
                                onClick = {
                                    selectedSectionId = section.id
                                    localError = null
                                },
                                enabled = !submitting
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium, color = AjesTextPrimary)
                        }
                    }
                } else if (audienceKeys.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Audience",
                        style = MaterialTheme.typography.labelLarge,
                        color = AjesTextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    audienceKeys.forEach { key ->
                        val label = audienceOptions[key] ?: key
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !submitting) {
                                    selectedAudience = key
                                    localError = null
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAudience == key,
                                onClick = {
                                    selectedAudience = key
                                    localError = null
                                },
                                enabled = !submitting
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium, color = AjesTextPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !submitting && (!isTeacher || teacherSections.isNotEmpty()),
                onClick = {
                    val t = title.trim()
                    val b = body.trim()
                    if (t.isEmpty() || b.isEmpty()) {
                        localError = "Title and message are required."
                        return@Button
                    }
                    if (isTeacher && selectedSectionId < 1) {
                        localError = "Please choose a section you handle."
                        return@Button
                    }
                    localError = null
                    scope.launch {
                        submitting = true
                        val sectionId = if (isTeacher) selectedSectionId else null
                        val audience = when {
                            isTeacher -> "students-only"
                            audienceKeys.isNotEmpty() -> selectedAudience
                            else -> null
                        }
                        repository.create(t, b, sectionId = sectionId, audienceType = audience)
                            .onSuccess {
                                onPublished()
                                onDismiss()
                            }
                            .onFailure { err ->
                                localError = err.message ?: "Publish failed."
                            }
                        submitting = false
                    }
                }
            ) {
                Text("Publish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) { Text("Cancel") }
        }
    )
}
