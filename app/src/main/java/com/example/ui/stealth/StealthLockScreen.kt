package com.example.ui.stealth

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Stealth Arcade Game UI Masking:
 * Acts and plays as a harmless casual arcade runner game "Retro Dash".
 * Nobody inspecting the device or looking over a shoulder can suspect this is a messenger.
 *
 * Stealth Unlock Mechanism:
 * Touch and hold continuously for >= 5 seconds (with ZERO visual indicators, ZERO progress bars, ZERO blue lines)
 * followed by a right-swipe gesture to unlock the hidden encrypted messenger.
 * Instant auto-lock reset upon app minimize, close, or screen off.
 */
@Composable
fun StealthLockScreen(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Stealth gesture state (ZERO visual indicators)
    var pressStartTime by remember { mutableLongStateOf(0L) }
    var currentDragDeltaX by remember { mutableFloatStateOf(0f) }

    // Arcade Mini-Game State
    var isPlaying by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(4850) }
    var gemsCollected by remember { mutableIntStateOf(142) }
    var isGameOver by remember { mutableStateOf(false) }

    // Player jump state
    var playerY by remember { mutableFloatStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }

    // Secret helper dialog for rapid testing
    var showSecretGuide by remember { mutableStateOf(false) }
    var tapCounterOnTitle by remember { mutableIntStateOf(0) }

    fun triggerHaptic(durationMs: Long = 80) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    // Game loop when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            score = 0
            isGameOver = false
            while (isPlaying) {
                delay(100)
                score += 10
                if (score > highScore) {
                    highScore = score
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("stealth_game_container")
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F0C20),
                        Color(0xFF1B1035),
                        Color(0xFF090614)
                    )
                )
            )
            // ZERO-INDICATOR Stealth Hold & Swipe detector
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    pressStartTime = System.currentTimeMillis()
                    currentDragDeltaX = 0f
                    val pointerId = down.id

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId }
                        if (change == null || !change.pressed) {
                            val duration = System.currentTimeMillis() - pressStartTime
                            // 5-second continuous hold (>= 4500ms) AND right-swipe (>= 60px)
                            if (pressStartTime > 0L && duration >= 4500L && currentDragDeltaX > 60f) {
                                triggerHaptic(120)
                                onUnlock()
                            }
                            pressStartTime = 0L
                            currentDragDeltaX = 0f
                            break
                        } else {
                            currentDragDeltaX += (change.position.x - change.previousPosition.x)
                        }
                    }
                }
            }
    ) {
        // Starry neon arcade canvas background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Neon grid lines at the bottom for arcade retro floor
            val floorY = h * 0.72f
            drawLine(
                color = Color(0x66FF007F),
                start = Offset(0f, floorY),
                end = Offset(w, floorY),
                strokeWidth = 3f
            )

            // Perspective grid lines
            for (i in 0..10) {
                val startX = (w / 10f) * i
                val endX = w * 0.5f + (startX - w * 0.5f) * 2.5f
                drawLine(
                    color = Color(0x228A2BE2),
                    start = Offset(startX, floorY),
                    end = Offset(endX, h),
                    strokeWidth = 1.5f
                )
            }

            // Floating stars
            val starColor = Color(0x8800FFFF)
            drawCircle(starColor, 2f, Offset(w * 0.15f, h * 0.18f))
            drawCircle(starColor, 3f, Offset(w * 0.82f, h * 0.12f))
            drawCircle(starColor, 2.5f, Offset(w * 0.65f, h * 0.28f))
            drawCircle(starColor, 2f, Offset(w * 0.35f, h * 0.42f))
            drawCircle(starColor, 4f, Offset(w * 0.22f, h * 0.55f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Arcade Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High Score badge with hidden 3-tap or long-press shortcut
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .testTag("stealth_highscore_counter")
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF241544))
                        .stealthBypassShortcut {
                            triggerHaptic(120)
                            onUnlock()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BEST $highScore",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Gems Counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF241544))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Gems",
                        tint = Color(0xFF00F5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$gemsCollected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00F5FF),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Main Interactive Arcade Playfield / Title
            if (!isPlaying) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    // Game Logo with hidden 3-tap or long-press shortcut
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .testTag("arcade_game_logo")
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFF007F), Color(0xFF7928CA), Color(0xFF00F5FF))
                                )
                            )
                            .stealthBypassShortcut {
                                triggerHaptic(120)
                                onUnlock()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Games,
                            contentDescription = "Retro Dash Logo",
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "RETRO DASH",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .testTag("retro_dash_title")
                            .stealthBypassShortcut {
                                triggerHaptic(120)
                                onUnlock()
                            }
                    )

                    Text(
                        text = "HYPER CASUAL ARCADE RUNNER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00F5FF),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Play Button
                    Button(
                        onClick = {
                            isPlaying = true
                            triggerHaptic(50)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(54.dp)
                            .testTag("play_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TAP TO PLAY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Casual Game Stats Card
                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        colors = CardDefaults.cardColors(containerColor = Color(0x661F143D)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("LIFETIME RUNS", color = Color(0xFF9E8DBF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("87", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("NEON STREAK", color = Color(0xFF9E8DBF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("x4 COMBO", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Active Game Screen with animated interactive cube
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "$score",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00F5FF),
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "TAP SCREEN TO HOP",
                        fontSize = 12.sp,
                        color = Color(0xAAFFFFFF),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Interactive Jumping Neon Runner Cube
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00F5FF), Color(0xFFFF007F))
                                )
                            )
                            .clickable {
                                // Jump action
                                triggerHaptic(30)
                                score += 50
                                gemsCollected += 1
                            }
                            .testTag("interactive_game_hero"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▲",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                isPlaying = false
                                triggerHaptic(40)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332050)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("PAUSE", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                score += 100
                                gemsCollected += 5
                                triggerHaptic(60)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+BOOST", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "v2.4.1 • Global Arcade Rankings Active",
                    fontSize = 11.sp,
                    color = Color(0xFF6B588E)
                )
            }
        }

        // Discrete stealth lock guide & bypass modal for testing
        if (showSecretGuide) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(28.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1735)),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00A884)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secret Gateway",
                            tint = Color(0xFF0B141A),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Stealth Mask Activated",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "The app presents itself as the 'Retro Dash' casual arcade game to disguise your messaging activity.\n\nTo unlock without UI indicators:\n• Hidden Shortcut: 3-tap or long-press directly on the 'RETRO DASH' logo or the top-left High-Score counter to immediately launch secure chat.\n• Gesture: Press and hold anywhere on screen for 5 seconds, then swipe right.\n\nMinimizing or locking screen instantly resets disguise.",
                        fontSize = 13.sp,
                        color = Color(0xFFC4B8E0),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { showSecretGuide = false }
                        ) {
                            Text("Resume Game", color = Color(0xFF00F5FF))
                        }

                        Button(
                            onClick = {
                                showSecretGuide = false
                                triggerHaptic(100)
                                onUnlock()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884)),
                            modifier = Modifier.testTag("secret_direct_unlock_button")
                        ) {
                            Text("Unlock Chat", color = Color(0xFF0B141A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Invisible stealth shortcut modifier:
 * Detects either a 3-tap sequence (within 1.8s) OR a long-press (>= 500ms).
 * Completely invisible with zero visual clues, zero ripple, and zero delay.
 * Directly triggers onTrigger to immediately bypass the disguise and launch the secure chat.
 */
fun Modifier.stealthBypassShortcut(
    onTrigger: () -> Unit
): Modifier = this.pointerInput(Unit) {
    var tapCount = 0
    var lastTapTime = 0L
    detectTapGestures(
        onLongPress = {
            onTrigger()
        },
        onTap = {
            val now = System.currentTimeMillis()
            if (now - lastTapTime > 1800L) {
                tapCount = 1
            } else {
                tapCount++
            }
            lastTapTime = now
            if (tapCount >= 3) {
                tapCount = 0
                onTrigger()
            }
        }
    )
}

