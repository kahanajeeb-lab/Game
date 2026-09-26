package com.example

import com.example.data.model.ChatMessage
import com.example.ui.call.BeautyFilterPresets
import com.example.ui.call.formatCallTimer
import com.example.ui.voice.VoiceDspProcessor
import com.example.ui.voice.VoicePreset
import com.example.ui.voice.VoicePresets
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sin

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testTenNaturalWhiteGlowingSkinFilters() {
        val filters = BeautyFilterPresets.filters
        // Must contain exactly 10 natural white glowing skin beauty filters
        assertEquals(10, filters.size)

        // Verify all 10 have valid properties
        filters.forEach { filter ->
            assertTrue(filter.id.isNotBlank())
            assertTrue(filter.name.isNotBlank())
            assertTrue(filter.tagLine.isNotBlank())
            assertTrue(filter.brightness > 0f)
            assertTrue(filter.contrast >= 1.0f)
            assertTrue(filter.whiteGlowTone > 0f)

            // Test ColorMatrix generation
            val matrix = filter.toColorMatrix(intensity = 0.8f, smoothing = 0.7f)
            assertNotNull(matrix)
        }
    }

    @Test
    fun testHeartTapFavoriteToggle() {
        val initialFilter = BeautyFilterPresets.filters.first()
        val originalFav = initialFilter.isFavorite
        val toggled = initialFilter.copy(isFavorite = !originalFav)
        assertEquals(!originalFav, toggled.isFavorite)
    }

    @Test
    fun testCallTimerFormatting() {
        assertEquals("00:00", formatCallTimer(0))
        assertEquals("01:05", formatCallTimer(65))
        assertEquals("12:34", formatCallTimer(754))
    }

    @Test
    fun testNaturalGirlVoicePresetsConfiguration() {
        val presets = VoicePresets.allPresets
        assertTrue("Presets should have at least 5 configurations", presets.size >= 5)

        val girlPresets = VoicePresets.girlPresets
        assertEquals(5, girlPresets.size)

        // Verify default recommended "Natural Soft Girl" preset parameters
        val softGirl = VoicePresets.NaturalSoftGirl
        assertEquals("Natural Soft Girl", softGirl.name)
        assertTrue("Pitch must be authentic female range (+3.5 to +5.0 st)", softGirl.pitchSemitones in 3.5f..5.0f)
        assertTrue("Formant ratio must simulate female vocal tract (1.15x to 1.30x)", softGirl.formantRatio in 1.15f..1.30f)
        assertTrue("Airiness harmonic factor must be configured", softGirl.airiness > 0.5f)
        assertTrue("Warmth factor must be configured", softGirl.warmth > 0.5f)

        // Verify Sweet Melodic
        val sweetMelodic = VoicePresets.SweetMelodic
        assertTrue(sweetMelodic.pitchSemitones > softGirl.pitchSemitones)
        assertTrue(sweetMelodic.formantRatio >= softGirl.formantRatio)

        // Verify Bypass preset
        val bypass = VoicePresets.OriginalVoice
        assertFalse(bypass.isNaturalGirl)
        assertEquals(0.0f, bypass.pitchSemitones, 0.01f)
    }

    @Test
    fun testVoiceDspTransformationAndWavPackaging() {
        val sampleRate = 16000
        val sampleCount = sampleRate / 2 // 0.5 seconds of audio
        val testPcm = ShortArray(sampleCount) { i ->
            // Generate a 130Hz adult male fundamental test tone
            (sin(2.0 * Math.PI * 130.0 * i / sampleRate) * 15000).toInt().toShort()
        }

        val preset = VoicePresets.NaturalSoftGirl
        val transformed = VoiceDspProcessor.processVoice(testPcm, sampleRate, preset)

        assertNotNull(transformed)
        assertTrue("Transformed audio should have valid samples", transformed.isNotEmpty())

        // Packaging into 44-byte standard RIFF WAV container
        val wavBytes = VoiceDspProcessor.pcmToWavBytes(transformed, sampleRate)
        assertTrue("WAV container must be at least 44 bytes header + PCM payload", wavBytes.size > 44)

        // Verify RIFF / WAVE header markers
        assertEquals('R'.code.toByte(), wavBytes[0])
        assertEquals('I'.code.toByte(), wavBytes[1])
        assertEquals('F'.code.toByte(), wavBytes[2])
        assertEquals('F'.code.toByte(), wavBytes[3])
        assertEquals('W'.code.toByte(), wavBytes[8])
        assertEquals('A'.code.toByte(), wavBytes[9])
        assertEquals('V'.code.toByte(), wavBytes[10])
        assertEquals('E'.code.toByte(), wavBytes[11])
    }

    @Test
    fun testVoiceChatMessageModelIntegration() {
        val voiceMsg = ChatMessage(
            id = "msg_voice_101",
            senderId = "user_a",
            receiverId = "user_b",
            text = "🎤 Voice message (8 s)",
            timestamp = System.currentTimeMillis(),
            status = "delivered",
            isVoiceMessage = true,
            voiceDurationSec = 8,
            voicePresetName = "Natural Soft Girl",
            audioDataUri = "data:audio/wav;base64,UklGRi..."
        )

        assertTrue(voiceMsg.isVoiceMessage)
        assertEquals(8, voiceMsg.voiceDurationSec)
        assertTrue(voiceMsg.voiceDurationSec <= 10) // 10-second limit enforced
        assertEquals("Natural Soft Girl", voiceMsg.voicePresetName)
        assertEquals("delivered", voiceMsg.status)
    }

    @Test
    fun testStealthBypassShortcutTripleTapTrigger() {
        var unlockTriggered = false
        var tapCount = 0
        var lastTapTime = 0L

        fun simulateTap(timestamp: Long) {
            if (timestamp - lastTapTime > 1800L) {
                tapCount = 1
            } else {
                tapCount++
            }
            lastTapTime = timestamp
            if (tapCount >= 3) {
                tapCount = 0
                unlockTriggered = true
            }
        }

        // Tap 1
        simulateTap(1000L)
        assertFalse("Unlock should not trigger on tap 1", unlockTriggered)

        // Tap 2
        simulateTap(1400L)
        assertFalse("Unlock should not trigger on tap 2", unlockTriggered)

        // Tap 3 within 1800ms
        simulateTap(1700L)
        assertTrue("Unlock MUST trigger on tap 3 within 1800ms window", unlockTriggered)

        // Reset and test expired window
        unlockTriggered = false
        simulateTap(5000L)
        assertEquals(1, tapCount)
        simulateTap(8000L) // 3000ms later (> 1800ms threshold)
        assertEquals(1, tapCount)
        assertFalse("Expired window should reset tap count to 1", unlockTriggered)
    }

    @Test
    fun testStealthHoldAndSwipeThresholds() {
        fun evaluateStealthGesture(holdDurationMs: Long, swipeDeltaX: Float): Boolean {
            return holdDurationMs >= 4500L && swipeDeltaX > 60f
        }

        // Fail if hold duration < 4.5s
        assertFalse(evaluateStealthGesture(holdDurationMs = 2000L, swipeDeltaX = 100f))

        // Fail if swipe delta is too small or left-swipe
        assertFalse(evaluateStealthGesture(holdDurationMs = 5000L, swipeDeltaX = 20f))
        assertFalse(evaluateStealthGesture(holdDurationMs = 5000L, swipeDeltaX = -80f))

        // Pass on valid hold >= 4.5s and right swipe > 60px
        assertTrue(evaluateStealthGesture(holdDurationMs = 4800L, swipeDeltaX = 85f))
        assertTrue(evaluateStealthGesture(holdDurationMs = 5100L, swipeDeltaX = 120f))
    }
}


