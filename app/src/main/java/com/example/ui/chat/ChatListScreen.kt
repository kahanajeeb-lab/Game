package com.example.ui.chat

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseChatRepository
import com.example.data.model.UserProfile
import com.example.ui.theme.WhatsAppDarkBg
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppDivider
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    repository: FirebaseChatRepository,
    onSelectChat: (UserProfile) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenConnections: () -> Unit,
    onLockToStealth: () -> Unit,
    currentWallpaper: ChatWallpaper,
    onSelectWallpaper: (ChatWallpaper) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by repository.currentUserProfile.collectAsState()
    val friends by repository.observeFriends().collectAsState(initial = emptyList())
    val friendRequests by repository.observeFriendRequests().collectAsState(initial = emptyList())

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var selectedFilterTab by remember { mutableIntStateOf(0) }

    val filterTabs = listOf("Chats", "Connections", "Unread")

    val filteredFriends = friends.filter {
        it.displayName.contains(searchQuery, ignoreCase = true) ||
                it.chatId.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(WhatsAppDarkSurface)) {
                TopAppBar(
                    title = {
                        if (isSearchActive) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search encrypted chats...", color = WhatsAppTextSecondary, fontSize = 14.sp) },
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        isSearchActive = false
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = WhatsAppTextSecondary)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chat_search_input")
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SecureChat",
                                    fontWeight = FontWeight.Bold,
                                    color = WhatsAppTextPrimary,
                                    fontSize = 20.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(WhatsAppGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = userProfile?.chatId ?: "ONLINE",
                                        color = WhatsAppLightGreen,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (!isSearchActive) {
                            IconButton(onClick = { isSearchActive = true }, modifier = Modifier.testTag("search_chats_button")) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = WhatsAppTextPrimary)
                            }
                            IconButton(onClick = onLockToStealth, modifier = Modifier.testTag("stealth_lock_action")) {
                                Icon(Icons.Default.Lock, contentDescription = "Stealth Lock", tint = WhatsAppGreen)
                            }
                            IconButton(onClick = { showMenu = true }, modifier = Modifier.testTag("chat_list_overflow_menu")) {
                                BadgedBox(
                                    badge = {
                                        if (friendRequests.isNotEmpty()) {
                                            Badge(containerColor = WhatsAppLightGreen) {
                                                Text(friendRequests.size.toString(), color = Color(0xFF0B141A))
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = WhatsAppTextPrimary)
                                }
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(WhatsAppDarkSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Profile & Security", color = WhatsAppTextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WhatsAppGreen) },
                                    onClick = {
                                        showMenu = false
                                        onOpenProfile()
                                    },
                                    modifier = Modifier.testTag("menu_profile")
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Connections", color = WhatsAppTextPrimary)
                                            if (friendRequests.isNotEmpty()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(WhatsAppLightGreen)
                                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = friendRequests.size.toString(),
                                                        color = Color(0xFF0B141A),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = WhatsAppGreen) },
                                    onClick = {
                                        showMenu = false
                                        onOpenConnections()
                                    },
                                    modifier = Modifier.testTag("menu_connections")
                                )
                                DropdownMenuItem(
                                    text = { Text("Chat Wallpaper Theme", color = WhatsAppTextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = WhatsAppGreen) },
                                    onClick = {
                                        showMenu = false
                                        showThemeDialog = true
                                    },
                                    modifier = Modifier.testTag("menu_wallpaper")
                                )
                                HorizontalDivider(color = WhatsAppDivider)
                                DropdownMenuItem(
                                    text = { Text("Lock Stealth Mode", color = WhatsAppGreen, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WhatsAppGreen) },
                                    onClick = {
                                        showMenu = false
                                        onLockToStealth()
                                    },
                                    modifier = Modifier.testTag("menu_stealth_lock")
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WhatsAppDarkSurface)
                )

                // Category filter tabs
                TabRow(
                    selectedTabIndex = selectedFilterTab,
                    containerColor = WhatsAppDarkSurface,
                    contentColor = WhatsAppGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedFilterTab]),
                            color = WhatsAppGreen
                        )
                    }
                ) {
                    filterTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedFilterTab == index,
                            onClick = { selectedFilterTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedFilterTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedFilterTab == index) WhatsAppGreen else WhatsAppTextSecondary,
                                        fontSize = 14.sp
                                    )
                                    if (index == 1 && friendRequests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(WhatsAppLightGreen)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text("${friendRequests.size}", fontSize = 10.sp, color = Color(0xFF0B141A), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenConnections,
                containerColor = WhatsAppGreen,
                contentColor = Color(0xFF0B141A),
                modifier = Modifier.testTag("new_chat_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "New Chat"
                )
            }
        },
        containerColor = WhatsAppDarkBg,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // E2EE Disclaimer
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF141F24))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Encrypted",
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Realtime Database E2EE sync with delivery status ticks.",
                        fontSize = 12.sp,
                        color = WhatsAppTextSecondary
                    )
                }
            }

            if (filteredFriends.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F2C34)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "No chats",
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No conversations match '$searchQuery'" else "No Active Chats",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add contacts using their Unique Chat ID or accept pending connection requests to start chatting.",
                            fontSize = 13.sp,
                            color = WhatsAppTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onOpenConnections,
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Manage Connections", color = Color(0xFF0B141A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(filteredFriends, key = { it.uid }) { friend ->
                    ChatListItem(
                        friend = friend,
                        onClick = { onSelectChat(friend) }
                    )
                    HorizontalDivider(
                        color = WhatsAppDivider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 78.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Wallpaper Picker Dialog
        if (showThemeDialog) {
            Card(
                modifier = Modifier
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Choose Chat Wallpaper Theme",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ChatWallpaper.entries.forEach { wallpaper ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentWallpaper == wallpaper) Color(0xFF1F2C34) else Color.Transparent)
                                .clickable {
                                    onSelectWallpaper(wallpaper)
                                    showThemeDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(wallpaper.bgColor)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = wallpaper.displayName,
                                color = if (currentWallpaper == wallpaper) WhatsAppGreen else WhatsAppTextPrimary,
                                fontWeight = if (currentWallpaper == wallpaper) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    friend: UserProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_item_${friend.uid}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with online status badge
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF008069)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.displayName.take(1).uppercase().ifBlank { "U" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            if (friend.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(WhatsAppLightGreen)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = friend.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WhatsAppTextPrimary,
                    maxLines = 1
                )
                Text(
                    text = if (friend.isOnline) "online" else formatLastSeenTime(friend.lastSeen),
                    fontSize = 12.sp,
                    color = if (friend.isOnline) WhatsAppLightGreen else WhatsAppTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Delivery Tick in preview
                DeliveryStatusTicks(
                    status = "read",
                    modifier = Modifier.padding(end = 4.dp)
                )

                Text(
                    text = friend.about.ifBlank { "Encrypted connection established." },
                    fontSize = 13.sp,
                    color = WhatsAppTextSecondary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

fun formatLastSeenTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
