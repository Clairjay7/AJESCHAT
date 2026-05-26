package com.example.ajeschat.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ajeschat.navigation.AjesWebModules
import com.example.ajeschat.navigation.webModuleRoute
import com.example.ajeschat.ui.chat.ChatListScreen
import com.example.ajeschat.ui.chat.ChatListViewModel
import com.example.ajeschat.session.SessionStore
import com.example.ajeschat.ui.theme.AjesBorder
import com.example.ajeschat.ui.theme.AjesCardShapeMedium
import com.example.ajeschat.ui.theme.AjesGreen
import com.example.ajeschat.ui.theme.AjesMintBg2
import com.example.ajeschat.ui.theme.AjesTextPrimary
import com.example.ajeschat.ui.theme.AjesTextSecondary
import com.example.ajeschat.ui.theme.ajesLogoTopAppBarColors
import com.example.ajeschat.ui.theme.ajesNavigationBarItemColors
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
    var userRole by remember { mutableStateOf("") }
    var showSectionsMenu by remember { mutableStateOf(false) }
    var showAcademicYearsMenu by remember { mutableStateOf(false) }
    var showReportsMenu by remember { mutableStateOf(false) }
    var principalMenuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab, badgeNonce) {
        mobileRepo.summary().onSuccess {
            unreadBell = it.unreadNotifications
            showSectionsMenu = it.sections || it.sectionsRead
            showAcademicYearsMenu = it.academicYears
            showReportsMenu = it.records
        }
        SessionStore(appCtx).load()?.role?.let { userRole = it.uppercase() }
    }

    val showPrincipalOverflow =
        userRole == "PRINCIPAL" && (showSectionsMenu || showAcademicYearsMenu || showReportsMenu)

    val onBadgeRefresh: () -> Unit = { badgeNonce += 1 }
    val navColors = ajesNavigationBarItemColors()

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
                colors = ajesLogoTopAppBarColors(),
                actions = {
                    if (showPrincipalOverflow) {
                        PrincipalToolsMenu(
                            expanded = principalMenuOpen,
                            onExpandedChange = { principalMenuOpen = it },
                            showSections = showSectionsMenu,
                            showAcademicYears = showAcademicYearsMenu,
                            showReports = showReportsMenu,
                            onSections = {
                                principalMenuOpen = false
                                navController.navigate(webModuleRoute(AjesWebModules.SECTIONS))
                            },
                            onAcademicYears = {
                                principalMenuOpen = false
                                navController.navigate(webModuleRoute(AjesWebModules.ACADEMIC_YEARS))
                            },
                            onReports = {
                                principalMenuOpen = false
                                navController.navigate(webModuleRoute(AjesWebModules.REPORTS))
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.border(
                    width = 1.dp,
                    color = AjesBorder
                ),
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 2.dp,
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.Home,
                    onClick = { selectedTab = MainTab.Home },
                    colors = navColors,
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Chats,
                    onClick = { selectedTab = MainTab.Chats },
                    colors = navColors,
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
                NavigationBarItem(
                    selected = selectedTab == MainTab.Announcements,
                    onClick = { selectedTab = MainTab.Announcements },
                    colors = navColors,
                    icon = { Icon(Icons.Filled.Campaign, contentDescription = "Announcements") },
                    label = { Text("News") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Notifications,
                    onClick = { selectedTab = MainTab.Notifications },
                    colors = navColors,
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
                NavigationBarItem(
                    selected = selectedTab == MainTab.Profile,
                    onClick = { selectedTab = MainTab.Profile },
                    colors = navColors,
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
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
            when (selectedTab) {
                MainTab.Home -> HomeTab(onRefreshNotificationBadge = onBadgeRefresh)
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

@Composable
private fun PrincipalToolsMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    showSections: Boolean,
    showAcademicYears: Boolean,
    showReports: Boolean,
    onSections: () -> Unit,
    onAcademicYears: () -> Unit,
    onReports: () -> Unit
) {
    val itemColors = MenuDefaults.itemColors(
        textColor = AjesTextPrimary,
        leadingIconColor = AjesGreen,
        trailingIconColor = AjesGreen,
        disabledTextColor = AjesTextSecondary,
        disabledLeadingIconColor = AjesTextSecondary
    )

    Box {
        IconButton(
            onClick = { onExpandedChange(true) },
            modifier = Modifier
                .background(AjesMintBg2, CircleShape)
                .border(1.dp, AjesBorder, CircleShape)
        ) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = "Principal tools",
                tint = AjesGreen
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.widthIn(min = 220.dp),
            shape = AjesCardShapeMedium,
            containerColor = Color.White,
            shadowElevation = 8.dp,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, AjesBorder)
        ) {
            if (showSections) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Sections",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AjesTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.ViewList, contentDescription = null, tint = AjesGreen)
                    },
                    onClick = onSections,
                    colors = itemColors
                )
            }
            if (showSections && showAcademicYears) {
                HorizontalDivider(color = AjesBorder)
            }
            if (showAcademicYears) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Academic Years",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AjesTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.DateRange, contentDescription = null, tint = AjesGreen)
                    },
                    onClick = onAcademicYears,
                    colors = itemColors
                )
            }
            if ((showSections || showAcademicYears) && showReports) {
                HorizontalDivider(color = AjesBorder)
            }
            if (showReports) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Reports",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AjesTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Folder, contentDescription = null, tint = AjesGreen)
                    },
                    onClick = onReports,
                    colors = itemColors
                )
            }
        }
    }
}
