package com.example.ajeschat.navigation

import android.app.Application
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
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(ROUTE_LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(ROUTE_CHAT_LIST) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(ROUTE_CHAT_LIST) {
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
            )
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
