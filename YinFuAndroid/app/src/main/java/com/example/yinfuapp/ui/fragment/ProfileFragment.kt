package com.example.yinfuapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yinfuapp.R
import com.example.yinfuapp.data.SessionManager
import com.example.yinfuapp.databinding.FragmentProfileBinding
import com.example.yinfuapp.ui.LoginActivity
import com.example.yinfuapp.ui.MainActivity
import com.example.yinfuapp.ui.activity.AiCallActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserInfo()
        setupClickListeners()
    }

    private fun loadUserInfo() {
        val realName = SessionManager.getRealName()
        val phone = SessionManager.getPhone()
        binding.userName.text = realName ?: "用户"
        binding.userPhone.text = phone ?: ""
    }

    private fun setupClickListeners() {
        binding.paymentManagement.setOnClickListener {
            (activity as? MainActivity)?.navigateToPayment()
        }

        binding.healthRecords.setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.supportFragmentManager.beginTransaction()
                    .replace(com.example.yinfuapp.R.id.fragment_container, HealthRecordsFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.aiCall.setOnClickListener {
            AiCallActivity.start(requireContext(), AiCallActivity.CALL_TYPE_CHAT)
        }

        binding.logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        SessionManager.clearSession()
        Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}