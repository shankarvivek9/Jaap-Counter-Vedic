package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.sin

class OmSoundGenerator {
    private var audioTrack: AudioTrack? = null
    private var playJob: Job? = null
    @Volatile private var isPlaying = false

    fun start() {
        if (isPlaying) return
        isPlaying = true

        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_OUT_STEREO
        val audioFormat = AudioFormat.ENCODING_PCM_FLOAT
        val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

        try {
            audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelConfig)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e("OmSoundGenerator", "Failed to initialize AudioTrack", e)
            isPlaying = false
            return
        }

        playJob = CoroutineScope(Dispatchers.Default).launch {
            val writeBuffer = FloatArray(22050) // Write 0.25 seconds of stereo audio at a time
            var sampleIndex = 0L

            // Sacred Sound frequencies representing ancient tuning
            val fSub = 68.05 // Deep base sub-octave (32' ground vibration)
            val f0 = 136.1   // Fundamental (Pranava cosmic sound / Earth frequency)
            val f1 = 272.2   // 2nd Harmonic
            val f2 = 408.3   // 3rd Harmonic
            val f3 = 544.4   // 4th Harmonic

            while (isPlaying) {
                val track = audioTrack ?: break
                for (i in 0 until writeBuffer.size step 2) {
                    val t = sampleIndex / sampleRate.toDouble()
                    
                    // Slow wave respiration LFO (7-second yogic pranayama breathing cycle)
                    val lfo = 0.75 + 0.25 * sin(2.0 * Math.PI * (1.0 / 7.0) * t)
                    
                    // Natural singing bowl multi-speed organic beating
                    val vibratoLeft = sin(2.0 * Math.PI * 3.5 * t) * 0.008
                    val vibratoRight = sin(2.0 * Math.PI * 3.8 * t) * 0.008

                    // Synthesis formula: Left Channel
                    val leftValue = (
                        0.40 * sin(2.0 * Math.PI * (fSub + vibratoLeft) * t) +
                        0.90 * sin(2.0 * Math.PI * (f0 + vibratoLeft) * t) +
                        0.30 * sin(2.0 * Math.PI * f1 * t) +
                        0.15 * sin(2.0 * Math.PI * f2 * t) +
                        0.08 * sin(2.0 * Math.PI * f3 * t)
                    )

                    // Synthesis formula: Right Channel (slightly detuned phase for immersive stereo depth)
                    val rightValue = (
                        0.40 * sin(2.0 * Math.PI * (fSub + 0.12 + vibratoRight) * t) +
                        0.90 * sin(2.0 * Math.PI * (f0 + 0.25 + vibratoRight) * t) +
                        0.30 * sin(2.0 * Math.PI * (f1 + 0.38) * t) +
                        0.15 * sin(2.0 * Math.PI * (f2 * 0.52) * t) +
                        0.08 * sin(2.0 * Math.PI * (f3 * 0.64) * t)
                    )

                    // Gain compression to avoid clipping
                    val gain = 0.20f
                    
                    if (i < writeBuffer.size) {
                        writeBuffer[i] = (leftValue * lfo * gain).toFloat().coerceIn(-1.0f, 1.0f)
                    }
                    if (i + 1 < writeBuffer.size) {
                        writeBuffer[i + 1] = (rightValue * lfo * gain).toFloat().coerceIn(-1.0f, 1.0f)
                    }
                    sampleIndex++
                }

                if (isPlaying) {
                    track.write(writeBuffer, 0, writeBuffer.size, AudioTrack.WRITE_BLOCKING)
                }
            }
        }
    }

    /**
     * Checks whether the background synthesis loop is active.
     */
    fun isActive(): Boolean {
        return isPlaying
    }

    fun stop() {
        isPlaying = false
        playJob?.cancel()
        playJob = null
        try {
            audioTrack?.apply {
                if (state == AudioTrack.STATE_INITIALIZED) {
                    try {
                        stop()
                    } catch (e: Exception) {
                        // ignore if already stopped or invalid state
                    }
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("OmSoundGenerator", "Error stopping AudioTrack", e)
        }
        audioTrack = null
    }
}
