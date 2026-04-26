package com.example.yinfuapp.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yinfuapp.R
import com.example.yinfuapp.data.adapter.ChatAdapter
import com.example.yinfuapp.data.api.SiliconFlowClient
import com.example.yinfuapp.data.local.ChatHistoryManager
import com.example.yinfuapp.data.model.ChatMessage
import com.example.yinfuapp.databinding.FragmentAiDiagnosisBinding
import com.example.yinfuapp.utils.AudioPlayerHelper
import com.example.yinfuapp.utils.RealtimeAudioManager
import com.example.yinfuapp.utils.VoiceHelper
import kotlinx.coroutines.launch
import java.io.File

class AiDiagnosisFragment : Fragment() {

    private var _binding: FragmentAiDiagnosisBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var chatHistoryManager: ChatHistoryManager
    private lateinit var voiceHelper: VoiceHelper
    private lateinit var audioPlayer: AudioPlayerHelper
    private lateinit var realtimeAudioManager: RealtimeAudioManager
    private var isInCall = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecording()
        } else {
            Toast.makeText(context, "需要麦克风权限才能使用语音功能", Toast.LENGTH_SHORT).show()
        }
    }

    private val systemPrompt = """你是专业AI医疗助手。请简洁回复用户症状咨询。
规则：
1. 详细的追问用户信息，给出专业的建议
2. 严重症状请及时就医
3. 回答简洁明了，不超过200字
4. 不确定时建议咨询专业医生"""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiDiagnosisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatHistoryManager = ChatHistoryManager.getInstance(requireContext())
        voiceHelper = VoiceHelper(requireContext())
        audioPlayer = AudioPlayerHelper(requireContext())
        realtimeAudioManager = RealtimeAudioManager(requireContext())
        setupRealtimeAudioManager()
        setupRecyclerView()
        setupClickListeners()
        loadHistory()
    }

    private fun setupRealtimeAudioManager() {
        realtimeAudioManager.onRecordingStarted = {
            activity?.runOnUiThread {
                updateCallUI(true, "请说话...")
                binding.callButton.setIconResource(R.drawable.ic_stop)
                binding.callButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.danger))
                binding.callHint.text = "正在收音"
                binding.callHint.setTextColor(ContextCompat.getColor(requireContext(), R.color.danger))
            }
        }
        realtimeAudioManager.onSpeakingStarted = {
            activity?.runOnUiThread {
                binding.callStatusText.text = "正在说话..."
                binding.callIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success))
            }
        }
        realtimeAudioManager.onSilenceDetected = {
            activity?.runOnUiThread {
                binding.callStatusText.text = "正在识别..."
            }
        }
        realtimeAudioManager.onRecordingStopped = {
            activity?.runOnUiThread {
                binding.callStatusText.text = "识别中..."
            }
        }
        realtimeAudioManager.onSpeechRecognized = { text ->
            activity?.runOnUiThread {
                val userMessage = ChatMessage(content = text, isFromUser = true)
                messages.add(userMessage)
                chatAdapter.submitList(messages.toList())
                scrollToBottom()
                binding.callStatusText.text = "AI分析中..."
                chatWithAIForCall(text)
            }
        }
        realtimeAudioManager.onAISpeakingStarted = {
            activity?.runOnUiThread {
                binding.callStatusText.text = "AI回复中..."
                binding.callIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.warning))
            }
        }
        realtimeAudioManager.onAISpeakingFinished = {
            activity?.runOnUiThread {
                if (isInCall) {
                    binding.callStatusText.text = "请说话..."
                    binding.callIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success))
                    binding.callHint.text = "正在收音"
                    binding.callHint.setTextColor(ContextCompat.getColor(requireContext(), R.color.danger))

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (isInCall) {
                            realtimeAudioManager.startRecording()
                        }
                    }, 500)
                }
            }
        }
        realtimeAudioManager.onError = { error ->
            activity?.runOnUiThread {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                if (isInCall) {
                    binding.callStatusText.text = "请说话..."
                }
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            saveHistory()
            parentFragmentManager.popBackStack()
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
            }
        }

        binding.voiceButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        false
                    } else {
                        startVoiceRecording()
                        true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (voiceHelper.isRecording()) {
                        stopVoiceRecording()
                    }
                    true
                }
                else -> false
            }
        }

        binding.callButton.setOnClickListener {
            if (isInCall) {
                endCall()
            } else {
                startCall()
            }
        }

        binding.endCallButton.setOnClickListener {
            endCall()
        }
    }

    private fun startCall() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        isInCall = true
        binding.callStatusContainer.visibility = View.VISIBLE
        binding.callStatusText.text = "请说话..."
        binding.callButton.setIconResource(R.drawable.ic_stop)
        binding.callButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.danger))
        binding.callHint.text = "正在收音"
        binding.callHint.setTextColor(ContextCompat.getColor(requireContext(), R.color.danger))

        if (messages.isEmpty()) {
            addWelcomeMessage()
        }

        realtimeAudioManager.startRecording()
    }

    private fun endCall() {
        isInCall = false
        realtimeAudioManager.stopAISpeaking()
        binding.callStatusContainer.visibility = View.GONE
        resetCallButtonUI()
    }

    private fun resetCallButtonUI() {
        binding.callButton.setIconResource(R.drawable.ic_phone)
        binding.callButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success))
        binding.callHint.text = "AI通话"
        binding.callHint.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint))
    }

    private fun updateCallUI(inCall: Boolean, status: String) {
        binding.callStatusContainer.visibility = if (inCall) View.VISIBLE else View.GONE
        binding.callStatusText.text = status
    }

    private fun loadHistory() {
        val history = chatHistoryManager.getAiDiagnosisHistory()
        if (history.isNotEmpty()) {
            messages.clear()
            messages.addAll(history)
            chatAdapter.submitList(messages.toList())
            scrollToBottom()
        } else {
            addWelcomeMessage()
        }
    }

    private fun saveHistory() {
        chatHistoryManager.saveAiDiagnosisHistory(messages.toList())
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = com.example.yinfuapp.data.model.ChatMessage(
            content = "您好！我是AI医疗助手。根据您描述的症状，我可以提供一些初步的健康咨询建议。请告诉我您有哪些不适症状？",
            isFromUser = false
        )
        messages.add(welcomeMessage)
        chatAdapter.submitList(messages.toList())
        scrollToBottom()
    }

    private fun sendMessage(content: String) {
        val userMessage = com.example.yinfuapp.data.model.ChatMessage(content = content, isFromUser = true)
        messages.add(userMessage)
        chatAdapter.submitList(messages.toList())
        binding.messageInput.text?.clear()
        scrollToBottom()
        saveHistory()

        chatWithAI(content)
    }

    private fun startVoiceRecording() {
        val file = voiceHelper.startRecording()
        if (file != null) {
            binding.voiceButton.setIconResource(R.drawable.ic_stop)
            binding.voiceButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.danger))
            binding.voiceHint.text = "正在录音..."
            binding.voiceHint.setTextColor(ContextCompat.getColor(requireContext(), R.color.danger))
        }
    }

    private fun stopVoiceRecording() {
        val audioFile = voiceHelper.stopRecording()
        resetVoiceButtonUI()

        audioFile?.let { processVoiceInput(it) }
    }

    private fun resetVoiceButtonUI() {
        binding.voiceButton.setIconResource(R.drawable.ic_mic)
        binding.voiceButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
        binding.voiceHint.text = "长按说话"
        binding.voiceHint.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint))
    }

    private fun processVoiceInput(audioFile: File) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = SiliconFlowClient.speechToText(audioFile)

            result.onSuccess { recognizedText ->
                activity?.runOnUiThread {
                    binding.messageInput.setText(recognizedText)
                    sendMessage(recognizedText)
                }
            }.onFailure { error ->
                activity?.runOnUiThread {
                    Toast.makeText(context, "语音识别失败: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playAIResponse(text: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = SiliconFlowClient.textToSpeech(text)
            result.onSuccess { audioFile ->
                activity?.runOnUiThread {
                    audioPlayer.playFile(audioFile)
                }
            }
        }
    }

    private fun scrollToBottom() {
        binding.chatRecyclerView.post {
            if (messages.isNotEmpty()) {
                binding.chatRecyclerView.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    private fun chatWithAI(userMessage: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val requestMessages = mutableListOf<Map<String, Any>>()
                requestMessages.add(mapOf(
                    "role" to "system",
                    "content" to listOf(mapOf("type" to "text", "text" to systemPrompt))
                ))
                messages.filter { !it.isFromUser && it.content != "您好！我是AI医疗助手。根据您描述的症状，我可以提供一些初步的健康咨询建议。请告诉我您有哪些不适症状？" }
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
                    saveHistory()
                    playAIResponse(aiContent)
                }.onFailure { error ->
                    Toast.makeText(context, "AI 回复失败: ${error.message}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun chatWithAIForCall(userMessage: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val requestMessages = mutableListOf<Map<String, Any>>()
                requestMessages.add(mapOf(
                    "role" to "system",
                    "content" to listOf(mapOf("type" to "text", "text" to systemPrompt))
                ))
                messages.filter { !it.isFromUser && it.content != "您好！我是AI医疗助手。根据您描述的症状，我可以提供一些初步的健康咨询建议。请告诉我您有哪些不适症状？" }
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
                    saveHistory()

                    if (isInCall) {
                        realtimeAudioManager.speakAIResponse(aiContent)
                    }
                }.onFailure { error ->
                    if (isInCall) {
                        realtimeAudioManager.onError?.invoke("AI 回复失败: ${error.message}")
                    } else {
                        Toast.makeText(context, "AI 回复失败: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                if (isInCall) {
                    realtimeAudioManager.onError?.invoke("网络错误: ${e.message}")
                } else {
                    Toast.makeText(context, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isInCall) {
            realtimeAudioManager.stopAISpeaking()
        }
        voiceHelper.release()
        audioPlayer.release()
        realtimeAudioManager.release()
        saveHistory()
        _binding = null
    }
}