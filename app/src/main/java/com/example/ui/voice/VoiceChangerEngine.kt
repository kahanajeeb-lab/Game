package com.example.ui.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Ultra-realistic Natural Girl Voice Preset definitions.
 * Specifically engineered with biological female vocal tract acoustic properties:
 * - Pitch shift: +3.2 to +5.8 semitones (lifts typical 110-140Hz male fundamental to 200-240Hz natural feminine pitch)
 * - Formant shift: 1.14x to 1.30x (simulates ~15-20% shorter female vocal tract resonances)
 * - Acoustic harmonic airiness & warmth (suppresses lower chest resonance, enhances 3kHz presence & 7kHz breathiness)
 */
data class VoicePreset(
    val id: String,
    val name: String,
    val tagLine: String,
    val pitchSemitones: Float,      // Semitone offset (e.g. +4.2 st)
    val formantRatio: Float,        // Vocal tract shortening ratio (1.15x - 1.30x)
    val airiness: Float,            // Breathiness / soft harmonic lift (0.0 - 1.0)
    val warmth: Float,              // Mid-range presence factor (0.0 - 1.0)
    val isNaturalGirl: Boolean = true,
    val emoji: String = "🌸"
)

object VoicePresets {
    val NaturalSoftGirl = VoicePreset(
        id = "natural_soft_girl",
        name = "Natural Soft Girl",
        tagLine = "Authentic, lifelike gentle feminine speech tone",
        pitchSemitones = 4.2f,
        formantRatio = 1.22f,
        airiness = 0.75f,
        warmth = 0.85f,
        emoji = "🌸"
    )

    val SweetMelodic = VoicePreset(
        id = "sweet_melodic",
        name = "Sweet Melodic",
        tagLine = "Bright, cheerful & radiant female vocal timbre",
        pitchSemitones = 4.8f,
        formantRatio = 1.26f,
        airiness = 0.80f,
        warmth = 0.80f,
        emoji = "✨"
    )

    val WarmYouthfulTeen = VoicePreset(
        id = "warm_teen",
        name = "Warm Youthful",
        tagLine = "Natural casual female speaking voice with warmth",
        pitchSemitones = 3.6f,
        formantRatio = 1.16f,
        airiness = 0.65f,
        warmth = 0.90f,
        emoji = "🎀"
    )

    val KawaiiAnime = VoicePreset(
        id = "kawaii_anime",
        name = "Kawaii Anime",
        tagLine = "High-energy, cute & playful anime-style voice",
        pitchSemitones = 5.8f,
        formantRatio = 1.30f,
        airiness = 0.85f,
        warmth = 0.70f,
        emoji = "💖"
    )

    val ElegantSoprano = VoicePreset(
        id = "elegant_soprano",
        name = "Elegant Soprano",
        tagLine = "Smooth, refined & mature feminine elegance",
        pitchSemitones = 3.2f,
        formantRatio = 1.14f,
        airiness = 0.70f,
        warmth = 0.88f,
        emoji = "💎"
    )

    val OriginalVoice = VoicePreset(
        id = "original_bypass",
        name = "Original Voice",
        tagLine = "Unmodified, natural microphone sound bypass",
        pitchSemitones = 0.0f,
        formantRatio = 1.00f,
        airiness = 0.0f,
        warmth = 1.00f,
        isNaturalGirl = false,
        emoji = "🎙️"
    )

    val allPresets: List<VoicePreset> = listOf(
        NaturalSoftGirl,
        SweetMelodic,
        WarmYouthfulTeen,
        KawaiiAnime,
        ElegantSoprano,
        OriginalVoice
    )

    val girlPresets: List<VoicePreset> = allPresets.filter { it.isNaturalGirl }
}

/**
 * High-performance digital signal processing (DSP) for pitch-shifting and
 * acoustic formant shaping on raw 16-bit PCM audio samples.
 */
object VoiceDspProcessor {
    private const val TAG = "VoiceDspProcessor"

