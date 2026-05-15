package com.example.ajeschat.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.ajeschat.R
import com.example.ajeschat.data.ChatUser
import com.example.ajeschat.ui.theme.AjesStatusOnline
import com.example.ajeschat.ui.theme.AjesTextPrimary
import com.example.ajeschat.ui.theme.AjesTextSecondary
import com.example.ajeschat.ui.theme.ajesScreenBackground
import com.example.ajeschat.ui.theme.ajesTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel,
    onUserClick: (ChatUser) -> Unit,
    onLogout: () -> Unit,
    showLogoutInTopBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showLogoutInTopBar) {
                TopAppBar(
                    colors = ajesTopAppBarColors(),
                    navigationIcon = {
                        Image(
                            painter = painterResource(R.drawable.ajes_logo),
                            contentDescription = "AJES logo"
                        )
                    },
                    title = { Text("Messages") },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Log out",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .ajesScreenBackground()
                .padding(padding)
        ) {
            when {
                uiState.loading && uiState.users.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null && uiState.users.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    val keyboard = LocalSoftwareKeyboardController.current
                    val filtered = uiState.filteredUsers
                    Column(Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::updateSearchQuery,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Search name…") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(
                                            Icons.Filled.Clear,
                                            contentDescription = "Clear search"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { keyboard?.hide() }
                            )
                        )
                        when {
                            filtered.isEmpty() && uiState.users.isNotEmpty() -> {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No matching names",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                                ) {
                                    itemsIndexed(
                                        items = filtered,
                                        key = { _, u -> u.id }
                                    ) { index, user ->
                                        Column(Modifier.fillMaxWidth()) {
                                            ChatListUserRow(
                                                user = user,
                                                onClick = { onUserClick(user) }
                                            )
                                            if (index < filtered.lastIndex) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(start = 84.dp),
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                        alpha = 0.55f
                                                    ),
                                                    thickness = 0.5.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun userInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].first()}${parts[1].first()}".uppercase()
    }
}

@Composable
private fun ChatListUserRow(
    user: ChatUser,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val preview = when {
        !user.lastMessagePreview.isNullOrBlank() -> user.lastMessagePreview.trim()
        !user.role.isNullOrBlank() -> user.role.trim()
        else -> "Tap to chat"
    }
    val timeLabel = user.lastMessageAt?.trim()?.takeIf { it.isNotEmpty() }
    val activeLabel = user.activeStatus?.trim()?.takeIf { it.isNotEmpty() }
    val avatarColors = listOf(
        scheme.primaryContainer,
        scheme.secondaryContainer,
        scheme.tertiaryContainer
    )
    val avatarColorIndex = user.id.mod(avatarColors.size)
    val avatarBg = avatarColors[avatarColorIndex]
    val onAvatar = when (avatarColorIndex) {
        0 -> scheme.onPrimaryContainer
        1 -> scheme.onSecondaryContainer
        else -> scheme.onTertiaryContainer
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = scheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(width = 60.dp, height = 56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .border(1.dp, scheme.outlineVariant, CircleShape)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitials(user.name),
                        style = MaterialTheme.typography.titleMedium,
                        color = onAvatar,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (activeLabel != null) {
                    Text(
                        text = activeLabel,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(AjesStatusOnline)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AjesTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (user.pinned) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(18.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = AjesTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (timeLabel != null) {
                        Text(
                            text = " · $timeLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = AjesTextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
