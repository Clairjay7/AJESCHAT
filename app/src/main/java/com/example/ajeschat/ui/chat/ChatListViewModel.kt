package com.example.ajeschat.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ajeschat.AjesChatApp
import com.example.ajeschat.data.ChatUser
import com.example.ajeschat.data.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatListUiState(
    val users: List<ChatUser> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val chatRepository: ChatRepository = (application as AjesChatApp).chatRepository

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            chatRepository.getUsers()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(users = list, loading = false, error = null)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = it.message ?: "Failed to load users"
                    )
                }
        }
    }
}
