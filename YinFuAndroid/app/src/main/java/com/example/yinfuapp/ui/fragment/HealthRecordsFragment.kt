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
import com.example.yinfuapp.data.api.RetrofitClient
import com.example.yinfuapp.data.model.HealthProfile
import com.example.yinfuapp.data.model.HealthRecord
import com.example.yinfuapp.data.model.MedicalHistory
import com.example.yinfuapp.data.model.MedicalHistoryRequest
import com.example.yinfuapp.data.model.User
import com.example.yinfuapp.data.model.UserProfileRequest
import com.example.yinfuapp.databinding.FragmentHealthRecordsBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch

class HealthRecordsFragment : Fragment() {

    private var _binding: FragmentHealthRecordsBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()

    private var userProfile: User? = null
    private var healthProfile: HealthProfile? = null
    private var medicalHistoryList: List<MedicalHistory> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        loadData()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.editButton.setOnClickListener {
            showEditDialog()
        }
    }

    private fun loadData() {
        loadUserProfile()
        loadHealthRecords()
        loadMedicalHistory()
    }

    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getProfile()
                if (response.code == 200 && response.data != null) {
                    val jsonStr = gson.toJson(response.data)
                    userProfile = gson.fromJson(jsonStr, User::class.java)
                    updateBasicInfoUI()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "加载用户信息失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadHealthRecords() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getHealthRecord()
                if (response.code == 200 && response.data != null) {
                    val jsonStr = gson.toJson(response.data)
                    healthProfile = gson.fromJson(jsonStr, HealthProfile::class.java)
                    updateHealthUI()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "加载健康记录失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMedicalHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMedicalHistory()
                if (response.code == 200 && response.data != null) {
                    val jsonStr = gson.toJson(response.data)
                    val type = object : com.google.gson.reflect.TypeToken<List<MedicalHistory>>() {}.type
                    medicalHistoryList = gson.fromJson(jsonStr, type)
                    updateMedicalHistoryUI()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "加载病史记录失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBasicInfoUI() {
        userProfile?.let { user ->
            binding.nameText.text = user.realName ?: user.username ?: "未设置"
            binding.ageText.text = user.age?.toString()?.plus("岁") ?: "未设置"
            binding.genderText.text = user.gender ?: "未设置"
            binding.bloodTypeText.text = user.bloodType ?: "未设置"
            binding.allergyText.text = user.allergy ?: "无"
        }
    }

    private fun updateHealthUI() {
        healthProfile?.let { profile ->
            binding.bloodPressureText.text = profile.bloodPressure?.value ?: "未测量"
            binding.heartRateText.text = profile.heartRate?.value ?: "未测量"
            binding.bloodSugarText.text = profile.bloodSugar?.value ?: "未测量"
            binding.temperatureText.text = profile.temperature?.value ?: "未测量"
        }
    }

    private fun updateMedicalHistoryUI() {
        if (medicalHistoryList.isEmpty()) {
            binding.medicalHistoryText.text = "暂无病史记录"
        } else {
            val historyText = medicalHistoryList.joinToString("\n") { history ->
                val status = history.currentStatus ?: "未知"
                "${history.diseaseName} - $status"
            }
            binding.medicalHistoryText.text = historyText
        }
    }

    private fun showEditDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val user = userProfile
        val health = healthProfile

        val nameInput = EditText(context).apply {
            hint = "姓名"
            setText(user?.realName ?: "")
        }
        val ageInput = EditText(context).apply {
            hint = "年龄"
            setText(user?.age?.toString() ?: "")
        }
        val genderInput = EditText(context).apply {
            hint = "性别"
            setText(user?.gender ?: "")
        }
        val bloodTypeInput = EditText(context).apply {
            hint = "血型"
            setText(user?.bloodType ?: "")
        }
        val allergyInput = EditText(context).apply {
            hint = "过敏史"
            setText(user?.allergy ?: "")
        }
        val bloodPressureInput = EditText(context).apply {
            hint = "血压（如：120/80）"
            setText(health?.bloodPressure?.value ?: "")
        }
        val heartRateInput = EditText(context).apply {
            hint = "心率"
            setText(health?.heartRate?.value ?: "")
        }
        val bloodSugarInput = EditText(context).apply {
            hint = "血糖"
            setText(health?.bloodSugar?.value ?: "")
        }
        val temperatureInput = EditText(context).apply {
            hint = "体温"
            setText(health?.temperature?.value ?: "")
        }

        layout.addView(nameInput)
        layout.addView(ageInput)
        layout.addView(genderInput)
        layout.addView(bloodTypeInput)
        layout.addView(allergyInput)
        layout.addView(bloodPressureInput)
        layout.addView(heartRateInput)
        layout.addView(bloodSugarInput)
        layout.addView(temperatureInput)

        AlertDialog.Builder(context)
            .setTitle("编辑健康档案")
            .setView(layout)
            .setPositiveButton("保存") { _, _ ->
                saveProfile(
                    name = nameInput.text.toString(),
                    age = ageInput.text.toString(),
                    gender = genderInput.text.toString(),
                    bloodType = bloodTypeInput.text.toString(),
                    allergy = allergyInput.text.toString(),
                    bloodPressure = bloodPressureInput.text.toString(),
                    heartRate = heartRateInput.text.toString(),
                    bloodSugar = bloodSugarInput.text.toString(),
                    temperature = temperatureInput.text.toString()
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun saveProfile(
        name: String,
        age: String,
        gender: String,
        bloodType: String,
        allergy: String,
        bloodPressure: String,
        heartRate: String,
        bloodSugar: String,
        temperature: String
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profileRequest = UserProfileRequest(
                    realName = name.ifEmpty { null },
                    age = age.toIntOrNull(),
                    gender = gender.ifEmpty { null },
                    bloodType = bloodType.ifEmpty { null },
                    allergy = allergy.ifEmpty { null }
                )
                val profileResponse = RetrofitClient.apiService.updateProfile(profileRequest)
                if (profileResponse.code != 200) {
                    Toast.makeText(context, "保存失败: ${profileResponse.message}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (bloodPressure.isNotEmpty()) {
                    saveHealthRecord("blood_pressure", bloodPressure, "mmHg")
                }
                if (heartRate.isNotEmpty()) {
                    saveHealthRecord("heart_rate", heartRate, "次/分")
                }
                if (bloodSugar.isNotEmpty()) {
                    saveHealthRecord("blood_sugar", bloodSugar, "mmol/L")
                }
                if (temperature.isNotEmpty()) {
                    saveHealthRecord("temperature", temperature, "°C")
                }

                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                loadData()

            } catch (e: Exception) {
                Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveHealthRecord(type: String, value: String, unit: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = com.example.yinfuapp.data.model.HealthRecordRequest(
                    recordType = type,
                    value = value,
                    unit = unit
                )
                RetrofitClient.apiService.addHealthRecord(request)
            } catch (e: Exception) {
                // silent fail
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}