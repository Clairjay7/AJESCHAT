package com.example.ajeschat.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as lazyGridItems
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import com.example.ajeschat.data.ChatMessage
import com.example.ajeschat.ui.theme.AjesGreen

private const val UNSENT_PLACEHOLDER = "The message was unsent for everyone."

private val QUICK_EMOJIS = listOf(
    "😀", "😃", "😄", "😁", "😅", "😂", "🤣", "😊",
    "😍", "🥰", "😘", "😎", "🤔", "😢", "😭", "😡",
    "👍", "👎", "🙏", "👏", "🤝", "💪", "🔥", "✨",
    "❤️", "💯", "✅", "⭐", "🎉", "🎓", "📚", "✏️",
    "📷", "📎", "💬", "📢", "⏰", "☀️", "🌙", "🌸"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val partner = viewModel.partner
    val context = LocalContext.current
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEmojiSheet by remember { mutableStateOf(false) }
    val emojiSheetState = rememberModalBottomSheetState()
    var pendingCameraCapture by remember { mutableStateOf(false) }
    var pendingMicStart by remember { mutableStateOf(false) }
    val latestCameraUri = remember { mutableStateOf<Uri?>(null) }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) viewModel.setPendingAttachment(uri)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) latestCameraUri.value?.let { viewModel.setPendingAttachment(it) }
    }

    val requestCameraPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingCameraCapture) {
            pendingCameraCapture = false
            val dir = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            latestCameraUri.value = uri
            takePictureLauncher.launch(uri)
        } else {
            pendingCameraCapture = false
        }
    }

    val requestMicPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingMicStart) {
            pendingMicStart = false
            viewModel.startVoiceRecording()
        } else {
            pendingMicStart = false
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.partnerTyping) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(partner.name)
                        if (!partner.role.isNullOrBlank()) {
                            Text(
                                partner.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete conversation") },
                                onClick = {
                                    showOverflowMenu = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                if (uiState.partnerTyping) {
                    Text(
                        "${partner.name} is typing…",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                if (uiState.isRecordingVoice) {
                    Text(
                        "Recording… tap the mic again to stop and attach",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
                if (uiState.pendingAttachmentUri != null) {
                    val seg = uiState.pendingAttachmentUri?.lastPathSegment.orEmpty()
                    val pendingLabel = when {
                        seg.contains("voice_", ignoreCase = true) -> "Voice clip ready — tap Send (optional caption above)"
                        seg.endsWith(".jpg", ignoreCase = true) ||
                            seg.endsWith(".jpeg", ignoreCase = true) -> "Photo ready — tap Send"
                        else -> "File attached — tap Send"
                    }
                    Text(
                        pendingLabel,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
                if (uiState.error != null) {
                    Text(
                        uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    IconButton(
                        onClick = { showEmojiSheet = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Filled.Mood, contentDescription = "Emoji")
                    }
                    IconButton(
                        onClick = { pickMedia.launch("*/*") },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Filled.AttachFile, contentDescription = "Attach file")
                    }
                    IconButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) !=
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                pendingCameraCapture = true
                                requestCameraPerm.launch(Manifest.permission.CAMERA)
                            } else {
                                val dir = File(context.cacheDir, "images").apply { mkdirs() }
                                val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                latestCameraUri.value = uri
                                takePictureLauncher.launch(uri)
                            }
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Camera")
                    }
                    IconButton(
                        onClick = {
                            if (uiState.isRecordingVoice) {
                                viewModel.stopVoiceRecordingAndAttach()
                            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                pendingMicStart = true
                                requestMicPerm.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.startVoiceRecording()
                            }
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = if (uiState.isRecordingVoice) "Stop recording" else "Voice message",
                            tint = if (uiState.isRecordingVoice) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::updateInput,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        placeholder = { Text("Message") },
                        minLines = 1,
                        maxLines = 4
                    )
                    TextButton(
                        onClick = viewModel::sendMessage,
                        enabled = !uiState.sending && !uiState.isRecordingVoice &&
                            (uiState.inputText.isNotBlank() || uiState.pendingAttachmentUri != null)
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            reverseLayout = false,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(uiState.messages, key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    onUnsendForMe = { viewModel.unsendForMe(msg.id) },
                    onUnsendForEveryone = if (msg.isMine) ({ viewModel.unsendForEveryone(msg.id) }) else null
                )
            }
        }
    }

    if (showEmojiSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEmojiSheet = false },
            sheetState = emojiSheetState
        ) {
            Text(
                "Emoji",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .padding(horizontal = 8.dp)
            ) {
                lazyGridItems(QUICK_EMOJIS, key = { it }) { emoji ->
                    TextButton(
                        onClick = {
                            viewModel.appendToInput(emoji)
                            showEmojiSheet = false
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Text(emoji, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete conversation?") },
            text = {
                Text(
                    "All messages with ${partner.name} will be removed. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteConversation(onSuccess = onBack)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: ChatMessage,
    onUnsendForMe: () -> Unit,
    onUnsendForEveryone: (() -> Unit)?
) {
    var showMenu by remember { mutableStateOf(false) }
    val isMine = message.isMine
    val displayText = if (message.unsentForAll) UNSENT_PLACEHOLDER else message.content
    val url = message.attachmentUrl?.trim()?.takeIf { it.isNotEmpty() }
    val type = message.attachmentType?.lowercase()

    // Outgoing: [ flex ][ bubble ][ menu ] — incoming: [ bubble ][ menu ][ flex ]
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        if (isMine) {
            Spacer(Modifier.weight(1f))
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (isMine) 12.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 12.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMine) AjesGreen else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .combinedClickable(
                        onClick = { },
                        onLongClick = { if (!message.unsentForAll) showMenu = true }
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!message.unsentForAll && url != null && type == "image") {
                        AsyncImage(
                            model = url,
                            contentDescription = message.attachmentName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(8.dp))
                    } else if (!message.unsentForAll && url != null && type != null) {
                        Text(
                            "Attachment (${type}): ${message.attachmentName ?: "file"}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    if (displayText.isNotBlank()) {
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = message.createdAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            if (!message.unsentForAll) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Unsend for me") },
                            onClick = {
                                showMenu = false
                                onUnsendForMe()
                            }
                        )
                        onUnsendForEveryone?.let { handler ->
                            DropdownMenuItem(
                                text = { Text("Unsend for everyone") },
                                onClick = {
                                    showMenu = false
                                    handler()
                                }
                            )
                        }
                    }
                }
            }
        }
        if (!isMine) {
            Spacer(Modifier.weight(1f))
        }
    }
}