    /**
     * Pitch shifts and acoustic-filters 16-bit PCM audio into a natural girl's voice.
     * Uses Synchronized Overlap-Add (SOLA) with Hanning windowing to eliminate robotic buzz.
     */
    fun processVoice(
        inputPcm: ShortArray,
        sampleRate: Int = 16000,
        preset: VoicePreset
    ): ShortArray {
        if (inputPcm.isEmpty() || (!preset.isNaturalGirl && preset.pitchSemitones == 0f)) {
            return inputPcm.clone()
        }

        // Pitch shift ratio: R = 2^(semitones / 12)
        val pitchRatio = 2.0.pow(preset.pitchSemitones.toDouble() / 12.0).toFloat()
        val windowSize = (sampleRate * 0.025f).toInt() // 25ms window (~400 samples at 16kHz)
        val hopSize = windowSize / 2

        // Step 1: Highpass Filter (Cut adult chest frequencies below 200Hz)
        val filtered = applyLowCutFilter(inputPcm, sampleRate, cutoffHz = 220f)

        // Step 2: Pitch shift using Granular SOLA algorithm
        val pitched = pitchShiftSOLA(filtered, pitchRatio, windowSize, hopSize)

        // Step 3: Female vocal tract acoustic formant boosting (2.8kHz - 3.4kHz peaking + 7kHz breathiness)
        val formantEnhanced = applyFormantEq(
            pitched,
            sampleRate,
            formantRatio = preset.formantRatio,
            airiness = preset.airiness,
            warmth = preset.warmth
        )

        return formantEnhanced
    }

    /**
     * Synchronized Overlap-Add (SOLA) Pitch Shifting for 16-bit PCM.
     */
    private fun pitchShiftSOLA(
        input: ShortArray,
        pitchRatio: Float,
        windowSize: Int,
        hopIn: Int
    ): ShortArray {
        if (pitchRatio in 0.98f..1.02f) return input

        val hopOut = (hopIn / pitchRatio).toInt().coerceAtLeast(1)
        val outputLength = ((input.size / pitchRatio) * 1.05f).toInt().coerceAtLeast(input.size)
        val output = FloatArray(outputLength)
        val normWeight = FloatArray(outputLength)

        // Precompute Hanning Window
        val window = FloatArray(windowSize) { i ->
            (0.5f * (1.0f - cos(2.0 * PI * i / (windowSize - 1)))).toFloat()
        }

        var inPos = 0
        var outPos = 0

        while (inPos + windowSize < input.size && outPos + windowSize < outputLength) {
            // Find cross-correlation search range for smooth phase alignment
            val maxDelta = hopOut / 2
            var bestOffset = 0
            var maxCorr = -Float.MAX_VALUE

            if (outPos > 0 && maxDelta > 0) {
                for (delta in -maxDelta..maxDelta) {
                    val candidateOut = outPos + delta
                    if (candidateOut < 0 || candidateOut + windowSize >= outputLength) continue

                    var corr = 0.0f
                    val step = 4 // Subsample for fast correlation
                    var k = 0
                    while (k < windowSize) {
                        corr += input[inPos + k] * output[candidateOut + k]
                        k += step
                    }
                    if (corr > maxCorr) {
                        maxCorr = corr
                        bestOffset = delta
                    }
                }
            }

            val targetOut = (outPos + bestOffset).coerceIn(0, outputLength - windowSize)

            // Overlap and add windowed frame
            for (i in 0 until windowSize) {
                val sample = input[inPos + i] * window[i]
                output[targetOut + i] += sample
                normWeight[targetOut + i] += window[i]
            }

            inPos += hopIn
            outPos += hopOut
        }

        // Normalize overlap weights & convert to ShortArray
        val finalShorts = ShortArray(outPos.coerceAtMost(outputLength))
        for (i in finalShorts.indices) {
            val weight = normWeight[i]
            val valFloat = if (weight > 0.001f) output[i] / weight else output[i]
            finalShorts[i] = valFloat.coerceIn(-32767f, 32767f).toInt().toShort()
        }

        return finalShorts
    }

    /**
     * Biquad High-Pass filter to attenuate male vocal chest resonance (< 220Hz).
     */
    private fun applyLowCutFilter(input: ShortArray, sampleRate: Int, cutoffHz: Float): ShortArray {
        val output = ShortArray(input.size)
        val w0 = (2.0 * PI * cutoffHz / sampleRate).toFloat()
        val alpha = (sin(w0.toDouble()) / (2.0 * 0.707)).toFloat()
        val cosw0 = cos(w0.toDouble()).toFloat()

        val b0 = (1f + cosw0) / 2f
        val b1 = -(1f + cosw0)
        val b2 = (1f + cosw0) / 2f
        val a0 = 1f + alpha
        val a1 = -2f * cosw0
        val a2 = 1f - alpha

        var x1 = 0f
        var x2 = 0f
        var y1 = 0f
        var y2 = 0f

        for (i in input.indices) {
            val x0 = input[i].toFloat()
            val y0 = (b0 / a0) * x0 + (b1 / a0) * x1 + (b2 / a0) * x2 - (a1 / a0) * y1 - (a2 / a0) * y2

            x2 = x1
            x1 = x0
            y2 = y1
            y1 = y0

            output[i] = y0.coerceIn(-32767f, 32767f).toInt().toShort()
        }
        return output
    }

