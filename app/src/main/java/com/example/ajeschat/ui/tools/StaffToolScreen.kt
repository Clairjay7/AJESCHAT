package com.example.ajeschat.ui.tools

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ajeschat.data.AdminSectionRow
import com.example.ajeschat.data.AdminUserRow
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.data.ChatLogRow
import com.example.ajeschat.data.RecordRow
import com.example.ajeschat.data.StaffRepository
import com.example.ajeschat.data.SysadminModule
import com.example.ajeschat.data.TeacherHubRow
import com.example.ajeschat.ui.theme.ajesScreenBackground

@Composable
fun StaffToolScreen(toolId: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().ajesScreenBackground()) {
        TextButton(onClick = onBack) { Text("Back") }
        when (toolId) {
            "admin_users" -> AdminUsersList()
            "admin_sections" -> AdminSectionsList()
            "teacher" -> TeacherHubList()
            "records" -> RecordsPagedList()
            "chat_logs" -> ChatLogsPagedList()
            "sysadmin" -> SysadminList()
            else -> Text("Unknown screen", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun AdminUsersList() {
    val repo = remember { StaffRepository(ApiModule.getStaffApi()) }
    var rows by remember { mutableStateOf<List<AdminUserRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        repo.adminUsers()
            .onSuccess { rows = it; error = null }
            .onFailure { error = it.message }
        loading = false
    }
    when {
        loading -> Loader()
        error != null -> Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        else -> LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows, key = { it.id }) { u ->
                Text("${u.name ?: u.username} — ${u.role} — ${u.email ?: ""}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AdminSectionsList() {
    val repo = remember { StaffRepository(ApiModule.getStaffApi()) }
    var rows by remember { mutableStateOf<List<AdminSectionRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        repo.adminSections()
            .onSuccess { rows = it; error = null }
            .onFailure { error = it.message }
        loading = false
    }
    when {
        loading -> Loader()
        error != null -> Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        else -> LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows, key = { it.id }) { s ->
                Text("${s.name} (${s.gradeLevel})", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun TeacherHubList() {
    val repo = remember { StaffRepository(ApiModule.getStaffApi()) }
    var invites by remember { mutableStateOf<List<TeacherHubRow>>(emptyList()) }
    var sections by remember { mutableStateOf<List<TeacherHubRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        repo.teacherHub()
            .onSuccess {
                invites = it.invites
                sections = it.sections
                error = null
            }
            .onFailure { error = it.message }
        loading = false
    }
    when {
        loading -> Loader()
        error != null -> Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        else -> LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Invites", style = MaterialTheme.typography.titleMedium) }
            items(invites, key = { it.assignmentId }) { r ->
                Text("${r.sectionName} — ${r.status} — ${r.subjectName}", style = MaterialTheme.typography.bodyMedium)
            }
            item { Text("Accepted sections", style = MaterialTheme.typography.titleMedium) }
            items(sections, key = { it.assignmentId }) { r ->
                Text("${r.sectionName} (${r.gradeLevel})", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun RecordsPagedList() {
    val repo = remember { StaffRepository(ApiModule.getStaffApi()) }
    var page by remember { mutableIntStateOf(1) }
    var rows by remember { mutableStateOf<List<RecordRow>>(emptyList()) }
    var totalPages by remember { mutableIntStateOf(1) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(page) {
        loading = true
        repo.records(page)
            .onSuccess {
                rows = it.items
                totalPages = it.totalPages.coerceAtLeast(1)
                error = null
            }
            .onFailure { error = it.message }
        loading = false
    }
    when {
        loading -> Loader()
        error != null -> Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        else -> Column(Modifier.fillMaxSize().padding(16.dp)) {
            RowNav(page, totalPages) { page = it }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rows, key = { it.id }) { r ->
                    Text("${r.type}: ${r.details?.take(120)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ChatLogsPagedList() {
    val repo = remember { StaffRepository(ApiModule.getStaffApi()) }
    var page by remember { mutableIntStateOf(1) }
    var rows by remember { mutableStateOf<List<ChatLogRow>>(emptyList()) }
    var totalPages by remember { mutableIntStateOf(1) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(page) {
        loading = true
        repo.chatLogs(page)
            .onSuccess {
                rows = it.items
                totalPages = it.totalPages.coerceAtLeast(1)
                error = null
            }
            .onFailure { error = it.message }
        loading = false
    }
    when {
        loading -> Loader()
        error != null -> Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        else -> Column(Modifier.fillMaxSize().padding(16.dp)) {
            RowNav(page, totalPages) { page = it }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rows, key = { it.id }) { r ->
                    Text(
                        "${r.senderName} → ${r.receiverName}: ${r.content?.take(100)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RowNav(page: Int, totalPages: Int, setPage: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { if (page > 1) setPage(page - 1) }, enabled = page > 1) { Text("Prev") }
        Text("Page $page / $totalPages", style = MaterialTheme.typography.labelLarge)
        TextButton(onClick = { if (page < totalPages) setPage(page + 1) }, enabled = page < totalPages) {
            Text("Next")
        }
    }
}

@Composable
private fun SysadminList() {
    val repo = remember { StaffRepository(ApiModule.getStaffApi()) }
    val androidCtx = LocalContext.current
    var mods by remember { mutableStateOf<List<SysadminModule>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        repo.sysadminModules()
            .onSuccess { mods = it; error = null }
            .onFailure { error = it.message }
        loading = false
    }
    when {
        loading -> Loader()
        error != null -> Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        else -> LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text(
                    "Opens the full AJES page in your browser.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(mods, key = { "${it.key}_${it.title}" }) { m ->
                val url = m.webUrl
                TextButton(
                    onClick = {
                        if (!url.isNullOrBlank()) {
                            androidCtx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    },
                    enabled = !url.isNullOrBlank()
                ) {
                    Text(m.title ?: m.key ?: "Open")
                }
            }
        }
    }
}

@Composable
private fun Loader() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) { CircularProgressIndicator() }
}
