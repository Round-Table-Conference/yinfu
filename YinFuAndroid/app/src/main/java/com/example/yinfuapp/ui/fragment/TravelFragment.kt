package com.example.yinfuapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yinfuapp.databinding.FragmentTravelBinding

class TravelFragment : Fragment() {

    private var _binding: FragmentTravelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTravelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.busCard.setOnClickListener {
            Toast.makeText(context, "公交查询", Toast.LENGTH_SHORT).show()
        }

        binding.subwayCard.setOnClickListener {
            Toast.makeText(context, "地铁查询", Toast.LENGTH_SHORT).show()
        }

        binding.taxiCard.setOnClickListener {
            Toast.makeText(context, "打车服务", Toast.LENGTH_SHORT).show()
        }

        binding.locationGuardCard.setOnClickListener {
            Toast.makeText(context, "行程守护", Toast.LENGTH_SHORT).show()
        }

        binding.searchButton.setOnClickListener {
            val from = binding.fromInput.text.toString()
            val to = binding.toInput.text.toString()
            if (from.isNotEmpty() && to.isNotEmpty()) {
                Toast.makeText(context, "正在搜索: $from 到 $to", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "请输入起点和终点", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
