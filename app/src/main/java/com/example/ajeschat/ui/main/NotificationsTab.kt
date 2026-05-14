package com.example.ajeschat.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.data.NotificationItem
import com.example.ajeschat.data.NotificationsRepository
import kotlinx.coroutines.launch

@Composable
fun NotificationsTab(onCountsChanged: () -> Unit) {
    val repo = remember { NotificationsRepository(ApiModule.getNotificationsApi()) }
    val scope = rememberCoroutineScope()
    var refresh by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

    LaunchedEffect(refresh) {
        loading = true
        error = null
        repo.list()
            .onSuccess {
                items = it
                error = null
                onCountsChanged()
            }
            .onFailure { e ->
                error = e.message ?: "Failed to load"
            }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { refresh++ }) { Text("Refresh") }
            Button(onClick = {
                scope.launch {
                    repo.markAllRead().onSuccess { refresh++ }
                }
            }) {
                Text("Mark all read")
            }
        }
        HorizontalDivider()
        when {
            loading -> {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator() }
            }
            error != null -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { refresh++ }) { Text("Retry") }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { n ->
                        NotificationRow(
                            item = n,
                            onMarkRead = {
                                scope.launch {
                                    repo.markRead(n.id).onSuccess { refresh++ }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem, onMarkRead: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = !item.isRead) { onMarkRead() }
            .padding(vertical = 8.dp)
    ) {
        Text(
            item.message,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.SemiBold
        )
        Text(
            item.createdAt,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!item.isRead) {
            TextButton(onClick = onMarkRead) { Text("Mark read") }
        }
    }
}
