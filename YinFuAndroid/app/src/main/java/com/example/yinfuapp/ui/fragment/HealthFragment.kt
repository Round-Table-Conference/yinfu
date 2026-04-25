package com.example.yinfuapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yinfuapp.databinding.FragmentHealthBinding
import com.example.yinfuapp.ui.MainActivity

class HealthFragment : Fragment() {

    private var _binding: FragmentHealthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.medicationCard.setOnClickListener {
            (activity as? MainActivity)?.navigateToMedicationReminder()
        }

        binding.aiDiagnosisCard.setOnClickListener {
            (activity as? MainActivity)?.navigateToAiDiagnosis()
        }

        binding.healthRecordsCard.setOnClickListener {
            (activity as? MainActivity)?.navigateToHealthRecords()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
