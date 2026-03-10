package com.example.ajeschat.navigation

import android.app.Application
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ajeschat.data.ChatUser
import com.example.ajeschat.ui.chat.ChatListScreen
import com.example.ajeschat.ui.chat.ChatListViewModel
import com.example.ajeschat.ui.chat.ConversationScreen
import com.example.ajeschat.ui.chat.ConversationViewModel
import com.example.ajeschat.ui.login.LoginScreen
import com.example.ajeschat.ui.login.LoginViewModel

private const val NAV_ANIM_DURATION = 300

const val ROUTE_LOGIN = "login"
const val ROUTE_CHAT_LIST = "chat_list"
const val ROUTE_CONVERSATION = "conversation/{userId}/{userName}/{userRole}"

fun conversationRoute(userId: Int, userName: String, userRole: String): String {
    val encName = userName.replace(" ", "+")
    val encRole = (userRole).replace(" ", "+")
    return "conversation/$userId/$encName/$encRole"
}

@Composable
fun AjesChatNavGraph(
    navController: NavHostController = rememberNavController(),
    loginViewModel: LoginViewModel,
    chatListViewModel: ChatListViewModel,
    startDestination: String = ROUTE_LOGIN
) {
    val navTransitions = object {
        val enter = slideInHorizontally(animationSpec = tween(NAV_ANIM_DURATION)) { it } + fadeIn(animationSpec = tween(NAV_ANIM_DURATION))
        val exit = slideOutHorizontally(animationSpec = tween(NAV_ANIM_DURATION)) { -it } + fadeOut(animationSpec = tween(NAV_ANIM_DURATION))
        val popEnter = slideInHorizontally(animationSpec = tween(NAV_ANIM_DURATION)) { -it } + fadeIn(animationSpec = tween(NAV_ANIM_DURATION))
        val popExit = slideOutHorizontally(animationSpec = tween(NAV_ANIM_DURATION)) { it } + fadeOut(animationSpec = tween(NAV_ANIM_DURATION))
    }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = ROUTE_LOGIN,
            enterTransition = { navTransitions.enter },
            exitTransition = { navTransitions.exit },
            popEnterTransition = { navTransitions.popEnter },
            popExitTransition = { navTransitions.popExit }
        ) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(ROUTE_CHAT_LIST) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = ROUTE_CHAT_LIST,
            enterTransition = { navTransitions.enter },
            exitTransition = { navTransitions.exit },
            popEnterTransition = { navTransitions.popEnter },
            popExitTransition = { navTransitions.popExit }
        ) {
            ChatListScreen(
                viewModel = chatListViewModel,
                onUserClick = { user ->
                    navController.navigate(conversationRoute(user.id, user.name, user.role ?: ""))
                },
                onLogout = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = ROUTE_CONVERSATION,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("userName") { type = NavType.StringType },
                navArgument("userRole") { type = NavType.StringType }
            ),
            enterTransition = { navTransitions.enter },
            exitTransition = { navTransitions.exit },
            popEnterTransition = { navTransitions.popEnter },
            popExitTransition = { navTransitions.popExit }
        ) { backStackEntry ->
            val app = LocalContext.current.applicationContext as Application
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val userName = (backStackEntry.arguments?.getString("userName") ?: "").replace("+", " ")
            val userRole = (backStackEntry.arguments?.getString("userRole") ?: "").replace("+", " ")
            val partner = remember(userId, userName, userRole) {
                ChatUser(id = userId, name = userName, role = userRole.ifEmpty { null }, hasChat = true)
            }
            val convViewModel = remember(partner) {
                ConversationViewModel(app, partner)
            }
            ConversationScreen(
                viewModel = convViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
