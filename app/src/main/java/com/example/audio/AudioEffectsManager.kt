package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-end cinematic tactile and ambient audio feedback engine.
 * Synthesizes pure PCM sine and frequency-swept tones dynamically,
 * eliminating the need for bulky external audio files while ensuring zero-latency.
 */
class AudioEffectsManager {

    private val audioScope = CoroutineScope(Dispatchers.Default)

    fun playTactileClick(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            try {
                val sampleRate = 44100
                val durationMs = 35
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    // Exponential drop from 380Hz down to 160Hz
                    val freq = 380.0 * exp(-0.86 * progress)
                    val t = i.toDouble() / sampleRate
                    val envelope = (1.0 - progress) * 0.18
                    val sample = sin(2.0 * PI * freq * t) * envelope
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playHyperspaceSequence(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            try {
                val sampleRate = 44100
                val durationMs = 1600
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    // Accelerating cinematic frequency sweep from 50Hz to 600Hz
                    val freq = 50.0 + 550.0 * (progress * progress)
                    val t = i.toDouble() / sampleRate
                    // Smooth envelope ramp up and exponential decay
                    val amp = if (progress < 0.7) {
                        (progress / 0.7) * 0.22
                    } else {
                        0.22 * exp(-4.0 * (progress - 0.7))
                    }
                    val sample = sin(2.0 * PI * freq * t) * amp
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            audioScope.launch {
                kotlinx.coroutines.delay((buffer.size * 1000L / sampleRate) + 100)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