    /**
     * Boosts female vocal tract formant resonances (F1/F2 around 3000Hz) and adds
     * silky breathiness / air above 6500Hz for ultra-realistic female cadence.
     */
    private fun applyFormantEq(
        input: ShortArray,
        sampleRate: Int,
        formantRatio: Float,
        airiness: Float,
        warmth: Float
    ): ShortArray {
        val output = ShortArray(input.size)
        val formantCenterHz = (3000f * formantRatio).coerceAtMost(sampleRate * 0.45f)
        val gainFormant = 1.0f + (0.5f * formantRatio) // ~+3dB to +5dB formant boost

        // Peaking filter for formant resonance
        val w0 = (2.0 * PI * formantCenterHz / sampleRate).toFloat()
        val q = 1.4f
        val alpha = (sin(w0.toDouble()) / (2.0 * q)).toFloat()
        val a = sqrt(gainFormant.toDouble()).toFloat()
        val cosw0 = cos(w0.toDouble()).toFloat()

        val b0 = 1f + alpha * a
        val b1 = -2f * cosw0
        val b2 = 1f - alpha * a
        val a0 = 1f + alpha / a
        val a1 = -2f * cosw0
        val a2 = 1f - alpha / a

        var x1 = 0f
        var x2 = 0f
        var y1 = 0f
        var y2 = 0f

        // High-frequency breathiness coefficient (air shelf)
        val airFactor = airiness * 0.22f

        for (i in input.indices) {
            val x0 = input[i].toFloat()
            var y0 = (b0 / a0) * x0 + (b1 / a0) * x1 + (b2 / a0) * x2 - (a1 / a0) * y1 - (a2 / a0) * y2

            x2 = x1
            x1 = x0
            y2 = y1
            y1 = y0

            // Apply breathiness high-frequency sheen
            if (i > 0) {
                val diffHigh = x0 - input[i - 1].toFloat()
                y0 += diffHigh * airFactor
            }

            // Warmth compression to keep output soft and natural
            val softCompressed = (y0 * (0.90f + warmth * 0.10f))
            output[i] = softCompressed.coerceIn(-32767f, 32767f).toInt().toShort()
        }

        return output
    }

    /**
     * Wraps raw 16-bit PCM ShortArray into standard RIFF WAV byte array.
     */
    fun pcmToWavBytes(pcmShorts: ShortArray, sampleRate: Int = 16000): ByteArray {
        val pcmBytes = ByteArray(pcmShorts.size * 2)
        ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcmShorts)

        val totalAudioLen = pcmBytes.size
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF/WAVE header
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        val output = ByteArray(header.size + pcmBytes.size)
        System.arraycopy(header, 0, output, 0, header.size)
        System.arraycopy(pcmBytes, 0, output, header.size, pcmBytes.size)
        return output
    }
}

/**
 * 10-Second Voice Message Recorder with real-time waveform capture
 * and instant Natural Girl Voice DSP transformation.
 */
