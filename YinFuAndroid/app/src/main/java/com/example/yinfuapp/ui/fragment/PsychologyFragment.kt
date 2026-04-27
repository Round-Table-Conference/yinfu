package com.example.yinfuapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yinfuapp.databinding.FragmentPsychologyBinding
import com.example.yinfuapp.ui.MainActivity
import com.example.yinfuapp.ui.activity.AiCallActivity

class PsychologyFragment : Fragment() {

    private var _binding: FragmentPsychologyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPsychologyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.aiChatCard.setOnClickListener {
            (activity as? MainActivity)?.navigateToAiChat()
        }

        binding.counselorCard.setOnClickListener {
            AiCallActivity.start(requireContext(), AiCallActivity.CALL_TYPE_CHAT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
