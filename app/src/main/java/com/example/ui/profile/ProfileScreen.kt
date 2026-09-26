package com.example.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseChatRepository
import com.example.ui.theme.WhatsAppDarkBg
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: FirebaseChatRepository,
    onBack: () -> Unit,
    onLockApp: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userProfile by repository.currentUserProfile.collectAsState()

    var displayName by remember { mutableStateOf(userProfile?.displayName ?: "") }
    var about by remember { mutableStateOf(userProfile?.about ?: "Hey there! I am using SecureChat.") }
    var selectedAvatar by remember { mutableStateOf(userProfile?.avatarUrl ?: "avatar_1") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            displayName = it.displayName
            about = it.about
            selectedAvatar = it.avatarUrl
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedAvatar = uri.toString()
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Profile photo selected")
            }
        }
    }

    // Avatar color sets for stylish WhatsApp profile badges
    val avatarPresets = listOf(
        "avatar_1" to Color(0xFF00A884),
        "avatar_2" to Color(0xFF34B7F1),
        "avatar_3" to Color(0xFFE91E63),
        "avatar_4" to Color(0xFFFF9800),
        "avatar_5" to Color(0xFF9C27B0),
        "avatar_6" to Color(0xFF4CAF50),
        "avatar_7" to Color(0xFF00BCD4)
    )

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Copied $text to clipboard")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Security",
                        color = WhatsAppTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WhatsAppTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLockApp,
                        modifier = Modifier.testTag("profile_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Stealth Mode",
                            tint = WhatsAppGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WhatsAppDarkSurface)
            )
        },
        containerColor = WhatsAppDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture (DP) Avatar with edit trigger
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                val currentAvatarColor = avatarPresets.find { it.first == selectedAvatar }?.second ?: Color(0xFF00A884)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(currentAvatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.take(1).uppercase().ifBlank { "U" },
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WhatsAppGreen)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("change_dp_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change DP",
                        tint = Color(0xFF0B141A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "Choose Profile Avatar Palette:",
                fontSize = 12.sp,
                color = WhatsAppTextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            // Preset Avatars Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(avatarPresets) { (key, color) ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedAvatar == key) 3.dp else 0.dp,
                                color = if (selectedAvatar == key) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedAvatar = key }
                            .testTag("avatar_color_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAvatar == key) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Unique Chat ID Card (Key Requirement for connection requests)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Secure ID",
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Unique Chat ID",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppTextPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                userProfile?.chatId?.let { copyToClipboard(it, "Chat ID") }
                            },
                            modifier = Modifier.testTag("copy_chat_id_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy ID",
                                tint = WhatsAppLightGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = userProfile?.chatId ?: "Generating...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = WhatsAppLightGreen,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Share this Unique ID with friends. They can add you and send confidential connection requests without revealing phone or email.",
                        fontSize = 12.sp,
                        color = WhatsAppTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Name & Status Edit Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Profile Information",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppTextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Profile Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Name", tint = WhatsAppGreen)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhatsAppGreen,
                            unfocusedBorderColor = Color(0xFF2A3942),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = about,
                        onValueChange = { about = it },
                        label = { Text("About / Status") },
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = "About", tint = WhatsAppGreen)
                        },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhatsAppGreen,
                            unfocusedBorderColor = Color(0xFF2A3942),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_about_input")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (displayName.isBlank()) return@Button
                            isSaving = true
                            coroutineScope.launch {
                                val res = repository.updateProfile(
                                    name = displayName.trim(),
                                    about = about.trim(),
                                    avatarUrl = selectedAvatar
                                )
                                isSaving = false
                                res.onSuccess {
                                    snackbarHostState.showSnackbar("Profile updated successfully")
                                }.onFailure { err ->
                                    snackbarHostState.showSnackbar(err.localizedMessage ?: "Failed to update profile")
                                }
                            }
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_profile_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Save Profile Changes", fontWeight = FontWeight.Bold, color = Color(0xFF0B141A))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (Lock Stealth, Sign Out)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLockApp() }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Lock to Stealth Decoy", color = WhatsAppTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Instantly hides chat behind fake crash interface", color = WhatsAppTextSecondary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                repository.signOut()
                                onSignedOut()
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sign Out",
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Sign Out", color = Color(0xFFEF5350), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Disconnect session from this device", color = WhatsAppTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
