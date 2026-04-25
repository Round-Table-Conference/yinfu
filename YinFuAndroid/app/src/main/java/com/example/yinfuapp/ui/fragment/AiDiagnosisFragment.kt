package com.example.yinfuapp.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yinfuapp.data.adapter.ChatAdapter
import com.example.yinfuapp.data.api.SiliconFlowClient
import com.example.yinfuapp.data.local.ChatHistoryManager
import com.example.yinfuapp.databinding.FragmentAiDiagnosisBinding
import com.example.yinfuapp.utils.AudioPlayerHelper
import com.example.yinfuapp.utils.VoiceHelper
import kotlinx.coroutines.launch
import java.io.File

class AiDiagnosisFragment : Fragment() {

    private var _binding: FragmentAiDiagnosisBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<com.example.yinfuapp.data.model.ChatMessage>()
    private lateinit var chatHistoryManager: ChatHistoryManager
    private lateinit var voiceHelper: VoiceHelper
    private lateinit var audioPlayer: AudioPlayerHelper

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
        setupRecyclerView()
        setupClickListeners()
        loadHistory()
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

        binding.voiceButton.setOnClickListener {
            if (voiceHelper.isRecording()) {
                stopVoiceRecording()
            } else {
                startVoiceRecording()
            }
        }
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        
        val file = voiceHelper.startRecording()
        if (file != null) {
            binding.voiceButton.setIconResource(com.example.yinfuapp.R.drawable.ic_stop)
            Toast.makeText(context, "开始录音", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopVoiceRecording() {
        val audioFile = voiceHelper.stopRecording()
        binding.voiceButton.setIconResource(com.example.yinfuapp.R.drawable.ic_mic)

        audioFile?.let { processVoiceInput(it) }
    }

    private fun processVoiceInput(audioFile: File) {
        viewLifecycleOwner.lifecycleScope.launch {
            val historyMessages = messages
                .filter { !it.isFromUser && it.content != "您好！我是AI医疗助手。根据您描述的症状，我可以提供一些初步的健康咨询建议。请告诉我您有哪些不适症状？" }
                .map { "assistant" to it.content }

            val result = SiliconFlowClient.chatWithAudio(
                audioFile = audioFile,
                systemPrompt = systemPrompt,
                historyMessages = historyMessages
            )

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
                    val aiMessage = com.example.yinfuapp.data.model.ChatMessage(
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

    override fun onDestroyView() {
        super.onDestroyView()
        voiceHelper.release()
        audioPlayer.release()
        saveHistory()
        _binding = null
    }
}