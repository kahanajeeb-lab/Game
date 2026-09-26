package com.example.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseChatRepository
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.example.ui.voice.VoiceMessageBottomSheet
import com.example.ui.voice.VoiceMessageBubble
import com.example.ui.theme.WhatsAppDarkBg
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    repository: FirebaseChatRepository,
    friend: UserProfile,
    wallpaper: ChatWallpaper,
    onBack: () -> Unit,
    onStartVideoCall: () -> Unit,
    onLockToStealth: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val myUid = repository.currentUserId
    val messages by repository.observeMessages(friend.uid).collectAsState(initial = emptyList())
    val isFriendTyping by repository.observeTyping(friend.uid).collectAsState(initial = false)
    val presence by repository.observeUserPresence(friend.uid).collectAsState(initial = Pair(friend.isOnline, friend.lastSeen))

    var messageText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showVoiceMessageSheet by remember { mutableStateOf(false) }

    // Scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Typing status listener
    LaunchedEffect(messageText) {
        if (messageText.isNotBlank()) {
            repository.setTyping(friend.uid, true)
            delay(2500)
            repository.setTyping(friend.uid, false)
        } else {
            repository.setTyping(friend.uid, false)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            repository.setTyping(friend.uid, false)
        }
    }

    fun sendMessage() {
        val text = messageText.trim()
        if (text.isBlank()) return
        messageText = ""
        coroutineScope.launch {
            repository.sendMessage(friend.uid, text)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* Could view user info */ }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF008069)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.displayName.take(1).uppercase().ifBlank { "U" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = friend.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WhatsAppTextPrimary,
                                maxLines = 1
                            )
                            val subtitle = when {
                                isFriendTyping -> "typing..."
                                presence.first -> "online"
                                presence.second > 0 -> "last seen " + formatLastSeenTime(presence.second)
                                else -> "ID: " + friend.chatId
                            }
                            Text(
                                text = subtitle,
                                fontSize = 12.sp,
                                color = if (isFriendTyping || presence.first) WhatsAppLightGreen else WhatsAppTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("conversation_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WhatsAppTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Secure voice call channel ready")
                        }
                    }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = WhatsAppTextPrimary)
                    }
                    IconButton(
                        onClick = onStartVideoCall,
                        modifier = Modifier.testTag("conversation_videocam_button")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = WhatsAppTextPrimary)
                    }
                    IconButton(onClick = onLockToStealth, modifier = Modifier.testTag("conversation_stealth_lock")) {
                        Icon(Icons.Default.Lock, contentDescription = "Stealth Lock", tint = WhatsAppGreen)
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = WhatsAppTextPrimary)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(WhatsAppDarkSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Contact info", color = WhatsAppTextPrimary) },
                            onClick = {
                                showMenu = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("${friend.displayName} • ${friend.chatId}")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lock to Stealth Mode", color = WhatsAppGreen) },
                            onClick = {
                                showMenu = false
                                onLockToStealth()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WhatsAppDarkSurface)
            )
        },
        containerColor = WhatsAppDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.imePadding()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // WhatsApp Doodle Wallpaper Background
            ChatBackgroundCanvas(wallpaper = wallpaper)

            Column(modifier = Modifier.fillMaxSize()) {
                // Message List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    item {
                        // End-to-end encryption pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF182229))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🔒 Messages are end-to-end encrypted with Firebase Realtime Database. Zero unencrypted central logs.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFD279),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    items(messages, key = { it.id }) { msg ->
                        val isMe = msg.senderId == myUid
                        MessageBubble(
                            message = msg,
                            isMe = isMe,
                            wallpaper = wallpaper
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (isFriendTyping) {
                        item {
                            TypingBubble(friendName = friend.displayName)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // WhatsApp Chat Input Bar
                ChatBottomInputBar(
                    text = messageText,
                    onTextChanged = { messageText = it },
                    onSend = { sendMessage() },
                    onMicClick = { showVoiceMessageSheet = true },
                    onAttach = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Encrypted file vault attachment selector")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showVoiceMessageSheet) {
        VoiceMessageBottomSheet(
            recipientName = friend.displayName,
            onDismiss = { showVoiceMessageSheet = false },
            onSendVoiceMessage = { durationSec, presetName, audioDataUri ->
                coroutineScope.launch {
                    repository.sendVoiceMessage(
                        friendUid = friend.uid,
                        durationSec = durationSec,
                        presetName = presetName,
                        audioDataUri = audioDataUri
                    )
                    snackbarHostState.showSnackbar("10s Girl Voice Message sent! 🌸")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    wallpaper: ChatWallpaper,
    modifier: Modifier = Modifier
) {
    if (message.isVoiceMessage) {
        val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = alignment
        ) {
            VoiceMessageBubble(
                message = message,
                isMe = isMe
            )
        }
        return
    }

    val bubbleColor = if (isMe) wallpaper.outgoingBubbleColor else wallpaper.incomingBubbleColor
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .testTag("message_bubble_${message.id}"),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = Color.White,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        fontSize = 11.sp,
                        color = Color(0xB3FFFFFF)
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        // Real-time Delivery Status Ticks (1 tick = sent, 2 ticks = delivered, blue ticks = read)
                        DeliveryStatusTicks(
                            status = message.status,
                            modifier = Modifier.testTag("tick_${message.status}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingBubble(friendName: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2C34))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$friendName is typing...",
                    fontSize = 12.sp,
                    color = WhatsAppLightGreen,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun ChatBottomInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    onAttach: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(WhatsAppDarkBg)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pill input container
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(WhatsAppDarkSurface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SentimentSatisfiedAlt,
                contentDescription = "Emoji",
                tint = WhatsAppTextSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* Emoji toggle */ }
            )

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = text,
                onValueChange = onTextChanged,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(WhatsAppGreen),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = "Message...",
                            color = WhatsAppTextSecondary,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onAttach, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach",
                    tint = WhatsAppTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (text.isBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = WhatsAppTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // WhatsApp Round Green Send / Mic FAB
        val isSend = text.isNotBlank()
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isSend) WhatsAppGreen else Color(0xFFFF5E8A))
                .clickable {
                    if (isSend) onSend() else onMicClick()
                }
                .testTag("chat_send_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSend) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                contentDescription = if (isSend) "Send" else "10s Girl Voice Message",
                tint = if (isSend) Color(0xFF0B141A) else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

fun formatMessageTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
