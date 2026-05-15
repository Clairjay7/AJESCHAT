package com.example.ajeschat.ui.main

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ajeschat.data.AnnouncementItem
import com.example.ajeschat.data.AnnouncementsPage
import com.example.ajeschat.data.AnnouncementsRepository
import com.example.ajeschat.data.TeacherSectionOption
import com.example.ajeschat.session.SessionHolder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val DATE_PATTERN = Regex("^\\d{4}-\\d{2}-\\d{2}$")

private val DEFAULT_AUDIENCE_OPTIONS = mapOf(
    "school-wide" to "For all users",
    "staff-only" to "For staffs only",
    "students-only" to "For students only",
    "teachers-only" to "For teachers only"
)

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
    var dateFrom by remember { mutableStateOf("") }
    var dateTo by remember { mutableStateOf("") }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) {
        loading = true
        error = null
        val from = dateFrom.trim().takeIf { it.matches(DATE_PATTERN) }
        val to = dateTo.trim().takeIf { it.matches(DATE_PATTERN) }
        repository.loadPage(dateFrom = from, dateTo = to)
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
    val isStudent = roleUpper == "STUDENT"
    val showAdd = p != null && !isStudent

    if (showFromPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dateFrom = formatPickerDate(it) }
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showToPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dateTo = formatPickerDate(it) }
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error ?: "Error", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { refreshKey++ }) { Text("Retry") }
                }
            }
            p != null && p.announcements.isEmpty() -> {
                Column(Modifier.fillMaxSize()) {
                    AnnouncementsToolbar(
                        showAdd = showAdd,
                        dateFrom = dateFrom,
                        dateTo = dateTo,
                        onDateFromClick = { showFromPicker = true },
                        onDateToClick = { showToPicker = true },
                        onRefresh = { refreshKey++ },
                        onAdd = { showCreate = true },
                        onFilter = { refreshKey++ },
                        onResetFilter = {
                            dateFrom = ""
                            dateTo = ""
                            refreshKey++
                        }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Announcements", style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (dateFrom.isNotBlank() || dateTo.isNotBlank()) {
                                    "No announcements in this date range."
                                } else {
                                    "No announcements yet."
                                },
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            p != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        AnnouncementsToolbar(
                            showAdd = showAdd,
                            dateFrom = dateFrom,
                            dateTo = dateTo,
                            onDateFromClick = { showFromPicker = true },
                            onDateToClick = { showToPicker = true },
                            onRefresh = { refreshKey++ },
                            onAdd = { showCreate = true },
                            onFilter = { refreshKey++ },
                            onResetFilter = {
                                dateFrom = ""
                                dateTo = ""
                                refreshKey++
                            }
                        )
                    }
                    items(p.announcements) { ann ->
                        AnnouncementCard(ann, onClick = { selectedAnnouncement = ann })
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
    dateFrom: String,
    dateTo: String,
    onDateFromClick: () -> Unit,
    onDateToClick: () -> Unit,
    onRefresh: () -> Unit,
    onAdd: () -> Unit,
    onFilter: () -> Unit,
    onResetFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onRefresh) { Text("Refresh") }
            if (showAdd) {
                TextButton(onClick = onAdd) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Add", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateFilterField(
                label = "From",
                value = dateFrom,
                onClick = onDateFromClick,
                modifier = Modifier.weight(1f)
            )
            DateFilterField(
                label = "To",
                value = dateTo,
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
            TextButton(onClick = onFilter) { Text("Filter") }
            if (dateFrom.isNotBlank() || dateTo.isNotBlank()) {
                TextButton(onClick = onResetFilter) { Text("Reset") }
            }
        }
    }
}

@Composable
private fun DateFilterField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("yyyy-MM-dd") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = ann.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = ann.body,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            val meta = listOfNotNull(
                ann.created_by_name?.takeIf { it.isNotBlank() },
                ann.audience_type?.takeIf { it.isNotBlank() },
                ann.created_at?.takeIf { it.isNotBlank() }
            ).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val audienceOrder = listOf("school-wide", "staff-only", "students-only", "teachers-only")
    val audienceOptions = remember(page.audienceOptions, page.role) {
        if (page.audienceOptions.isNotEmpty()) {
            page.audienceOptions
        } else if (page.role.trim().uppercase(Locale.US) == "TEACHER") {
            DEFAULT_AUDIENCE_OPTIONS
        } else {
            emptyMap()
        }
    }
    val audienceKeys = remember(audienceOptions) {
        audienceOrder.filter { audienceOptions.containsKey(it) }.ifEmpty { audienceOptions.keys.toList() }
    }
    var selectedAudience by remember {
        mutableStateOf(audienceKeys.firstOrNull() ?: "school-wide")
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
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it; localError = null },
                    label = { Text("Message") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                if (audienceKeys.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Audience", style = MaterialTheme.typography.labelLarge)
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
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !submitting,
                onClick = {
                    val t = title.trim()
                    val b = body.trim()
                    if (t.isEmpty() || b.isEmpty()) {
                        localError = "Title and message are required."
                        return@Button
                    }
                    localError = null
                    scope.launch {
                        submitting = true
                        val audience = selectedAudience.takeIf { audienceKeys.isNotEmpty() }
                        repository.create(t, b, sectionId = null, audienceType = audience)
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
