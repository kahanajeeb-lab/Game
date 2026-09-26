package com.example.ui.voice

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Base64
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.chat.DeliveryStatusTicks
import com.example.ui.chat.formatMessageTime
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun VoiceMessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var playbackJob by remember { mutableStateOf<Job?>(null) }
    var audioTrack by remember { mutableStateOf<AudioTrack?>(null) }

    DisposableEffect(message.id) {
        onDispose {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Exception) {}
            playbackJob?.cancel()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wavePulse"
    )

    fun startPlayback() {
        if (isPlaying) {
            isPlaying = false
            playbackProgress = 0f
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Exception) {}
            playbackJob?.cancel()
            return
        }

        playbackJob = coroutineScope.launch(Dispatchers.IO) {
            try {
                val wavBytes = if (message.audioDataUri.isNotBlank()) {
                    Base64.decode(message.audioDataUri, Base64.DEFAULT)
                } else null

                isPlaying = true
                val durationMs = if (message.voiceDurationSec > 0) message.voiceDurationSec * 1000L else 3000L

                if (wavBytes != null && wavBytes.size > 44) {
                    val pcmBytes = wavBytes.copyOfRange(44, wavBytes.size)
                    val sampleRate = 16000
                    val minBuf = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    ).coerceAtLeast(pcmBytes.size)

                    val track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBuf)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()

                    audioTrack = track
                    track.play()
                    track.write(pcmBytes, 0, pcmBytes.size)
                }

                val startTime = System.currentTimeMillis()
                while (isActive && isPlaying) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val prog = (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    playbackProgress = prog

                    if (elapsed >= durationMs) {
                        break
                    }
                    delay(50)
                }
            } catch (e: Exception) {
                Log.e("VoiceBubble", "Playback error", e)
            } finally {
                isPlaying = false
                playbackProgress = 0f
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {}
            }
        }
    }

    val bubbleColor = if (isMe) Color(0xFF005C4B) else WhatsAppDarkSurface
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = bubbleColor),
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .width(280.dp)
            .testTag("voice_message_card_${message.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Natural Girl Voice Preset Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RosePink.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = "🌸", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = message.voicePresetName.ifBlank { "Natural Girl Voice" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftPink
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player Row: Play/Pause button + Avatar + Waveform + Duration
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Play / Pause Circle Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) RosePink else WhatsAppGreen)
                        .clickable { startPlayback() }
                        .testTag("voice_play_button_${message.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF0B141A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Waveform Bars & Progress
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val barCount = 18
                        val barAmplitudes = remember(message.id) {
                            listOf(0.3f, 0.6f, 0.9f, 0.4f, 0.8f, 1.0f, 0.5f, 0.7f, 0.3f, 0.8f, 0.6f, 0.9f, 0.4f, 0.7f, 0.5f, 0.8f, 0.4f, 0.2f)
                        }

                        for (i in 0 until barCount) {
                            val activeRatio = (i.toFloat() / barCount.toFloat())
                            val isPlayed = playbackProgress >= activeRatio
                            val baseAmp = barAmplitudes.getOrElse(i) { 0.5f }
                            val dynamicHeight = if (isPlaying && isPlayed) {
                                (baseAmp * 24 * pulseScale).coerceIn(4f, 26f).dp
                            } else {
                                (baseAmp * 20).coerceIn(4f, 20f).dp
                            }

                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(dynamicHeight)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(
                                        if (isPlayed) SoftPink else Color.White.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Duration and Timestamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val durationText = if (isPlaying) {
                            val currentSec = ((playbackProgress * (message.voiceDurationSec.coerceAtLeast(1)))).toInt()
                            String.format("0:%02d", currentSec)
                        } else {
                            String.format("0:%02d", message.voiceDurationSec.coerceAtLeast(1))
                        }
                        Text(
                            text = durationText,
                            fontSize = 11.sp,
                            color = WhatsAppTextSecondary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatMessageTime(message.timestamp),
                                fontSize = 11.sp,
                                color = WhatsAppTextSecondary
                            )
                            if (isMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                DeliveryStatusTicks(status = message.status)
                            }
                        }
                    }
                }
            }
        }
    }
}
