package com.example.ajeschat.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ajeschat.data.AnnouncementsRepository
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.data.ChatUser
import com.example.ajeschat.data.MobileRepository
import com.example.ajeschat.data.ProfileRepository
import com.example.ajeschat.ui.chat.ChatListScreen
import com.example.ajeschat.ui.chat.ChatListViewModel

private enum class MainTab {
    Home, Chats, Announcements, Notifications, Profile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    navController: NavHostController,
    chatListViewModel: ChatListViewModel,
    onUserClick: (ChatUser) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.Home) }
    val chatState by chatListViewModel.uiState.collectAsState()
    val chatsBadgeCount = chatState.users.count { it.hasChat }
    val appCtx = LocalContext.current.applicationContext
    val announcementsRepository = remember { AnnouncementsRepository(ApiModule.getAnnouncementApi()) }
    val profileRepository = remember(appCtx) { ProfileRepository(appCtx, ApiModule.getProfileApi()) }
    val mobileRepo = remember { MobileRepository(ApiModule.getMobileApi()) }
    var unreadBell by remember { mutableIntStateOf(0) }
    var badgeNonce by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab, badgeNonce) {
        mobileRepo.summary().onSuccess { unreadBell = it.unreadNotifications }
    }

    val onBadgeRefresh: () -> Unit = { badgeNonce += 1 }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.Home,
                    onClick = { selectedTab = MainTab.Home },
                    icon = {
                        Icon(Icons.Filled.Home, contentDescription = "Home")
                    },
                    label = { Text("Home") }
                )
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
                            Icon(Icons.Filled.Chat, contentDescription = "Chats")
                        }
                    },
                    label = { Text("Chats") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Announcements,
                    onClick = { selectedTab = MainTab.Announcements },
                    icon = {
                        Icon(Icons.Filled.Campaign, contentDescription = "Announcements")
                    },
                    label = { Text("News") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Notifications,
                    onClick = { selectedTab = MainTab.Notifications },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadBell > 0) {
                                    Badge {
                                        Text(
                                            unreadBell.coerceAtMost(99).toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                    },
                    label = { Text("Alerts") }
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
                MainTab.Home -> HomeTab(
                    navController = navController,
                    onRefreshNotificationBadge = onBadgeRefresh,
                    onOpenChats = { selectedTab = MainTab.Chats },
                    onOpenNews = { selectedTab = MainTab.Announcements },
                    onOpenAlerts = { selectedTab = MainTab.Notifications }
                )
                MainTab.Chats -> ChatListScreen(
                    viewModel = chatListViewModel,
                    onUserClick = onUserClick,
                    onLogout = onLogout,
                    showLogoutInTopBar = false
                )
                MainTab.Announcements -> AnnouncementsTab(repository = announcementsRepository)
                MainTab.Notifications -> NotificationsTab(onCountsChanged = onBadgeRefresh)
                MainTab.Profile -> ProfileTab(repository = profileRepository, onLogout = onLogout)
            }
        }
    }
}
