package com.example.ui.contacts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.data.model.FriendRequest
import com.example.data.model.UserProfile
import com.example.ui.theme.WhatsAppDarkBg
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppDivider
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    repository: FirebaseChatRepository,
    onBack: () -> Unit,
    onSelectContact: (UserProfile) -> Unit,
    onStartVideoCall: (UserProfile) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val myProfile by repository.currentUserProfile.collectAsState()
    val friends by repository.observeFriends().collectAsState(initial = emptyList())
    val friendRequests by repository.observeFriendRequests().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Connections", "Requests", "Add by ID")

    // Search by ID state
    var searchIdInput by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var foundUserProfile by remember { mutableStateOf<UserProfile?>(null) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var isSendingRequest by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Copied $text to clipboard")
        }
    }

    fun shareChatId(chatId: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Connect with me on Retro Dash Secure Messenger with my Unique ID: $chatId")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Unique Chat ID")
        context.startActivity(shareIntent)
    }

    fun performSearch(idToSearch: String) {
        val clean = idToSearch.trim().uppercase()
        if (clean.isBlank()) return
        isSearching = true
        foundUserProfile = null
        searchError = null
        coroutineScope.launch {
            val user = repository.findUserByChatId(clean)
            isSearching = false
            if (user != null) {
                if (user.uid == myProfile?.uid) {
                    searchError = "This is your own Unique Chat ID."
                } else {
                    foundUserProfile = user
                }
            } else {
                searchError = "No registered user found with ID: $clean"
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(WhatsAppDarkSurface)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Friend Requests & IDs",
                            color = WhatsAppTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("contacts_back_button")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = WhatsAppTextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WhatsAppDarkSurface)
                )

                // Sub-tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = WhatsAppDarkSurface,
                    contentColor = WhatsAppGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = WhatsAppGreen
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) WhatsAppGreen else WhatsAppTextSecondary,
                                        fontSize = 13.sp
                                    )
                                    if (index == 1 && friendRequests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(WhatsAppLightGreen)
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${friendRequests.size}",
                                                color = Color(0xFF0B141A),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.testTag("contacts_subtab_$index")
                        )
                    }
                }
            }
        },
        containerColor = WhatsAppDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // My Unique ID Hero Card (visible in all tabs for convenience)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "My ID",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MY UNIQUE CHAT ID",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = myProfile?.chatId ?: "Generating...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = WhatsAppLightGreen,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            myProfile?.chatId?.let { copyToClipboard(it, "Chat ID") }
                        },
                        modifier = Modifier.testTag("copy_my_id_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy ID",
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            myProfile?.chatId?.let { shareChatId(it) }
                        },
                        modifier = Modifier.testTag("share_my_id_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share ID",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: MUTUAL CONNECTIONS LIST
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ACTIVE CONNECTIONS (${friends.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WhatsAppTextSecondary,
                                    letterSpacing = 1.sp
                                )

                                Text(
                                    text = "Tap to chat",
                                    fontSize = 12.sp,
                                    color = WhatsAppGreen
                                )
                            }
                        }

                        if (friends.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = "No connections",
                                            tint = WhatsAppGreen,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No connections yet",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WhatsAppTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Switch to the 'Add by ID' tab to send a secret connection request using your friend's Unique ID number.",
                                            fontSize = 13.sp,
                                            color = WhatsAppTextSecondary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { selectedTab = 2 },
                                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Find by ID", color = Color(0xFF0B141A), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            items(friends, key = { it.uid }) { friend ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onSelectContact(friend) }
                                        .testTag("friend_item_${friend.uid}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF008069)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = friend.displayName.take(1).uppercase().ifBlank { "U" },
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = friend.displayName,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = WhatsAppTextPrimary
                                            )
                                            Text(
                                                text = "ID: ${friend.chatId}",
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = WhatsAppLightGreen
                                            )
                                        }

                                        IconButton(
                                            onClick = { onStartVideoCall(friend) },
                                            modifier = Modifier.testTag("video_call_friend_${friend.uid}")
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Videocam,
                                                contentDescription = "Video Call",
                                                tint = WhatsAppGreen,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val res = repository.removeFriend(friend.uid)
                                                    res.onSuccess {
                                                        snackbarHostState.showSnackbar("Disconnected with ${friend.displayName}")
                                                    }
                                                }
                                            },
                                            modifier = Modifier.testTag("remove_friend_${friend.uid}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Disconnect",
                                                tint = Color(0xFF6B7280),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: INCOMING & OUTGOING REQUESTS
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "PENDING INVITATIONS (${friendRequests.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppGreen,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        if (friendRequests.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = "Inbox clear",
                                            tint = WhatsAppGreen,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "No Pending Requests",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WhatsAppTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "When someone enters your Unique Chat ID, their encrypted request will arrive here for your approval.",
                                            fontSize = 13.sp,
                                            color = WhatsAppTextSecondary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(friendRequests, key = { it.id }) { req ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00A884)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = req.fromName.take(1).uppercase().ifBlank { "U" },
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = req.fromName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = WhatsAppTextPrimary
                                            )
                                            Text(
                                                text = "ID: ${req.fromChatId}",
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = WhatsAppLightGreen
                                            )
                                        }

                                        Row {
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        val res = repository.respondToRequest(req, accept = true)
                                                        res.onSuccess {
                                                            snackbarHostState.showSnackbar("Connected with ${req.fromName}!")
                                                        }.onFailure { err ->
                                                            snackbarHostState.showSnackbar(err.localizedMessage ?: "Failed")
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("accept_req_${req.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Accept",
                                                    tint = Color(0xFF0B141A),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Accept", color = Color(0xFF0B141A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            OutlinedButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        repository.respondToRequest(req, accept = false)
                                                        snackbarHostState.showSnackbar("Request declined")
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("reject_req_${req.id}")
                                            ) {
                                                Text("Reject", color = Color(0xFFEF5350), fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: ADD BY UNIQUE ID (Live search & connection request sender)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "SEND CONNECTION INVITATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppTextSecondary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Search by Chat ID",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WhatsAppTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Input your contact's Unique ID (e.g. SC-772101 or 772101) to verify profile and send a confidential invitation.",
                                        fontSize = 12.sp,
                                        color = WhatsAppTextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = searchIdInput,
                                            onValueChange = { searchIdInput = it.uppercase() },
                                            placeholder = { Text("SC-XXXXXX", color = WhatsAppTextSecondary) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = WhatsAppGreen,
                                                unfocusedBorderColor = Color(0xFF2A3942),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("search_chat_id_input")
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { performSearch(searchIdInput) },
                                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("search_user_button")
                                        ) {
                                            if (isSearching) {
                                                CircularProgressIndicator(color = Color(0xFF0B141A), modifier = Modifier.size(18.dp))
                                            } else {
                                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF0B141A))
                                            }
                                        }
                                    }

                                    // Quick demo ID chips for rapid testing
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Quick Test Profiles:", fontSize = 11.sp, color = WhatsAppTextSecondary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val testIds = listOf(
                                            "Hamza" to "SC-772101",
                                            "Elena" to "SC-883204",
                                            "Marcus" to "SC-441920"
                                        )
                                        items(testIds) { (name, id) ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF1F2C34))
                                                    .clickable {
                                                        searchIdInput = id
                                                        performSearch(id)
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("$name ($id)", color = WhatsAppLightGreen, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Search Result Preview Card
                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            searchError?.let { err ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1618)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = err,
                                        color = Color(0xFFFF8A80),
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }

                            foundUserProfile?.let { user ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Verified User Found:",
                                            fontSize = 12.sp,
                                            color = WhatsAppGreen,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF00A884)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = user.displayName.take(1).uppercase().ifBlank { "U" },
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = user.displayName,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WhatsAppTextPrimary
                                                )
                                                Text(
                                                    text = user.chatId,
                                                    fontSize = 13.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = WhatsAppLightGreen
                                                )
                                                Text(
                                                    text = user.about,
                                                    fontSize = 12.sp,
                                                    color = WhatsAppTextSecondary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                isSendingRequest = true
                                                coroutineScope.launch {
                                                    val res = repository.sendFriendRequest(user.chatId)
                                                    isSendingRequest = false
                                                    res.onSuccess { msg ->
                                                        snackbarHostState.showSnackbar(msg)
                                                        foundUserProfile = null
                                                        searchIdInput = ""
                                                    }.onFailure { err ->
                                                        snackbarHostState.showSnackbar(err.localizedMessage ?: "Failed to send request")
                                                    }
                                                }
                                            },
                                            enabled = !isSendingRequest,
                                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("confirm_send_request_button")
                                        ) {
                                            if (isSendingRequest) {
                                                CircularProgressIndicator(color = Color(0xFF0B141A), modifier = Modifier.size(20.dp))
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.PersonAdd,
                                                    contentDescription = "Send",
                                                    tint = Color(0xFF0B141A)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Send Encrypted Invitation",
                                                    color = Color(0xFF0B141A),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
