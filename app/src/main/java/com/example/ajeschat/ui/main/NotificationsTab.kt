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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.data.NotificationItem
import com.example.ajeschat.data.NotificationsRepository
import com.example.ajeschat.ui.theme.AjesCardShape
import com.example.ajeschat.ui.theme.AjesGreen
import com.example.ajeschat.ui.theme.AjesOnGreen
import com.example.ajeschat.ui.theme.AjesTextPrimary
import com.example.ajeschat.ui.theme.AjesTextSecondary
import com.example.ajeschat.ui.theme.ajesCardBorder
import com.example.ajeschat.ui.theme.ajesScreenBackground
import com.example.ajeschat.ui.theme.ajesTextButtonColors
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

    Column(Modifier.fillMaxSize().ajesScreenBackground()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { refresh++ }, colors = ajesTextButtonColors()) {
                Text("Refresh", color = AjesTextPrimary, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = {
                    scope.launch {
                        repo.markAllRead().onSuccess { refresh++ }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AjesGreen,
                    contentColor = AjesOnGreen
                )
            ) {
                Text("Mark all read", fontWeight = FontWeight.SemiBold)
            }
        }
        HorizontalDivider(color = AjesTextSecondary.copy(alpha = 0.35f))
        when {
            loading -> {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator(color = AjesGreen) }
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
                    TextButton(onClick = { refresh++ }, colors = ajesTextButtonColors()) {
                        Text("Retry", color = AjesTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            items.isEmpty() -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No alerts yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AjesTextPrimary
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
    val body = item.message.trim().ifBlank { "Notification" }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .ajesCardBorder()
            .clickable(enabled = !item.isRead) { onMarkRead() },
        shape = AjesCardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = AjesTextPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold,
                color = AjesTextPrimary
            )
            if (item.createdAt.isNotBlank()) {
                Text(
                    text = item.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = AjesTextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            if (!item.isRead) {
                Button(
                    onClick = onMarkRead,
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AjesGreen,
                        contentColor = AjesOnGreen
                    )
                ) {
                    Text("Mark read", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
