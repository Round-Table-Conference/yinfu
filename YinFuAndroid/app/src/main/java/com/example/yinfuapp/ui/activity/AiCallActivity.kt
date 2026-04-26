package com.example.yinfuapp.ui.activity

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yinfuapp.R
import com.example.yinfuapp.data.adapter.ChatAdapter
import com.example.yinfuapp.data.api.SiliconFlowClient
import com.example.yinfuapp.data.model.ChatMessage
import com.example.yinfuapp.databinding.ActivityAiCallBinding
import com.example.yinfuapp.utils.AudioPlayerHelper
import com.example.yinfuapp.utils.RealtimeAudioManager
import kotlinx.coroutines.launch
import org.json.JSONArray

class AiCallActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CALL_TYPE = "call_type"
        const val CALL_TYPE_CHAT = "chat"
        const val CALL_TYPE_DIAGNOSIS = "diagnosis"

        fun start(context: Context, callType: String) {
            val intent = Intent(context, AiCallActivity::class.java).apply {
                putExtra(EXTRA_CALL_TYPE, callType)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityAiCallBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    private lateinit var realtimeAudioManager: RealtimeAudioManager
    private lateinit var audioPlayer: AudioPlayerHelper

    private var isMuted = false
    private var isSpeakerOn = true
    private var isInCall = true

    private var callType = CALL_TYPE_CHAT
    private var systemPrompt = ""

    private val waveViews by lazy {
        listOf(
            binding.waveContainer.findViewById<View>(R.id.wave_1),
            binding.waveContainer.findViewById<View>(R.id.wave_2),
            binding.waveContainer.findViewById<View>(R.id.wave_3),
            binding.waveContainer.findViewById<View>(R.id.wave_4),
            binding.waveContainer.findViewById<View>(R.id.wave_5),
            binding.waveContainer.findViewById<View>(R.id.wave_6),
            binding.waveContainer.findViewById<View>(R.id.wave_7)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: CALL_TYPE_CHAT
        setupUI()
        setupRecyclerView()
        setupAudioManager()
        setupClickListeners()
        setupSystemPrompt()

        if (messages.isEmpty()) {
            addWelcomeMessage()
        }

        startWaveAnimation()
        startCall()
    }

    private fun setupUI() {
        binding.callTypeText.text = if (callType == CALL_TYPE_DIAGNOSIS) "AI 医疗诊断" else "AI 智能助手"
        binding.userNameText.text = if (callType == CALL_TYPE_DIAGNOSIS) "AI 医疗助手" else "AI 智能助手"
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AiCallActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupAudioManager() {
        realtimeAudioManager = RealtimeAudioManager(this)
        audioPlayer = AudioPlayerHelper(this)

        realtimeAudioManager.onRecordingStarted = {
            runOnUiThread {
                updateStatus("请说话...")
                updateWaveState(true)
                binding.muteButton.setIconResource(R.drawable.ic_mic)
                binding.muteButton.setBackgroundColor(getColor(R.color.danger))
            }
        }

        realtimeAudioManager.onSpeakingStarted = {
            runOnUiThread {
                updateStatus("正在说话...")
                updateWaveState(true)
            }
        }

        realtimeAudioManager.onSilenceDetected = {
            runOnUiThread {
                updateStatus("正在识别...")
                updateWaveState(false)
            }
        }

        realtimeAudioManager.onRecordingStopped = {
            runOnUiThread {
                updateStatus("识别中...")
                updateWaveState(false)
            }
        }

        realtimeAudioManager.onSpeechRecognized = { text ->
            runOnUiThread {
                val userMessage = ChatMessage(content = text, isFromUser = true)
                messages.add(userMessage)
                chatAdapter.submitList(messages.toList())
                scrollToBottom()
                updateStatus("AI思考中...")
                chatWithAIForCall(text)
            }
        }

        realtimeAudioManager.onAISpeakingStarted = {
            runOnUiThread {
                updateStatus("AI回复中...")
                binding.muteButton.setIconResource(R.drawable.ic_mic_off)
                binding.muteButton.setBackgroundColor(getColor(R.color.dark_gray))
                updateWaveState(true)
            }
        }

        realtimeAudioManager.onAISpeakingFinished = {
            runOnUiThread {
                if (isInCall) {
                    updateStatus("请说话...")
                    binding.muteButton.setIconResource(R.drawable.ic_mic)
                    binding.muteButton.setBackgroundColor(getColor(R.color.danger))
                    updateWaveState(true)

                    android.os.Handler(mainLooper).postDelayed({
                        if (isInCall) {
                            realtimeAudioManager.startRecording()
                        }
                    }, 500)
                }
            }
        }

        realtimeAudioManager.onError = { error ->
            runOnUiThread {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                if (isInCall) {
                    updateStatus("请说话...")
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.closeButton.setOnClickListener {
            endCall()
        }

        binding.hangupButton.setOnClickListener {
            endCall()
        }

        binding.muteButton.setOnClickListener {
            toggleMute()
        }

        binding.speakerButton.setOnClickListener {
            toggleSpeaker()
        }
    }

    private fun setupSystemPrompt() {
        systemPrompt = if (callType == CALL_TYPE_DIAGNOSIS) {
            "您好！我是AI医疗助手。根据您描述的症状，我可以提供一些初步的健康咨询建议。请告诉我您有哪些不适症状？"
        } else {
            "你是一个友好的AI助手，可以和用户进行自然的对话交流。"
        }
    }

    private fun addWelcomeMessage() {
        val welcomeText = if (callType == CALL_TYPE_DIAGNOSIS) {
            "您好！我是AI医疗助手。根据您描述的症状，我可以提供一些初步的健康咨询建议。请告诉我您有哪些不适症状？"
        } else {
            "您好！我是AI智能助手，很高兴为您服务。有什么我可以帮助您的吗？"
        }
        val welcomeMsg = ChatMessage(content = welcomeText, isFromUser = false)
        messages.add(welcomeMsg)
        chatAdapter.submitList(messages.toList())
    }

    private fun startCall() {
        isInCall = true
        realtimeAudioManager.startRecording()
    }

    private fun endCall() {
        isInCall = false
        realtimeAudioManager.stopAISpeaking()
        realtimeAudioManager.release()
        audioPlayer.release()
        finish()
    }

    private fun toggleMute() {
        isMuted = !isMuted
        if (isMuted) {
            realtimeAudioManager.stopRecordingAndProcess()
            binding.muteButton.setIconResource(R.drawable.ic_mic_off)
            binding.muteButton.setBackgroundColor(getColor(R.color.dark_gray))
            updateStatus("已静音")
            updateWaveState(false)
        } else {
            binding.muteButton.setIconResource(R.drawable.ic_mic)
            binding.muteButton.setBackgroundColor(getColor(R.color.danger))
            updateStatus("请说话...")
            updateWaveState(true)
            realtimeAudioManager.startRecording()
        }
    }

    private fun toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = isSpeakerOn

        if (isSpeakerOn) {
            binding.speakerButton.setBackgroundColor(getColor(R.color.danger))
            Toast.makeText(this, "扬声器已开启", Toast.LENGTH_SHORT).show()
        } else {
            binding.speakerButton.setBackgroundColor(getColor(R.color.dark_gray))
            Toast.makeText(this, "扬声器已关闭", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateStatus(status: String) {
        binding.statusText.text = status
    }

    private fun scrollToBottom() {
        binding.chatRecyclerView.post {
            if (messages.isNotEmpty()) {
                binding.chatRecyclerView.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    private fun startWaveAnimation() {
        waveViews.forEachIndexed { index, view ->
            animateWave(view, index)
        }
    }

    private fun animateWave(view: View, index: Int) {
        val randomDuration = (300..800).random().toLong()
        val randomHeight = (20..80)

        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.5f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1.5f, 0.5f)

        ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).apply {
            duration = randomDuration
            repeatCount = ObjectAnimator.INFINITE
            startDelay = index * 100L
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun updateWaveState(isActive: Boolean) {
        waveViews.forEach { view ->
            view.alpha = if (isActive) 1f else 0.3f
        }
    }

    private fun chatWithAIForCall(userMessage: String) {
        lifecycleScope.launch {
            try {
                val requestMessages = mutableListOf<Map<String, Any>>()
                requestMessages.add(mapOf(
                    "role" to "system",
                    "content" to listOf(mapOf("type" to "text", "text" to systemPrompt))
                ))

                messages.filter { !it.isFromUser && !it.content.startsWith("您好！") }
                    .forEach {
                        requestMessages.add(mapOf(
                            "role" to "assistant",
                            "content" to listOf(mapOf("type" to "text", "text" to it.content))
                        ))
                    }

                requestMessages.add(mapOf(
                    "role" to "user",
                    "content" to listOf(mapOf("type" to "text", "text" to userMessage))
                ))

                val result = SiliconFlowClient.chatWithText(requestMessages)

                result.onSuccess { aiContent ->
                    val aiMessage = ChatMessage(
                        content = aiContent,
                        isFromUser = false
                    )
                    messages.add(aiMessage)
                    chatAdapter.submitList(messages.toList())
                    scrollToBottom()

                    if (isInCall) {
                        realtimeAudioManager.speakAIResponse(aiContent)
                    }
                }.onFailure { error ->
                    if (isInCall) {
                        realtimeAudioManager.onError?.invoke("AI 回复失败: ${error.message}")
                    } else {
                        Toast.makeText(this@AiCallActivity, "AI 回复失败: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                if (isInCall) {
                    realtimeAudioManager.onError?.invoke("网络错误: ${e.message}")
                } else {
                    Toast.makeText(this@AiCallActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isInCall) {
            realtimeAudioManager.stopAISpeaking()
        }
        realtimeAudioManager.release()
        audioPlayer.release()
    }
}