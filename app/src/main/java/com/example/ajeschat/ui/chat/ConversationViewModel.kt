package com.example.ajeschat.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ajeschat.AjesChatApp
import com.example.ajeschat.data.ChatMessage
import com.example.ajeschat.data.ChatRepository
import com.example.ajeschat.data.ChatUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ConversationUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val loading: Boolean = false,
    val sending: Boolean = false,
    val error: String? = null
)

class ConversationViewModel(
    application: Application,
    val partner: ChatUser
) : AndroidViewModel(application) {

    private val chatRepository: ChatRepository = (application as AjesChatApp).chatRepository

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        loadMessages()
        startPolling()
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            chatRepository.getMessages(partner.id)
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        messages = list,
                        loading = false,
                        error = null
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = it.message ?: "Failed to load messages"
                    )
                }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(4000L)
                chatRepository.getMessages(partner.id).onSuccess { list ->
                    _uiState.value = _uiState.value.copy(messages = list)
                }
            }
        }
    }

    fun updateInput(s: String) {
        _uiState.value = _uiState.value.copy(inputText = s, error = null)
    }

    fun sendMessage() {
        val content = _uiState.value.inputText.trim()
        if (content.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sending = true, inputText = "", error = null)
            chatRepository.send(partner.id, content)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(sending = false)
                    loadMessages()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        sending = false,
                        inputText = content,
                        error = it.message ?: "Failed to send"
                    )
                }
        }
    }

    fun unsendForMe(messageId: Int) {
        viewModelScope.launch {
            chatRepository.unsend(messageId, "me", partner.id).onSuccess {
                loadMessages()
            }
        }
    }

    fun unsendForEveryone(messageId: Int) {
        viewModelScope.launch {
            chatRepository.unsend(messageId, "all", partner.id).onSuccess {
                loadMessages()
            }
        }
    }
}
