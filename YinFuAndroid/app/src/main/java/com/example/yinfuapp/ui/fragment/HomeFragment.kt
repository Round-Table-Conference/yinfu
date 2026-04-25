package com.example.yinfuapp.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.yinfuapp.databinding.FragmentHomeBinding
import com.example.yinfuapp.ui.MainActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isLongPressing = false
    private val longPressHandler = Handler(Looper.getMainLooper())
    private val longPressRunnable = Runnable {
        isLongPressing = true
        makeEmergencyCall()
    }

    companion object {
        private const val LONG_PRESS_DELAY = 3000L
        private const val CALL_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.emergencyCard.setOnClickListener {
            longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_DELAY)
        }

        binding.emergencyCard.setOnLongClickListener {
            longPressHandler.removeCallbacks(longPressRunnable)
            if (!isLongPressing) {
                isLongPressing = true
                makeEmergencyCall()
            }
            true
        }

        binding.emergencyCard.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_UP -> {
                    val hadLongPressed = isLongPressing
                    longPressHandler.removeCallbacks(longPressRunnable)
                    isLongPressing = false
                    if (!hadLongPressed) {
                        (activity as? MainActivity)?.navigateToSos()
                    }
                }
                android.view.MotionEvent.ACTION_CANCEL -> {
                    longPressHandler.removeCallbacks(longPressRunnable)
                    isLongPressing = false
                }
            }
            false
        }

        binding.healthCard.setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.supportFragmentManager.beginTransaction()
                    .replace(com.example.yinfuapp.R.id.fragment_container, HealthFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.psychologyCard.setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.supportFragmentManager.beginTransaction()
                    .replace(com.example.yinfuapp.R.id.fragment_container, PsychologyFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.paymentCard.setOnClickListener {
            (activity as? MainActivity)?.navigateToPayment()
        }

        binding.classroomCard.setOnClickListener {
            (activity as? MainActivity)?.navigateToClassroom()
        }
    }

    private fun makeEmergencyCall() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CALL_PHONE),
                CALL_PERMISSION_REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:120")
            startActivity(intent)
        }
        isLongPressing = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "已获得通话权限", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "需要通话权限才能使用紧急呼叫", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        longPressHandler.removeCallbacks(longPressRunnable)
        _binding = null
    }
}
