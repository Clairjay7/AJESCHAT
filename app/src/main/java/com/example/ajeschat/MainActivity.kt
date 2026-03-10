package com.example.ajeschat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajeschat.navigation.AjesChatNavGraph
import com.example.ajeschat.navigation.ROUTE_CHAT_LIST
import com.example.ajeschat.navigation.ROUTE_LOGIN
import com.example.ajeschat.session.SessionHolder
import com.example.ajeschat.ui.login.LoginViewModel
import com.example.ajeschat.ui.chat.ChatListViewModel
import com.example.ajeschat.ui.theme.AJESCHATTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AJESCHATTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val startDestination = if (SessionHolder.session != null) ROUTE_CHAT_LIST else ROUTE_LOGIN
                    val loginViewModel: LoginViewModel = viewModel()
                    val chatListViewModel: ChatListViewModel = viewModel()
                    AjesChatNavGraph(
                        loginViewModel = loginViewModel,
                        chatListViewModel = chatListViewModel,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
