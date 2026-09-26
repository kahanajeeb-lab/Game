package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.firebase.FirebaseChatRepository
import com.example.data.model.UserProfile
import com.example.ui.auth.AuthScreen
import com.example.ui.call.VideoCallScreen
import com.example.ui.chat.ChatListScreen
import com.example.ui.chat.ChatWallpaper
import com.example.ui.chat.ConversationScreen
import com.example.ui.contacts.ContactsScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.stealth.StealthLockScreen
import com.example.ui.theme.SecureChatTheme

sealed interface Screen {
    data object ChatList : Screen
    data class Conversation(val friend: UserProfile) : Screen
    data class VideoCall(val friend: UserProfile) : Screen
    data object Contacts : Screen
    data object Profile : Screen
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: FirebaseChatRepository
    private val isStealthUnlocked = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = FirebaseChatRepository(applicationContext)

        setContent {
            SecureChatTheme(darkTheme = true) {
                SecureChatApp(
                    repository = repository,
                    isStealthUnlocked = isStealthUnlocked.value,
                    onUnlockStealth = { isStealthUnlocked.value = true },
                    onLockStealth = { isStealthUnlocked.value = false }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Key Requirement: If the app is closed, minimized, or the phone screen turns off,
        // it instantly resets to the lock state.
        isStealthUnlocked.value = false
    }
}

@Composable
fun SecureChatApp(
    repository: FirebaseChatRepository,
    isStealthUnlocked: Boolean,
    onUnlockStealth: () -> Unit,
    onLockStealth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUserProfile by repository.currentUserProfile.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.ChatList) }
    var currentWallpaper by remember { mutableStateOf(ChatWallpaper.WHATSAPP_DOODLE) }

    AnimatedContent(
        targetState = isStealthUnlocked,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "StealthTransition",
        modifier = modifier.fillMaxSize()
    ) { unlocked ->
        if (!unlocked) {
            // Stealth Decoy Screen (Fake system crash / zero-indicator hold & swipe)
            StealthLockScreen(
                onUnlock = onUnlockStealth,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Hidden Chat Dashboard
            if (!repository.isUserLoggedIn && currentUserProfile == null) {
                AuthScreen(
                    repository = repository,
                    onAuthSuccess = {
                        currentScreen = Screen.ChatList
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                when (val screen = currentScreen) {
                    is Screen.ChatList -> {
                        BackHandler {
                            // Pressing back on chat list locks to stealth mode
                            onLockStealth()
                        }
                        ChatListScreen(
                            repository = repository,
                            onSelectChat = { friend ->
                                currentScreen = Screen.Conversation(friend)
                            },
                            onOpenProfile = { currentScreen = Screen.Profile },
                            onOpenConnections = { currentScreen = Screen.Contacts },
                            onLockToStealth = onLockStealth,
                            currentWallpaper = currentWallpaper,
                            onSelectWallpaper = { currentWallpaper = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is Screen.Conversation -> {
                        ConversationScreen(
                            repository = repository,
                            friend = screen.friend,
                            wallpaper = currentWallpaper,
                            onBack = { currentScreen = Screen.ChatList },
                            onStartVideoCall = { currentScreen = Screen.VideoCall(screen.friend) },
                            onLockToStealth = onLockStealth,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is Screen.VideoCall -> {
                        VideoCallScreen(
                            repository = repository,
                            friend = screen.friend,
                            onEndCall = { currentScreen = Screen.Conversation(screen.friend) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is Screen.Contacts -> {
                        ContactsScreen(
                            repository = repository,
                            onBack = { currentScreen = Screen.ChatList },
                            onSelectContact = { friend ->
                                currentScreen = Screen.Conversation(friend)
                            },
                            onStartVideoCall = { friend ->
                                currentScreen = Screen.VideoCall(friend)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is Screen.Profile -> {
                        ProfileScreen(
                            repository = repository,
                            onBack = { currentScreen = Screen.ChatList },
                            onLockApp = onLockStealth,
                            onSignedOut = {
                                currentScreen = Screen.ChatList
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
