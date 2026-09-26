package com.example.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val phone: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val chatId: String = "",
    val about: String = "Hey there! I am using SecureChat.",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
)

data class FriendRequest(
    val id: String = "",
    val fromUid: String = "",
    val fromName: String = "",
    val fromAvatar: String = "",
    val fromChatId: String = "",
    val toUid: String = "",
    val status: String = "pending", // "pending", "accepted", "rejected"
    val timestamp: Long = 0L
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val status: String = "sent", // "sent", "delivered", "read"
    val isVoiceMessage: Boolean = false,
    val voiceDurationSec: Int = 0,
    val voicePresetName: String = "",
    val audioDataUri: String = ""
)

data class ChatSummary(
    val friend: UserProfile,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val lastMessageStatus: String = "sent"
)
