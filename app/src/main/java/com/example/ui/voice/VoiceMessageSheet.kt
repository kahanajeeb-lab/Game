package com.example.ui.voice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppDarkBg
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

val RosePink = Color(0xFFFF5E8A)
val SoftPink = Color(0xFFFF85A2)
val LightPinkBg = Color(0x33FF5E8A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceMessageBottomSheet(
    recipientName: String,
    onDismiss: () -> Unit,
    onSendVoiceMessage: (durationSec: Int, presetName: String, audioDataUri: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recorder = remember { VoiceMessageRecorder(context) }

    val isRecording by recorder.isRecording.collectAsState()
    val recordedSeconds by recorder.recordedSeconds.collectAsState()
    val waveforms by recorder.waveforms.collectAsState()
    val selectedPreset by recorder.selectedPreset.collectAsState()
    val previewPlaying by recorder.previewPlaying.collectAsState()
    val hasRecording by recorder.hasRecording.collectAsState()

    var playingOriginal by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            recorder.discardRecording()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            recorder.discardRecording()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = WhatsAppDarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        },
        modifier = modifier.testTag("voice_message_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Title & Recipient
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "10s Girl Voice Message",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightPinkBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Natural Ultra-HD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RosePink
                            )
                        }
                    }
                    Text(
                        text = "To $recipientName • Max 10-second limit",
                        fontSize = 12.sp,
                        color = WhatsAppTextSecondary
                    )
                }

                IconButton(
                    onClick = {
                        recorder.discardRecording()
                        onDismiss()
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = WhatsAppTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preset Selection Chips (Horizontal Carousel)
            Text(
                text = "SELECT NATURAL GIRL VOICE PRESET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WhatsAppTextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoicePresets.allPresets.forEach { preset ->
                    val isSelected = preset.id == selectedPreset.id
                    val chipBg = if (isSelected) {
                        if (preset.isNaturalGirl) RosePink else WhatsAppGreen
                    } else {
                        Color(0xFF26353F)
                    }
                    val textColor = if (isSelected) Color.White else WhatsAppTextPrimary

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(chipBg)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                recorder.selectPreset(preset)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("voice_preset_chip_${preset.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = preset.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = preset.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                                if (preset.pitchSemitones > 0) {
                                    Text(
                                        text = "+${preset.pitchSemitones} st • ${preset.formantRatio}x",
                                        fontSize = 10.sp,
                                        color = textColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center Display: 10-Second Timer Ring & Dancing Waveforms
            Box(
                modifier = Modifier
                    .size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Progress circle (10-second limit)
                val progress = (recordedSeconds / 10f).coerceIn(0f, 1f)
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(150.dp),
                    color = if (isRecording) RosePink else WhatsAppGreen,
                    trackColor = Color(0xFF2A3942),
                    strokeWidth = 6.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val formattedSec = String.format("%.1f", recordedSeconds)
                    Text(
                        text = "${formattedSec}s",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isRecording) RosePink else WhatsAppTextPrimary
                    )
                    Text(
                        text = if (isRecording) "RECORDING (MAX 10s)" else if (hasRecording) "RECORDED" else "TAP MIC TO SPEAK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isRecording) RosePink else WhatsAppTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Waveform Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B242A))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (waveforms.isEmpty() && !hasRecording) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = WhatsAppTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Acoustic Girl Voice DSP Ready",
                            fontSize = 12.sp,
                            color = WhatsAppTextSecondary
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayWaves = if (waveforms.isNotEmpty()) {
                            waveforms
                        } else {
                            listOf(0.2f, 0.5f, 0.8f, 0.6f, 0.9f, 0.4f, 0.7f, 0.3f, 0.6f, 0.4f, 0.2f)
                        }

                        displayWaves.forEach { amp ->
                            val barHeight = (amp * 36).coerceIn(4f, 36f).dp
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                if (selectedPreset.isNaturalGirl) RosePink else WhatsAppGreen,
                                                if (selectedPreset.isNaturalGirl) SoftPink else WhatsAppLightGreen
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Controls
            if (!hasRecording) {
                // Recording Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecording) {
                        IconButton(
                            onClick = { recorder.stopRecording() },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(RosePink)
                                .testTag("voice_stop_recording_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    } else {
                        IconButton(
                            onClick = {
                                recorder.startRecording(onAutoLimitReached = {
                                    // 10s auto stop
                                })
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(RosePink)
                                .testTag("voice_start_recording_button")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Record", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            } else {
                // Post-Recording Preview & Send Mode
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Preview Listen Options (Original vs Natural Girl Filtered)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {
                                playingOriginal = true
                                recorder.playPreview(playOriginal = true)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (previewPlaying && playingOriginal) WhatsAppGreen else WhatsAppTextPrimary
                            ),
                            modifier = Modifier.testTag("preview_original_voice_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Original Voice", fontSize = 12.sp)
                        }

                        ElevatedButton(
                            onClick = {
                                playingOriginal = false
                                recorder.playPreview(playOriginal = false)
                            },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = if (previewPlaying && !playingOriginal) RosePink else LightPinkBg,
                                contentColor = if (previewPlaying && !playingOriginal) Color.White else RosePink
                            ),
                            modifier = Modifier.testTag("preview_girl_voice_button")
                        ) {
                            Icon(
                                if (previewPlaying && !playingOriginal) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hear ${selectedPreset.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Delete & Send row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { recorder.discardRecording() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF26353F))
                                .testTag("discard_voice_recording_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF6B6B))
                        }

                        ElevatedButton(
                            onClick = {
                                val (wavBase64, duration) = recorder.exportProcessedWavBase64()
                                onSendVoiceMessage(duration, selectedPreset.name, wavBase64)
                                recorder.discardRecording()
                                onDismiss()
                            },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = WhatsAppGreen,
                                contentColor = Color(0xFF0B141A)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                                .height(48.dp)
                                .testTag("send_voice_message_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Send 10s Girl Voice",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
