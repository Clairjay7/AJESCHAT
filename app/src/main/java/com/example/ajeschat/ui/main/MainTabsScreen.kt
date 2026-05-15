package com.example.ajeschat.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ajeschat.R
import com.example.ajeschat.data.AnnouncementsRepository
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.data.ChatUser
import com.example.ajeschat.data.MobileRepository
import com.example.ajeschat.data.ProfileRepository
import com.example.ajeschat.ui.chat.ChatListScreen
import com.example.ajeschat.ui.chat.ChatListViewModel
import com.example.ajeschat.ui.theme.AjesBorder
import com.example.ajeschat.ui.theme.ajesLogoTopAppBarColors
import com.example.ajeschat.ui.theme.ajesNavigationRailItemColors
import com.example.ajeschat.ui.theme.ajesScreenBackground

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
    val railColors = ajesNavigationRailItemColors()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(R.drawable.ajes_logo),
                        contentDescription = "AJES logo",
                        modifier = Modifier
                            .heightIn(min = 48.dp, max = 64.dp)
                            .widthIn(max = 240.dp)
                            .height(64.dp),
                        contentScale = ContentScale.Fit
                    )
                },
                colors = ajesLogoTopAppBarColors()
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .border(width = 1.dp, color = AjesBorder),
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationRailItem(
                    selected = selectedTab == MainTab.Home,
                    onClick = { selectedTab = MainTab.Home },
                    colors = railColors,
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationRailItem(
                    selected = selectedTab == MainTab.Chats,
                    onClick = { selectedTab = MainTab.Chats },
                    colors = railColors,
                    icon = {
                        BadgedBox(
                            badge = {
                                if (chatsBadgeCount > 0) {
                                    Badge(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-2).dp)
                                    ) {
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
                NavigationRailItem(
                    selected = selectedTab == MainTab.Announcements,
                    onClick = { selectedTab = MainTab.Announcements },
                    colors = railColors,
                    icon = { Icon(Icons.Filled.Campaign, contentDescription = "Announcements") },
                    label = { Text("News") }
                )
                NavigationRailItem(
                    selected = selectedTab == MainTab.Notifications,
                    onClick = { selectedTab = MainTab.Notifications },
                    colors = railColors,
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadBell > 0) {
                                    Badge(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-2).dp)
                                    ) {
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
                NavigationRailItem(
                    selected = selectedTab == MainTab.Profile,
                    onClick = { selectedTab = MainTab.Profile },
                    colors = railColors,
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .ajesScreenBackground()
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
}
