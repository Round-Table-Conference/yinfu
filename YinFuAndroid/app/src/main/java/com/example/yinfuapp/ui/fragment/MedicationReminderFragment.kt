package com.example.yinfuapp.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yinfuapp.data.adapter.MedicationAdapter
import com.example.yinfuapp.data.api.RetrofitClient
import com.example.yinfuapp.data.model.MedicationReminder
import com.example.yinfuapp.data.model.MedicationReminderRequest
import com.example.yinfuapp.databinding.FragmentMedicationReminderBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class MedicationReminderFragment : Fragment() {

    private var _binding: FragmentMedicationReminderBinding? = null
    private val binding get() = _binding!!

    private lateinit var medicationAdapter: MedicationAdapter
    private val medicationList = mutableListOf<MedicationReminder>()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicationReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        loadMedications()
    }

    private fun setupRecyclerView() {
        medicationAdapter = MedicationAdapter { medication, isTaken ->
            if (isTaken) {
                recordMedicationTaken(medication.id, true)
            } else {
                recordMedicationTaken(medication.id, false)
            }
        }
        binding.medicationRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = medicationAdapter
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.addMedicationButton.setOnClickListener {
            showAddMedicationDialog()
        }
    }

    private fun loadMedications() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMedications()
                if (response.code == 200 && response.data != null) {
                    val jsonStr = gson.toJson(response.data)
                    val type = object : TypeToken<List<MedicationReminder>>() {}.type
                    val list: List<MedicationReminder> = gson.fromJson(jsonStr, type)
                    medicationList.clear()
                    medicationList.addAll(list)
                    updateUI()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun recordMedicationTaken(id: Long, isTaken: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (isTaken) {
                    RetrofitClient.apiService.takeMedication(id)
                    Toast.makeText(context, "已记录服药", Toast.LENGTH_SHORT).show()
                } else {
                    RetrofitClient.apiService.untakeMedication(id)
                    Toast.makeText(context, "已取消服药记录", Toast.LENGTH_SHORT).show()
                }
                loadMedications()
            } catch (e: Exception) {
                Toast.makeText(context, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI() {
        if (medicationList.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.medicationRecyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.medicationRecyclerView.visibility = View.VISIBLE
            medicationAdapter.submitList(medicationList.toList())
        }
    }

    private fun showAddMedicationDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val nameInput = EditText(context).apply { hint = "药物名称" }
        val dosageInput = EditText(context).apply { hint = "剂量（如：1片）" }
        val timeInput = EditText(context).apply { hint = "时间（如：08:00）" }
        val frequencyInput = EditText(context).apply { hint = "频率（如：每天）" }
        val remarkInput = EditText(context).apply { hint = "备注" }

        layout.addView(nameInput)
        layout.addView(dosageInput)
        layout.addView(timeInput)
        layout.addView(frequencyInput)
        layout.addView(remarkInput)

        AlertDialog.Builder(context)
            .setTitle("添加用药提醒")
            .setView(layout)
            .setPositiveButton("添加") { _, _ ->
                val name = nameInput.text.toString().trim()
                val dosage = dosageInput.text.toString().trim()
                val time = timeInput.text.toString().trim()
                val frequency = frequencyInput.text.toString().trim()
                val remark = remarkInput.text.toString().trim()

                if (name.isNotEmpty() && dosage.isNotEmpty() && time.isNotEmpty() && frequency.isNotEmpty()) {
                    addMedication(name, dosage, time, frequency, remark)
                } else {
                    Toast.makeText(context, "请填写必填项", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun addMedication(name: String, dosage: String, time: String, frequency: String, remark: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = MedicationReminderRequest(
                    name = name,
                    dosage = dosage,
                    time = time,
                    frequency = frequency,
                    remark = remark.ifEmpty { null }
                )
                val response = RetrofitClient.apiService.addMedication(request)
                if (response.code == 200) {
                    Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show()
                    loadMedications()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "添加失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}