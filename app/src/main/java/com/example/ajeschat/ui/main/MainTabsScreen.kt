package com.example.ajeschat.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ajeschat.data.AnnouncementsRepository
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.data.ChatUser
import com.example.ajeschat.data.ProfileRepository
import com.example.ajeschat.ui.chat.ChatListScreen
import com.example.ajeschat.ui.chat.ChatListViewModel

private enum class MainTab {
    Chats, Announcements, Profile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    chatListViewModel: ChatListViewModel,
    onUserClick: (ChatUser) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Chats) }
    val chatState by chatListViewModel.uiState.collectAsState()
    val chatsBadgeCount = chatState.users.count { it.hasChat }
    val appCtx = LocalContext.current.applicationContext
    val announcementsRepository = remember { AnnouncementsRepository(ApiModule.getAnnouncementApi()) }
    val profileRepository = remember(appCtx) { ProfileRepository(appCtx, ApiModule.getProfileApi()) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.Chats,
                    onClick = { selectedTab = MainTab.Chats },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (chatsBadgeCount > 0) {
                                    Badge {
                                        Text(
                                            chatsBadgeCount.coerceAtMost(99).toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.Chat,
                                contentDescription = "Chats"
                            )
                        }
                    },
                    label = { Text("Chats") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Announcements,
                    onClick = { selectedTab = MainTab.Announcements },
                    icon = {
                        Icon(
                            Icons.Filled.Campaign,
                            contentDescription = "Announcements"
                        )
                    },
                    label = { Text("Announcements") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Profile,
                    onClick = { selectedTab = MainTab.Profile },
                    icon = {
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
                    },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                MainTab.Chats -> ChatListScreen(
                    viewModel = chatListViewModel,
                    onUserClick = onUserClick,
                    onLogout = onLogout,
                    showLogoutInTopBar = false
                )
                MainTab.Announcements -> AnnouncementsTab(repository = announcementsRepository)
                MainTab.Profile -> ProfileTab(repository = profileRepository, onLogout = onLogout)
            }
        }
    }
}

@Composable
private fun PlaceholderTab(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}
