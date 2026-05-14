package com.example.ajeschat.ui.chat

import android.app.Application
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
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
import java.io.File

data class ConversationUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val loading: Boolean = false,
    val sending: Boolean = false,
    val error: String? = null,
    val partnerTyping: Boolean = false,
    val pendingAttachmentUri: Uri? = null,
    val isRecordingVoice: Boolean = false
)

class ConversationViewModel(
    application: Application,
    val partner: ChatUser
) : AndroidViewModel(application) {

    private val chatRepository: ChatRepository = (application as AjesChatApp).chatRepository

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var typingPollJob: Job? = null
    private var typingDebounce: Job? = null
    private var mediaRecorder: MediaRecorder? = null
    private var voiceOutputFile: File? = null

    init {
        loadMessages()
        startPolling()
        startTypingPoll()
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        typingPollJob?.cancel()
        typingDebounce?.cancel()
        discardVoiceRecordingInternal()
        viewModelScope.launch {
            runCatching { chatRepository.setTyping(partner.id, false) }
        }
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

    private fun startTypingPoll() {
        typingPollJob?.cancel()
        typingPollJob = viewModelScope.launch {
            while (isActive) {
                delay(2000L)
                chatRepository.getPartnerTyping(partner.id).onSuccess { typing ->
                    _uiState.value = _uiState.value.copy(partnerTyping = typing)
                }
            }
        }
    }

    fun updateInput(s: String) {
        _uiState.value = _uiState.value.copy(inputText = s, error = null)
        scheduleTypingPing(s.isNotBlank())
    }

    fun appendToInput(suffix: String) {
        if (suffix.isEmpty()) return
        val next = _uiState.value.inputText + suffix
        _uiState.value = _uiState.value.copy(inputText = next, error = null)
        scheduleTypingPing(next.isNotBlank())
    }

    private fun scheduleTypingPing(typing: Boolean) {
        typingDebounce?.cancel()
        if (!typing) {
            viewModelScope.launch {
                runCatching { chatRepository.setTyping(partner.id, false) }
            }
            return
        }
        typingDebounce = viewModelScope.launch {
            delay(450)
            if (isActive) {
                runCatching { chatRepository.setTyping(partner.id, true) }
            }
        }
    }

    fun setPendingAttachment(uri: Uri?) {
        _uiState.value = _uiState.value.copy(pendingAttachmentUri = uri)
    }

    fun startVoiceRecording() {
        if (_uiState.value.isRecordingVoice) return
        discardVoiceRecordingInternal()
        val app = getApplication<Application>()
        val file = File(app.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        voiceOutputFile = file
        val result = runCatching {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(app)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder
        }
        if (result.isSuccess) {
            _uiState.value = _uiState.value.copy(isRecordingVoice = true, error = null)
        } else {
            discardVoiceRecordingInternal()
            _uiState.value = _uiState.value.copy(
                error = result.exceptionOrNull()?.message ?: "Could not start microphone"
            )
        }
    }

    fun stopVoiceRecordingAndAttach() {
        if (!_uiState.value.isRecordingVoice) return
        runCatching { mediaRecorder?.stop() }
        runCatching { mediaRecorder?.release() }
        mediaRecorder = null
        val file = voiceOutputFile
        voiceOutputFile = null
        _uiState.value = _uiState.value.copy(isRecordingVoice = false)
        if (file != null && file.exists() && file.length() > 400) {
            val uri = FileProvider.getUriForFile(
                getApplication(),
                "${getApplication<Application>().packageName}.fileprovider",
                file
            )
            _uiState.value = _uiState.value.copy(pendingAttachmentUri = uri)
        } else {
            file?.delete()
        }
    }

    private fun discardVoiceRecordingInternal() {
        runCatching { mediaRecorder?.stop() }
        runCatching { mediaRecorder?.release() }
        mediaRecorder = null
        voiceOutputFile?.delete()
        voiceOutputFile = null
        if (_uiState.value.isRecordingVoice) {
            _uiState.value = _uiState.value.copy(isRecordingVoice = false)
        }
    }

    fun cancelVoiceRecording() {
        discardVoiceRecordingInternal()
    }

    fun sendMessage() {
        val content = _uiState.value.inputText.trim()
        val uri = _uiState.value.pendingAttachmentUri
        if (content.isEmpty() && uri == null) return
        viewModelScope.launch {
            runCatching { chatRepository.setTyping(partner.id, false) }
            _uiState.value = _uiState.value.copy(sending = true, inputText = "", pendingAttachmentUri = null, error = null)
            val savedContent = content
            chatRepository.send(partner.id, savedContent, uri)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(sending = false)
                    loadMessages()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        sending = false,
                        inputText = savedContent,
                        pendingAttachmentUri = uri,
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

    fun deleteConversation(onSuccess: () -> Unit) {
        viewModelScope.launch {
            pollingJob?.cancel()
            pollingJob = null
            typingPollJob?.cancel()
            typingPollJob = null
            chatRepository.deleteConversation(partner.id)
                .onSuccess {
                    _uiState.value = ConversationUiState()
                    onSuccess()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message ?: "Failed to delete conversation"
                    )
                    startPolling()
                    startTypingPoll()
                }
        }
    }
}
