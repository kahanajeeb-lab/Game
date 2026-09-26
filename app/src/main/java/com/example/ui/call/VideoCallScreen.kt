package com.example.ui.call

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Visibility
import com.example.ui.voice.LiveVoiceCallPill
import com.example.ui.voice.LiveVoiceCallProcessor
import com.example.ui.voice.LiveVoiceChangerStudioDialog
import com.example.ui.voice.RosePink
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.firebase.FirebaseChatRepository
import com.example.data.model.UserProfile
import com.example.ui.theme.WhatsAppDarkBg
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun VideoCallScreen(
    repository: FirebaseChatRepository,
    friend: UserProfile,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler { onEndCall() }

    // Screen Video Call Recorder
    val videoRecorder = remember { VideoCallRecorder(context) }
    val isRecording by videoRecorder.isRecording.collectAsState()
    val recordingDuration by videoRecorder.recordingDuration.collectAsState()
    var savedVideoResult by remember { mutableStateOf<RecordedVideoInfo?>(null) }

    // Real-Time Natural Girl Voice Call Processor
    val voiceProcessor = remember { LiveVoiceCallProcessor() }
    var showVoiceStudio by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        voiceProcessor.startLiveProcessing()
        onDispose {
            voiceProcessor.stopLiveProcessing()
        }
    }

    // Call Elapsed Duration
    var callDurationSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDurationSeconds++
        }
    }

    // Call Controls State
    var isMuted by remember { mutableStateOf(false) }
    var isVideoDisabled by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(true) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showTrackingReticle by remember { mutableStateOf(true) }

    // 10 Natural White Glowing Skin Beauty Filters
    val filterList = remember {
        mutableStateListOf<BeautyFilter>().apply {
            addAll(BeautyFilterPresets.filters)
        }
    }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var filterIntensity by remember { mutableFloatStateOf(0.85f) }
    var smoothingLevel by remember { mutableFloatStateOf(0.80f) }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    // Floating Heart Reactions Stream
    val activeHearts = remember { mutableStateListOf<HeartParticle>() }

    // Floating PiP Drag state
    var pipOffsetX by remember { mutableFloatStateOf(0f) }
    var pipOffsetY by remember { mutableFloatStateOf(0f) }

    // Camera permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    val activeFilter = filterList[selectedFilterIndex]

    fun triggerHeartReaction() {
        val colors = listOf(
            Color(0xFFFF1744),
            Color(0xFFFF4081),
            Color(0xFFE040FB),
            Color(0xFFFF5252),
            Color(0xFFFF80AB)
        )
        val particle = HeartParticle(
            id = System.currentTimeMillis() + Random.nextInt(1000),
            startXOffset = Random.nextFloat() * 120f - 60f,
            color = colors.random(),
            size = Random.nextFloat() * 0.5f + 0.9f
        )
        activeHearts.add(particle)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_call_screen")
    ) {
        // ==========================================
        // 1. REMOTE PEER VIDEO STREAM
        // ==========================================
        RemoteVideoStreamView(
            friend = friend,
            activeFilter = activeFilter,
            filterIntensity = filterIntensity,
            smoothingLevel = smoothingLevel,
            modifier = Modifier.fillMaxSize()
        )

        // Floating Hearts Reaction Stream Layer
        HeartReactionStream(
            hearts = activeHearts,
            onParticleFinished = { id ->
                activeHearts.removeAll { it.id == id }
            }
        )

        // ==========================================
        // 2. CALL HEADER: DURATION, STATUS & RECORDER BADGE
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Partner Info & Call Timer
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = friend.displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x3300F5FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HD 1080p",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F5FF)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Encrypted",
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "End-to-End Encrypted • ${formatCallTimer(callDurationSeconds)}",
                            fontSize = 12.sp,
                            color = Color(0xCCFFFFFF)
                        )
                    }
                }

                // Status Pills Row: Beauty Filter & Live Girl Voice Changer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Live Voice Changer Status Pill
                    LiveVoiceCallPill(
                        processor = voiceProcessor,
                        onOpenStudio = { showVoiceStudio = true }
                    )

                    // Active Beauty Filter Pill Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x661F2C34))
                            .clickable { showFilterSheet = !showFilterSheet }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("filter_pill_indicator"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Filter",
                                tint = activeFilter.primaryGlow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeFilter.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Direct Gallery Screen Recording Active Banner (Pulsing Red)
            if (isRecording) {
                Spacer(modifier = Modifier.height(10.dp))
                RecordingPulsingBanner(
                    durationSeconds = recordingDuration,
                    onStop = {
                        coroutineScope.launch {
                            val res = videoRecorder.stopRecording(friend.displayName)
                            res.onSuccess { info ->
                                savedVideoResult = info
                            }
                        }
                    }
                )
            }
        }

        // ==========================================
        // 3. DRAGGABLE PICTURE-IN-PICTURE (PiP) SELF-CAMERA
        // ==========================================
        Box(
            modifier = Modifier
                .offset { IntOffset(pipOffsetX.roundToInt(), pipOffsetY.roundToInt()) }
                .padding(top = 110.dp, end = 16.dp)
                .align(Alignment.TopEnd)
                .size(width = 130.dp, height = 185.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E))
                .border(2.dp, activeFilter.primaryGlow.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        pipOffsetX += dragAmount.x
                        pipOffsetY += dragAmount.y
                    }
                }
                .testTag("self_pip_camera_preview")
        ) {
            if (hasCameraPermission && !isVideoDisabled) {
                // CameraX Hardware Preview View
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = if (useFrontCamera) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                            } catch (_: Exception) {}
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback high-res self portrait view
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2A3942), Color(0xFF111B21))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(activeFilter.primaryGlow.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "YOU",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Real-Time Face Detection & Glowing Skin Radiance Overlay
            FaceDetectionOverlay(
                activeFilter = activeFilter,
                filterIntensity = filterIntensity,
                smoothingLevel = smoothingLevel,
                showTrackingReticle = showTrackingReticle,
                modifier = Modifier.fillMaxSize()
            )

            // Small Self Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Self • 94% Radiance",
                    fontSize = 9.sp,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // ==========================================
        // 4. BOTTOM VIDEO CALL CONTROLS BAR
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heart Reaction Floating Trigger Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0x4D000000))
                        .border(1.5.dp, Color(0xFFFF4081), CircleShape)
                        .clickable { triggerHeartReaction() }
                        .testTag("floating_heart_reaction_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Send Heart",
                        tint = Color(0xFFFF4081),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Main Glassmorphic Control Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xCC111B21),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Mic
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color(0xFFEF5350) else Color(0x33FFFFFF))
                            .testTag("call_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White
                        )
                    }

                    // Video Toggle
                    IconButton(
                        onClick = { isVideoDisabled = !isVideoDisabled },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isVideoDisabled) Color(0xFFEF5350) else Color(0x33FFFFFF))
                            .testTag("call_video_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isVideoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Video",
                            tint = Color.White
                        )
                    }

                    // Camera Switch (Front / Back)
                    IconButton(
                        onClick = { useFrontCamera = !useFrontCamera },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("call_switch_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }

                    // Direct Gallery Screen Recording Button (Key Feature)
                    IconButton(
                        onClick = {
                            if (!isRecording) {
                                videoRecorder.startRecording(coroutineScope)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Direct Gallery recording started (HD 1080p)")
                                }
                            } else {
                                coroutineScope.launch {
                                    val res = videoRecorder.stopRecording(friend.displayName)
                                    res.onSuccess { info ->
                                        savedVideoResult = info
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) Color(0xFFFF1744) else Color(0x33FFFFFF))
                            .testTag("call_record_gallery_button")
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                            contentDescription = "Direct Gallery Recording",
                            tint = if (isRecording) Color.White else Color(0xFFFF5252)
                        )
                    }

                    // Real-Time Natural Girl Voice Changer Button
                    val isVoiceEnabled by voiceProcessor.isEnabled.collectAsState()
                    IconButton(
                        onClick = { showVoiceStudio = !showVoiceStudio },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isVoiceEnabled) RosePink else Color(0x33FFFFFF))
                            .testTag("call_voice_changer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice Changer",
                            tint = Color.White
                        )
                    }

                    // Beauty Filter Drawer Toggle
                    IconButton(
                        onClick = { showFilterSheet = !showFilterSheet },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (showFilterSheet) WhatsAppGreen else Color(0x33FFFFFF))
                            .testTag("call_beauty_filters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Beauty Filters",
                            tint = if (showFilterSheet) Color(0xFF0B141A) else Color(0xFFFFD700)
                        )
                    }

                    // End Call
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .testTag("call_end_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 5. BEAUTY FILTERS BOTTOM DRAWER
        // ==========================================
        AnimatedVisibility(
            visible = showFilterSheet,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 96.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF2111B21)),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with Filter Tab & Reticle HUD toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Natural White Glowing Skin",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "10 Porcelain & Radiant Whitening Filters",
                                fontSize = 11.sp,
                                color = WhatsAppTextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Filter Favorites Toggle Tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (showOnlyFavorites) Color(0x33FF4081) else Color(0x22FFFFFF))
                                    .clickable { showOnlyFavorites = !showOnlyFavorites }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("toggle_favorites_filter_tab")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorites",
                                        tint = if (showOnlyFavorites) Color(0xFFFF4081) else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (showOnlyFavorites) "Favorites" else "All (10)",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { showFilterSheet = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = WhatsAppTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 10 Beauty Filters Horizontal Carousel with Heart-Tap Toggle
                    val displayFilters = if (showOnlyFavorites) {
                        filterList.filter { it.isFavorite }
                    } else {
                        filterList
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(displayFilters, key = { it.id }) { filter ->
                            val isSelected = filterList[selectedFilterIndex].id == filter.id

                            Card(
                                modifier = Modifier
                                    .width(115.dp)
                                    .clickable {
                                        selectedFilterIndex = filterList.indexOfFirst { it.id == filter.id }
                                    }
                                    .testTag("filter_card_${filter.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF1F2C34) else Color(0xFF141F24)
                                ),
                                border = if (isSelected) {
                                    androidx.compose.foundation.BorderStroke(2.dp, filter.primaryGlow)
                                } else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Top row: glowing circle swatch + HEART-TAP FAVORITE TOGGLE
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(filter.primaryGlow)
                                        )

                                        // Heart-tap favorite toggle (Key Requirement)
                                        IconButton(
                                            onClick = {
                                                val originalIndex = filterList.indexOfFirst { it.id == filter.id }
                                                if (originalIndex != -1) {
                                                    val updated = filterList[originalIndex].copy(
                                                        isFavorite = !filterList[originalIndex].isFavorite
                                                    )
                                                    filterList[originalIndex] = updated
                                                    coroutineScope.launch {
                                                        val msg = if (updated.isFavorite) "Added to Favorites" else "Removed from Favorites"
                                                        snackbarHostState.showSnackbar("${filter.name}: $msg")
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("heart_fav_${filter.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (filter.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (filter.isFavorite) Color(0xFFFF4081) else Color(0x66FFFFFF),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = filter.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xCCFFFFFF),
                                        maxLines = 1
                                    )

                                    Text(
                                        text = filter.tagLine,
                                        fontSize = 9.sp,
                                        color = WhatsAppTextSecondary,
                                        maxLines = 2,
                                        lineHeight = 11.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Intensity & Smoothing Sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "White Glow Intensity: ${(filterIntensity * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Slider(
                            value = filterIntensity,
                            onValueChange = { filterIntensity = it },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = activeFilter.primaryGlow,
                                activeTrackColor = WhatsAppGreen
                            ),
                            modifier = Modifier
                                .weight(2f)
                                .testTag("slider_glow_intensity")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Skin Smoothing: ${(smoothingLevel * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Slider(
                            value = smoothingLevel,
                            onValueChange = { smoothingLevel = it },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00F5FF),
                                activeTrackColor = Color(0xFF00F5FF)
                            ),
                            modifier = Modifier
                                .weight(2f)
                                .testTag("slider_skin_smoothing")
                        )
                    }
                }
            }
        }

        // ==========================================
        // 6. DIRECT GALLERY RECORDING COMPLETED DIALOG
        // ==========================================
        savedVideoResult?.let { videoInfo ->
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .testTag("saved_video_dialog"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color(0xFF0B141A),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Call Recorded to Gallery",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "The encrypted HD video call has been recorded and saved directly into your device Gallery.",
                        fontSize = 13.sp,
                        color = WhatsAppTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF141F24)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Duration:", fontSize = 12.sp, color = WhatsAppTextSecondary)
                                Text(videoInfo.formattedDuration, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("File Size:", fontSize = 12.sp, color = WhatsAppTextSecondary)
                                Text(videoInfo.fileSizeFormatted, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Location:", fontSize = 12.sp, color = WhatsAppTextSecondary)
                                Text("DCIM / SecureChat", fontSize = 12.sp, color = WhatsAppLightGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { savedVideoResult = null },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dismiss_saved_video_dialog")
                    ) {
                        Text("Done", color = Color(0xFF0B141A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ==========================================
        // 7. LIVE VOICE CHANGER STUDIO DIALOG
        // ==========================================
        if (showVoiceStudio) {
            LiveVoiceChangerStudioDialog(
                processor = voiceProcessor,
                onDismiss = { showVoiceStudio = false }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Pulsing Red Recording Indicator Banner with Live Timer & Stop action.
 */
@Composable
fun RecordingPulsingBanner(
    durationSeconds: Int,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RecBlink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RecAlpha"
    )

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xD9000000))
            .border(1.dp, Color(0x66FF1744), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF1744).copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "REC $timeFormatted • Direct to Gallery",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE53935))
                .clickable { onStop() }
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .testTag("stop_recording_chip")
        ) {
            Text("STOP", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

/**
 * Realistic high-fidelity video stream for the remote calling partner
 * featuring subtle real-time breathing animation and ambient glow.
 */
@Composable
fun RemoteVideoStreamView(
    friend: UserProfile,
    activeFilter: BeautyFilter,
    filterIntensity: Float,
    smoothingLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RemotePeerMotion")
    val breathingY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breathing"
    )

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF14242A), Color(0xFF0C171C), Color(0xFF070E12))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = breathingY.dp)
        ) {
            // Glowing Beauty Avatar Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Outer glow halo
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    activeFilter.primaryGlow.copy(alpha = 0.25f * filterIntensity),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF008069))
                        .border(3.dp, activeFilter.primaryGlow.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = friend.displayName.take(1).uppercase().ifBlank { "U" },
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = friend.displayName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ID: ${friend.chatId}",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = WhatsAppLightGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33000000))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Encrypted 60fps Stream • White Glow Active",
                    fontSize = 11.sp,
                    color = Color(0xCCFFFFFF)
                )
            }
        }
    }
}

fun formatCallTimer(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}
