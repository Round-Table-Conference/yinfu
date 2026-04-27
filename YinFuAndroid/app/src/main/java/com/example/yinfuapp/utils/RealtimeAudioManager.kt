package com.example.yinfuapp.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RealtimeAudioManager(private val context: Context) {

    companion object {
        private const val TAG = "RealtimeAudioManager"
        private const val BASE_URL = "https://api.siliconflow.cn/v1"
        private const val API_KEY = "sk-yckdyvnnixuckmwqkqjpbjltfipybwuviznbkorrzbegzbyf"

        private const val SILENCE_THRESHOLD = 2000
        private const val SILENCE_DURATION_MS = 2000L
        private const val MIN_SPEECH_LENGTH_MS = 300
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = AtomicBoolean(false)
    private var recordingThread: Thread? = null
    private var vadHandler: Handler? = null
    private var vadRunnable: Runnable? = null

    private var tempAudioFile: File? = null

    var onRecordingStarted: (() -> Unit)? = null
    var onRecordingStopped: (() -> Unit)? = null
    var onSpeechRecognized: ((String) -> Unit)? = null
    var onAISpeakingStarted: (() -> Unit)? = null
    var onAISpeakingFinished: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onSpeakingStarted: (() -> Unit)? = null
    var onSilenceDetected: (() -> Unit)? = null

    private var isAISpeaking = false
    private var audioPlayer: AudioPlayerHelper? = null

    private var lastSpeechTime = 0L
    private var silenceStartTime = 0L
    private var isInSpeech = false
    private var hasReceivedAudio = false
    private var speechStartTime = 0L
    private var silenceDetected = false
    private var volumeListener: ((Int) -> Unit)? = null

    fun startRecording() {
        if (isRecording.get()) return

        resetVADState()

        try {
            tempAudioFile = File(context.cacheDir, "realtime_audio_${System.currentTimeMillis()}.m4a")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(128000)
                setOutputFile(tempAudioFile?.absolutePath)
                prepare()
                start()
            }

            isRecording.set(true)
            vadHandler = Handler(Looper.getMainLooper())

            recordingThread = Thread {
                while (isRecording.get()) {
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        break
                    }

                    if (isRecording.get()) {
                        val maxAmplitude = try {
                            mediaRecorder?.maxAmplitude ?: 0
                        } catch (e: Exception) {
                            0
                        }
                        processVolume(maxAmplitude)
                    }
                }
            }
            recordingThread?.start()

            startVADCheck()

            mainHandler.post { onRecordingStarted?.invoke() }
            Log.d(TAG, "Recording started")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            mainHandler.post { onError?.invoke("启动录音失败: ${e.message}") }
        }
    }

    private fun resetVADState() {
        lastSpeechTime = System.currentTimeMillis()
        silenceStartTime = 0L
        isInSpeech = false
        hasReceivedAudio = false
        speechStartTime = 0L
        silenceDetected = false
    }

    private fun processVolume(volume: Int) {
        val currentTime = System.currentTimeMillis()

        if (silenceDetected) return

        if (volume > SILENCE_THRESHOLD) {
            if (!isInSpeech) {
                isInSpeech = true
                speechStartTime = currentTime
                hasReceivedAudio = true
                mainHandler.post { onSpeakingStarted?.invoke() }
            }
            lastSpeechTime = currentTime
            silenceStartTime = 0L
        } else {
            if (isInSpeech && silenceStartTime == 0L) {
                silenceStartTime = currentTime
            }
        }
    }

    private fun startVADCheck() {
        vadRunnable = object : Runnable {
            override fun run() {
                if (!isRecording.get()) return

                val currentTime = System.currentTimeMillis()

                if (isInSpeech && silenceStartTime > 0) {
                    val silenceDuration = currentTime - silenceStartTime
                    if (silenceDuration >= SILENCE_DURATION_MS && speechStartTime > 0) {
                        val speechDuration = silenceStartTime - speechStartTime
                        if (speechDuration >= MIN_SPEECH_LENGTH_MS) {
                            Log.d(TAG, "Silence detected for ${SILENCE_DURATION_MS}ms, stopping recording")
                            silenceDetected = true
                            mainHandler.post { onSilenceDetected?.invoke() }
                            stopRecordingAndProcess()
                            return
                        }
                    }
                }

                if (hasReceivedAudio && lastSpeechTime > 0 && isInSpeech) {
                    val totalSilence = currentTime - lastSpeechTime
                    if (totalSilence >= SILENCE_DURATION_MS && speechStartTime > 0) {
                        val speechDuration = lastSpeechTime - speechStartTime
                        if (speechDuration >= MIN_SPEECH_LENGTH_MS) {
                            Log.d(TAG, "Speech ended naturally, stopping recording")
                            silenceDetected = true
                            mainHandler.post { onSilenceDetected?.invoke() }
                            stopRecordingAndProcess()
                            return
                        }
                    }
                }

                vadHandler?.postDelayed(this, 100)
            }
        }
        vadHandler?.post(vadRunnable!!)
    }

    fun stopRecordingAndProcess() {
        if (!isRecording.get()) return

        isRecording.set(false)

        vadHandler?.removeCallbacks(vadRunnable ?: return)
        vadHandler = null
        vadRunnable = null

        try {
            recordingThread?.join(500)
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null

            mainHandler.post { onRecordingStopped?.invoke() }
            Log.d(TAG, "Recording stopped")

            tempAudioFile?.let { file ->
                if (file.length() > 0) {
                    processAudioFile(file)
                } else {
                    mainHandler.post { onError?.invoke("未检测到语音输入") }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            mainHandler.post { onError?.invoke("停止录音失败: ${e.message}") }
        }
    }

    private fun processAudioFile(file: File) {
        Thread {
            try {
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        file.name,
                        file.asRequestBody("audio/mpeg".toMediaType())
                    )
                    .addFormDataPart("model", "TeleAI/TeleSpeechASR")
                    .build()

                val request = Request.Builder()
                    .url("$BASE_URL/audio/transcriptions")
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d(TAG, "ASR Response: $responseBody")
                Log.d(TAG, "ASR Response Code: ${response.code}")

                if (!response.isSuccessful) {
                    mainHandler.post {
                        onError?.invoke("语音识别请求失败: ${response.code}")
                    }
                    file.delete()
                    return@Thread
                }

                if (!responseBody.isNullOrEmpty()) {
                    try {
                        val json = gson.fromJson(responseBody, JsonObject::class.java)
                        val text = json.get("text")?.asString
                            ?: json.get("result")?.asString
                            ?: ""

                        if (text.isNotEmpty()) {
                            mainHandler.post {
                                onSpeechRecognized?.invoke(text)
                            }
                        } else {
                            val errorMsg = json.get("error")?.asString
                                ?: json.get("message")?.asString
                                ?: "未能识别语音"
                            mainHandler.post {
                                onError?.invoke(errorMsg)
                            }
                        }
                    } catch (parseError: Exception) {
                        Log.e(TAG, "JSON parse error: $parseError, body: $responseBody")
                        if (responseBody.contains("text")) {
                            val regex = """"text"\s*:\s*"([^"]*)"""".toRegex()
                            val match = regex.find(responseBody)
                            if (match != null) {
                                val text = match.groupValues[1]
                                mainHandler.post {
                                    onSpeechRecognized?.invoke(text)
                                }
                            } else {
                                mainHandler.post {
                                    onError?.invoke("语音识别格式异常，请重试")
                                }
                            }
                        } else {
                            mainHandler.post {
                                onError?.invoke("语音识别失败: 响应格式错误")
                            }
                        }
                    }
                } else {
                    mainHandler.post {
                        onError?.invoke("语音识别返回为空")
                    }
                }

                file.delete()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to process audio", e)
                mainHandler.post { onError?.invoke("语音识别失败: ${e.message}") }
            }
        }.start()
    }

    fun speakAIResponse(text: String) {
        if (isAISpeaking) return

        isAISpeaking = true
        mainHandler.post { onAISpeakingStarted?.invoke() }

        Thread {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("model", "FunAudioLLM/CosyVoice2-0.5B")
                    addProperty("input", text)
                    addProperty("voice", "FunAudioLLM/CosyVoice2-0.5B:alex")
                    addProperty("response_format", "mp3")
                }.toString()

                val request = Request.Builder()
                    .url("$BASE_URL/audio/speech")
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val audioBytes = response.body?.bytes()

                if (audioBytes != null && audioBytes.isNotEmpty()) {
                    val audioFile = File(context.cacheDir, "ai_speech_${System.currentTimeMillis()}.mp3")
                    java.io.FileOutputStream(audioFile).use { it.write(audioBytes) }

                    mainHandler.post {
                        playAudioFile(audioFile)
                    }
                } else {
                    mainHandler.post {
                        isAISpeaking = false
                        onAISpeakingFinished?.invoke()
                        onError?.invoke("AI 响应为空")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to get AI speech", e)
                mainHandler.post {
                    isAISpeaking = false
                    onAISpeakingFinished?.invoke()
                    onError?.invoke("AI 语音合成失败: ${e.message}")
                }
            }
        }.start()
    }

    private fun playAudioFile(file: File) {
        audioPlayer = AudioPlayerHelper(context).apply {
            setOnCompletionListener {
                isAISpeaking = false
                mainHandler.post {
                    onAISpeakingFinished?.invoke()
                }
                file.delete()
            }
        }
        audioPlayer?.playFile(file)
    }

    fun stopAISpeaking() {
        isAISpeaking = false
        audioPlayer?.stop()
        audioPlayer?.release()
        audioPlayer = null
    }

    fun isAISpeaking() = isAISpeaking

    fun release() {
        isRecording.set(false)

        vadHandler?.removeCallbacks(vadRunnable ?: return)
        vadHandler = null
        vadRunnable = null

        isAISpeaking = false

        try {
            recordingThread?.join(500)
            mediaRecorder?.stop()
            mediaRecorder?.release()
            audioPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release resources", e)
        }

        mediaRecorder = null
        audioPlayer = null
        recordingThread = null
    }
}