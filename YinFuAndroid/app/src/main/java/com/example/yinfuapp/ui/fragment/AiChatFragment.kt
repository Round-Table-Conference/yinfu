package com.example.yinfuapp.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.yinfuapp.databinding.FragmentAiChatBinding
import com.example.yinfuapp.utils.AudioPlayerHelper
import com.example.yinfuapp.utils.VoiceHelper
import kotlinx.coroutines.launch
import java.io.File

class AiChatFragment : Fragment() {

    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
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

    private val systemPrompt = """你是温暖关怀的心理陪伴助手。简洁回复，倾听支持。
原则：
1. 倾听理解，不评判
2. 回答简洁，不超过150字
3. 给予积极正向的支持
4. 保持耐心友善"""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiChatBinding.inflate(inflater, container, false)
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
    }

    private fun loadHistory() {
        val history = chatHistoryManager.getAiChatHistory()
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
        chatHistoryManager.saveAiChatHistory(messages.toList())
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            content = "您好！我在这里陪伴您。有什么想聊的，或者有什么心事想倾诉吗？我会用心倾听并陪伴您。",
            isFromUser = false
        )
        messages.add(welcomeMessage)
        chatAdapter.submitList(messages.toList())
        scrollToBottom()
    }

    private fun sendMessage(content: String) {
        val userMessage = ChatMessage(content = content, isFromUser = true)
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
                messages.filter { !it.isFromUser && it.content != "您好！我在这里陪伴您。有什么想聊的，或者有什么心事想倾诉吗？我会用心倾听并陪伴您。" }
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

    override fun onDestroyView() {
        super.onDestroyView()
        voiceHelper.release()
        audioPlayer.release()
        saveHistory()
        _binding = null
    }
}