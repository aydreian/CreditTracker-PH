package com.example.credittrackph.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingStartTime: Long = 0L

    var isRecording: Boolean = false
        private set

    fun startRecording(): Boolean {
        if (isRecording) {
            cancelRecording()
        }

        return try {
            val audioFile = File(context.cacheDir, "voice_input.m4a")
            if (audioFile.exists()) {
                audioFile.delete()
            }
            currentOutputFile = audioFile

            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            recorder = newRecorder
            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            Log.d("AudioRecorderHelper", "Recording started: ${audioFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to start recording", e)
            cancelRecording()
            false
        }
    }

    fun stopRecording(): File? {
        if (!isRecording || recorder == null) return null

        val durationMs = System.currentTimeMillis() - recordingStartTime
        return try {
            // MediaRecorder requires at least ~300ms to produce a valid audio header
            if (durationMs < 350) {
                Thread.sleep(350 - durationMs)
            }
            recorder?.apply {
                stop()
                reset()
                release()
            }
            recorder = null
            isRecording = false

            val file = currentOutputFile
            if (file != null && file.exists() && file.length() > 0) {
                Log.d("AudioRecorderHelper", "Recording finished successfully. Size: ${file.length()} bytes")
                file
            } else {
                Log.w("AudioRecorderHelper", "Recorded file is empty or missing")
                null
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to stop recording cleanly", e)
            cancelRecording()
            null
        }
    }

    fun cancelRecording() {
        try {
            recorder?.apply {
                try { stop() } catch (_: Exception) {}
                reset()
                release()
            }
        } catch (_: Exception) {}
        recorder = null
        isRecording = false

        currentOutputFile?.let {
            if (it.exists()) it.delete()
        }
        currentOutputFile = null
    }

    fun getMaxAmplitude(): Int {
        return try {
            if (isRecording) recorder?.maxAmplitude ?: 0 else 0
        } catch (_: Exception) {
            0
        }
    }
}