class VoiceMessageRecorder(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordedSeconds = MutableStateFlow(0f)
    val recordedSeconds: StateFlow<Float> = _recordedSeconds.asStateFlow()

    private val _waveforms = MutableStateFlow<List<Float>>(emptyList())
    val waveforms: StateFlow<List<Float>> = _waveforms.asStateFlow()

    private val _selectedPreset = MutableStateFlow(VoicePresets.NaturalSoftGirl)
    val selectedPreset: StateFlow<VoicePreset> = _selectedPreset.asStateFlow()

    private val _previewPlaying = MutableStateFlow(false)
    val previewPlaying: StateFlow<Boolean> = _previewPlaying.asStateFlow()

    private val _hasRecording = MutableStateFlow(false)
    val hasRecording: StateFlow<Boolean> = _hasRecording.asStateFlow()

    private var rawPcmBuffer = mutableListOf<Short>()
    private var processedPcmShorts: ShortArray? = null
    private var recordingJob: Job? = null
    private var playbackTrack: AudioTrack? = null
    private var audioRecord: AudioRecord? = null

    val sampleRate = 16000
    val maxDurationSeconds = 10f

    fun selectPreset(preset: VoicePreset) {
        _selectedPreset.value = preset
        // Re-process current recording if exists
        if (rawPcmBuffer.isNotEmpty()) {
            val shorts = rawPcmBuffer.toShortArray()
            processedPcmShorts = VoiceDspProcessor.processVoice(shorts, sampleRate, preset)
        }
    }

    fun startRecording(onAutoLimitReached: () -> Unit = {}) {
        stopPlayback()
        rawPcmBuffer.clear()
        processedPcmShorts = null
        _waveforms.value = emptyList()
        _recordedSeconds.value = 0f
        _hasRecording.value = false
        _isRecording.value = true

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        recordingJob = scope.launch(Dispatchers.IO) {
            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize
                )

                audioRecord?.startRecording()
                val buffer = ShortArray(minBufferSize / 2)
                val startTime = System.currentTimeMillis()
                val waveList = mutableListOf<Float>()

                while (isActive && _isRecording.value) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        var maxAmp = 0
                        for (i in 0 until read) {
                            val sample = buffer[i]
                            rawPcmBuffer.add(sample)
                            val amp = abs(sample.toInt())
                            if (amp > maxAmp) maxAmp = amp
                        }

                        // Normalized waveform amplitude for UI visualizer
                        val normalized = (maxAmp / 32767f).coerceIn(0.08f, 1f)
                        waveList.add(normalized)
                        if (waveList.size > 36) waveList.removeAt(0)
                        _waveforms.value = waveList.toList()
                    }

                    val elapsedSec = (System.currentTimeMillis() - startTime) / 1000f
                    _recordedSeconds.value = elapsedSec.coerceAtMost(maxDurationSeconds)

                    if (elapsedSec >= maxDurationSeconds) {
                        // Max 10-second limit reached!
                        launch(Dispatchers.Main) {
                            stopRecording()
                            onAutoLimitReached()
                        }
                        break
                    }
                }
            } catch (e: SecurityException) {
                Log.e("VoiceRecorder", "Permission missing for audio recording", e)
                _isRecording.value = false
            } catch (e: Exception) {
                Log.e("VoiceRecorder", "Error during recording", e)
                _isRecording.value = false
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
                audioRecord = null
            }
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        _isRecording.value = false
        recordingJob?.cancel()

        // Process raw recording through the selected Natural Girl Voice filter
        val rawShorts = rawPcmBuffer.toShortArray()
        if (rawShorts.isNotEmpty()) {
            processedPcmShorts = VoiceDspProcessor.processVoice(rawShorts, sampleRate, _selectedPreset.value)
            _hasRecording.value = true
        }
    }

    fun discardRecording() {
        stopPlayback()
        _isRecording.value = false
        _hasRecording.value = false
        _recordedSeconds.value = 0f
        _waveforms.value = emptyList()
        rawPcmBuffer.clear()
        processedPcmShorts = null
    }

    fun playPreview(playOriginal: Boolean = false) {
        stopPlayback()
        val dataToPlay = if (playOriginal) {
            rawPcmBuffer.toShortArray()
        } else {
            processedPcmShorts ?: rawPcmBuffer.toShortArray()
        }

        if (dataToPlay.isEmpty()) return

        scope.launch(Dispatchers.IO) {
            try {
                _previewPlaying.value = true
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(dataToPlay.size * 2)

                playbackTrack = AudioTrack.Builder()
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                playbackTrack?.play()
                playbackTrack?.write(dataToPlay, 0, dataToPlay.size)

                // Wait until played
                val playDurationMs = (dataToPlay.size * 1000L) / sampleRate
                delay(playDurationMs + 100)
            } catch (e: Exception) {
                Log.e("VoiceRecorder", "Preview error", e)
            } finally {
                stopPlayback()
            }
        }
    }

    fun stopPlayback() {
        try {
            playbackTrack?.stop()
            playbackTrack?.release()
        } catch (_: Exception) {}
        playbackTrack = null
        _previewPlaying.value = false
    }

    /**
     * Prepares and exports the transformed Natural Girl voice message as a
     * base64-encoded WAV container string ready for Firebase chat transmission.
     */
    fun exportProcessedWavBase64(): Pair<String, Int> {
        val shorts = processedPcmShorts ?: rawPcmBuffer.toShortArray()
        if (shorts.isEmpty()) return Pair("", 0)
        val wavBytes = VoiceDspProcessor.pcmToWavBytes(shorts, sampleRate)
        val base64 = Base64.encodeToString(wavBytes, Base64.NO_WRAP)
        val durationSec = (_recordedSeconds.value.toInt()).coerceAtLeast(1)
        return Pair(base64, durationSec)
    }

    /**
     * Saves processed WAV to application cache file.
     */
    fun saveToLocalCache(fileName: String = "voice_msg_${System.currentTimeMillis()}.wav"): File? {
        val shorts = processedPcmShorts ?: rawPcmBuffer.toShortArray()
        if (shorts.isEmpty()) return null
        return try {
            val wavBytes = VoiceDspProcessor.pcmToWavBytes(shorts, sampleRate)
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { it.write(wavBytes) }
            file
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Failed to save local cache file", e)
            null
        }
    }
}

