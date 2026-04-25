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
import com.example.yinfuapp.data.api.RetrofitClient
import com.example.yinfuapp.data.local.ChatHistoryManager
import com.example.yinfuapp.data.model.ChatMessage
import com.example.yinfuapp.data.model.Message
import com.example.yinfuapp.databinding.FragmentAiChatBinding
import kotlinx.coroutines.launch

class AiChatFragment : Fragment() {

    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var chatHistoryManager: ChatHistoryManager

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
                messages.filter { !it.isFromUser && it.content != "您好！我在这里陪伴您。有什么想聊的，或者有什么心事想倾诉吗？我会用心倾听并陪伴您。" }
                    .forEach { requestMessages.add(Message(role = "assistant", content = it.content)) }
                requestMessages.add(Message(role = "user", content = userMessage))

                val request = com.example.yinfuapp.data.model.AiChatRequest(messages = requestMessages)
                val response = RetrofitClient.apiService.aiChat(request)

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