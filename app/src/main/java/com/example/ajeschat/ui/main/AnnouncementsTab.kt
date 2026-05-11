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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import kotlinx.coroutines.launch

@Composable
fun AnnouncementsTab(
    repository: AnnouncementsRepository
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf<AnnouncementsPage?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var selectedAnnouncement by remember { mutableStateOf<AnnouncementItem?>(null) }

    fun load() {
        loading = true
        error = null
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        repository.loadPage()
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
    val showFab = p != null && p.canManage &&
        (p.role.uppercase() != "TEACHER" || p.teacherSections.isNotEmpty())

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
                    TextButton(onClick = { load() }) { Text("Retry") }
                }
            }
            p != null && p.announcements.isEmpty() -> {
                BoxPlaceholder(title = "Announcements", subtitle = "No announcements yet.")
            }
            p != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        TextButton(onClick = { load() }) { Text("Refresh") }
                    }
                    items(p.announcements) { ann ->
                        AnnouncementCard(ann, onClick = { selectedAnnouncement = ann })
                    }
                }
            }
        }

        if (showFab && !loading && error == null) {
            FloatingActionButton(
                onClick = { showCreate = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New announcement")
            }
        }
    }

    if (showCreate && p != null) {
        CreateAnnouncementDialog(
            page = p,
            repository = repository,
            onDismiss = { showCreate = false },
            onPublished = { load() }
        )
    }

    selectedAnnouncement?.let { ann ->
        AnnouncementDetailDialog(announcement = ann, onDismiss = { selectedAnnouncement = null })
    }
}

@Composable
private fun AnnouncementDetailDialog(
    announcement: AnnouncementItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(announcement.title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                val meta = listOfNotNull(
                    announcement.created_by_name?.takeIf { it.isNotBlank() },
                    announcement.audience_type?.takeIf { it.isNotBlank() },
                    announcement.created_at?.takeIf { it.isNotBlank() }
                ).joinToString(" • ")
                if (meta.isNotBlank()) {
                    Text(
                        meta,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Text(announcement.body, style = MaterialTheme.typography.bodyLarge)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
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
    var selectedSectionId by remember {
        mutableIntStateOf(page.teacherSections.firstOrNull()?.id ?: -1)
    }
    val audienceKeys = remember(page.audienceOptions) { page.audienceOptions.keys.toList().sorted() }
    var selectedAudience by remember {
        mutableStateOf(audienceKeys.firstOrNull() ?: "school-wide")
    }
    var localError by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    val isTeacher = page.role.uppercase() == "TEACHER"
    val needsSection = isTeacher && page.teacherSections.isNotEmpty()

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

                if (needsSection) {
                    Spacer(Modifier.height(12.dp))
                    Text("Section", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    page.teacherSections.forEach { sec: TeacherSectionOption ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !submitting) {
                                    selectedSectionId = sec.id
                                    localError = null
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSectionId == sec.id,
                                onClick = {
                                    selectedSectionId = sec.id
                                    localError = null
                                },
                                enabled = !submitting
                            )
                            Text(sec.display_label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (audienceKeys.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Audience", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    audienceKeys.forEach { key ->
                        val label = page.audienceOptions[key] ?: key
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
                    if (needsSection && selectedSectionId < 1) {
                        localError = "Please choose a section."
                        return@Button
                    }
                    localError = null
                    val sectionId = if (needsSection) selectedSectionId else null
                    val audience = if (audienceKeys.isNotEmpty()) selectedAudience else null
                    scope.launch {
                        submitting = true
                        repository.create(t, b, sectionId, audience)
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

@Composable
private fun BoxPlaceholder(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
