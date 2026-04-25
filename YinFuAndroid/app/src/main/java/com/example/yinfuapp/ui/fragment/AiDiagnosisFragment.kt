package com.example.yinfuapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        val welcomeMessage = ChatMessage(
            content = "您好！我是AI医疗助手。根据您描述的症状，我可以提供一些初步的健康咨询建议。请告诉我您有哪些不适症状？",
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
                val requestMessages = mutableListOf<Message>()
                requestMessages.add(Message(role = "system", content = systemPrompt))
                messages.filter { !it.isFromUser && it.content != "您好！我是AI医疗助手。根据您描述的症状，我可以提供一些初步的健康咨询建议。请告诉我您有哪些不适症状？" }
                    .forEach { requestMessages.add(Message(role = "assistant", content = it.content)) }
                requestMessages.add(Message(role = "user", content = userMessage))

                val request = com.example.yinfuapp.data.model.AiChatRequest(messages = requestMessages)
                val response = RetrofitClient.apiService.aiDiagnosis(
                    com.example.yinfuapp.data.model.AiDiagnosisRequest(symptom = userMessage)
                )

                val aiContent = if (response.data != null) {
                    val dataMap = response.data as? Map<*, *>
                    dataMap?.get("content") as? String ?: "抱歉，我现在无法回答您的问题，请稍后再试。"
                } else {
                    response.message
                }

                val aiMessage = ChatMessage(
                    content = aiContent,
                    isFromUser = false
                )
                messages.add(aiMessage)
                chatAdapter.submitList(messages.toList())
                scrollToBottom()
                saveHistory()

            } catch (e: Exception) {
                Toast.makeText(context, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
                val errorMessage = ChatMessage(
                    content = "抱歉，发生了网络错误。请检查您的网络连接后重试。",
                    isFromUser = false
                )
                messages.add(errorMessage)
                chatAdapter.submitList(messages.toList())
                scrollToBottom()
                saveHistory()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveHistory()
        _binding = null
    }
}