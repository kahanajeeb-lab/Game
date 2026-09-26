package com.example.ui.voice

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HeadphonesBattery
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

@Composable
fun LiveVoiceCallPill(
    processor: LiveVoiceCallProcessor,
    onOpenStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled by processor.isEnabled.collectAsState()
    val currentPreset by processor.currentPreset.collectAsState()
    val audioLevels by processor.liveAudioLevels.collectAsState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isEnabled) RosePink.copy(alpha = 0.90f) else Color(0x99202A30))
            .border(
                width = 1.dp,
                color = if (isEnabled) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onOpenStudio() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("live_voice_changer_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isEnabled) currentPreset.emoji else "🎙️",
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isEnabled) "Voice: ${currentPreset.name}" else "Voice: Normal",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))

            // Mini Equalizer Bars
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                audioLevels.takeLast(4).forEach { lvl ->
                    val barHeight = (lvl * 14).coerceIn(3f, 14f).dp
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(barHeight)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun LiveVoiceChangerStudioDialog(
    processor: LiveVoiceCallProcessor,
    onDismiss: () -> Unit
) {
    val isEnabled by processor.isEnabled.collectAsState()
    val currentPreset by processor.currentPreset.collectAsState()
    val isMonitoring by processor.isMonitoring.collectAsState()
    val audioLevels by processor.liveAudioLevels.collectAsState()

    var customPitch by remember { mutableFloatStateOf(currentPreset.pitchSemitones) }
    var customFormant by remember { mutableFloatStateOf(currentPreset.formantRatio) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("live_voice_changer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Live Girl Voice Changer",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "🌸", fontSize = 16.sp)
                        }
                        Text(
                            text = "Real-Time DSP Vocal Filter for Live Calls",
                            fontSize = 12.sp,
                            color = WhatsAppTextSecondary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = WhatsAppTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Master Toggle Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1B242A))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Enable Real-Time Voice Changer",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WhatsAppTextPrimary
                        )
                        Text(
                            text = if (isEnabled) "Active: Callers hear natural female voice" else "Inactive: Callers hear raw mic",
                            fontSize = 11.sp,
                            color = if (isEnabled) RosePink else WhatsAppTextSecondary
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { processor.toggleVoiceChanger(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RosePink,
                            uncheckedThumbColor = WhatsAppTextSecondary,
                            uncheckedTrackColor = Color(0xFF26353F)
                        ),
                        modifier = Modifier.testTag("live_voice_master_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets Title & Scrollable Selector
                Text(
                    text = "FEMININE VOCAL PROFILES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoicePresets.allPresets.forEach { preset ->
                        val isSelected = preset.id == currentPreset.id
                        val bgColor = if (isSelected) {
                            if (preset.isNaturalGirl) RosePink else WhatsAppGreen
                        } else {
                            Color(0xFF26353F)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(bgColor)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    processor.setPreset(preset)
                                    customPitch = preset.pitchSemitones
                                    customFormant = preset.formantRatio
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("live_preset_chip_${preset.id}")
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = preset.emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = preset.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                                if (preset.isNaturalGirl) {
                                    Text(
                                        text = "+${preset.pitchSemitones}st • ${preset.formantRatio}x",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-Time Audio Level & Equalizer Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF161F24))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        audioLevels.forEach { lvl ->
                            val height = (lvl * 28).coerceIn(4f, 28f).dp
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(height)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(RosePink, SoftPink)
                                        )
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pitch & Formant Fine-Tuning Sliders
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pitch Shift (F0)",
                            fontSize = 12.sp,
                            color = WhatsAppTextPrimary
                        )
                        Text(
                            text = "+${String.format("%.1f", customPitch)} semitones",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RosePink
                        )
                    }
                    Slider(
                        value = customPitch,
                        onValueChange = {
                            customPitch = it
                            val custom = currentPreset.copy(pitchSemitones = it)
                            processor.setPreset(custom)
                        },
                        valueRange = 0.0f..8.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = RosePink,
                            activeTrackColor = RosePink,
                            inactiveTrackColor = Color(0xFF26353F)
                        ),
                        modifier = Modifier.height(30.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Female Vocal Tract Formant",
                            fontSize = 12.sp,
                            color = WhatsAppTextPrimary
                        )
                        Text(
                            text = "${String.format("%.2f", customFormant)}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RosePink
                        )
                    }
                    Slider(
                        value = customFormant,
                        onValueChange = {
                            customFormant = it
                            val custom = currentPreset.copy(formantRatio = it)
                            processor.setPreset(custom)
                        },
                        valueRange = 1.0f..1.35f,
                        colors = SliderDefaults.colors(
                            thumbColor = RosePink,
                            activeTrackColor = RosePink,
                            inactiveTrackColor = Color(0xFF26353F)
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Headset Real-Time Audio Monitor Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF202A30))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = if (isMonitoring) SoftPink else WhatsAppTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Hear My Own Voice (Headset)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = WhatsAppTextPrimary
                            )
                            Text(
                                text = "Real-time ear monitor feedback",
                                fontSize = 10.sp,
                                color = WhatsAppTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isMonitoring,
                        onCheckedChange = { processor.toggleMonitoring(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RosePink,
                            uncheckedThumbColor = WhatsAppTextSecondary,
                            uncheckedTrackColor = Color(0xFF26353F)
                        ),
                        modifier = Modifier.testTag("hear_myself_monitor_switch")
                    )
                }
            }
        }
    }
}