/**
 * Real-time Voice Call Audio Processor for live calls.
 * Runs continuous live mic captures through the DSP pitch and formant shifters
 * for natural girl voice during calls.
 */
class LiveVoiceCallProcessor {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _currentPreset = MutableStateFlow(VoicePresets.NaturalSoftGirl)
    val currentPreset: StateFlow<VoicePreset> = _currentPreset.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _liveAudioLevels = MutableStateFlow<List<Float>>(listOf(0.2f, 0.4f, 0.6f, 0.3f, 0.5f))
    val liveAudioLevels: StateFlow<List<Float>> = _liveAudioLevels.asStateFlow()

    private var processJob: Job? = null
    private var monitorTrack: AudioTrack? = null
    private var liveAudioRecord: AudioRecord? = null
    private val sampleRate = 16000

    fun toggleVoiceChanger(enabled: Boolean? = null) {
        _isEnabled.value = enabled ?: !_isEnabled.value
    }

    fun setPreset(preset: VoicePreset) {
        _currentPreset.value = preset
    }

    fun toggleMonitoring(enable: Boolean? = null) {
        val newState = enable ?: !_isMonitoring.value
        _isMonitoring.value = newState
        if (!newState) {
            stopMonitorTrack()
        }
    }

    fun startLiveProcessing() {
        if (processJob != null && processJob?.isActive == true) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(1024)

        processJob = scope.launch(Dispatchers.IO) {
            try {
                liveAudioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize
                )

                if (liveAudioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    liveAudioRecord?.startRecording()
                }

                val inputBuf = ShortArray(minBufferSize / 2)

                while (isActive) {
                    val read = liveAudioRecord?.read(inputBuf, 0, inputBuf.size) ?: 0
                    if (read > 0) {
                        val chunk = inputBuf.copyOf(read)
                        val processed = if (_isEnabled.value) {
                            VoiceDspProcessor.processVoice(chunk, sampleRate, _currentPreset.value)
                        } else {
                            chunk
                        }

                        // Calculate visual amplitude levels
                        var maxAmp = 0
                        for (i in 0 until read) {
                            val a = abs(processed[i].toInt())
                            if (a > maxAmp) maxAmp = a
                        }
                        val norm = (maxAmp / 32767f).coerceIn(0.1f, 1f)
                        val levels = _liveAudioLevels.value.toMutableList()
                        levels.add(norm)
                        if (levels.size > 8) levels.removeAt(0)
                        _liveAudioLevels.value = levels

                        // Play to monitoring headphone track if enabled
                        if (_isMonitoring.value) {
                            playMonitorChunk(processed)
                        }
                    } else {
                        delay(20)
                    }
                }
            } catch (e: Exception) {
                Log.e("LiveVoiceCall", "Live voice processing error", e)
            } finally {
                stopLiveProcessing()
            }
        }
    }

    private fun playMonitorChunk(shorts: ShortArray) {
        try {
            if (monitorTrack == null) {
                val bufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(2048)

                monitorTrack = AudioTrack.Builder()
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                monitorTrack?.play()
            }
            monitorTrack?.write(shorts, 0, shorts.size)
        } catch (_: Exception) {}
    }

    private fun stopMonitorTrack() {
        try {
            monitorTrack?.stop()
            monitorTrack?.release()
        } catch (_: Exception) {}
        monitorTrack = null
    }

    fun stopLiveProcessing() {
        processJob?.cancel()
        processJob = null
        try {
            liveAudioRecord?.stop()
            liveAudioRecord?.release()
        } catch (_: Exception) {}
        liveAudioRecord = null
        stopMonitorTrack()
    }
}
