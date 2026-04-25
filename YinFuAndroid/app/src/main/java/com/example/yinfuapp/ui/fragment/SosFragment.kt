package com.example.yinfuapp.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.yinfuapp.R
import com.example.yinfuapp.data.api.RetrofitClient
import com.example.yinfuapp.data.model.EmergencyContact
import com.example.yinfuapp.data.model.EmergencyContactRequest
import com.example.yinfuapp.databinding.FragmentSosBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class SosFragment : Fragment() {

    private var _binding: FragmentSosBinding? = null
    private val binding get() = _binding!!

    private val contactList = mutableListOf<EmergencyContact>()
    private val gson = Gson()

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
        _binding = FragmentSosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSosButton()
        setupAddContact()
        loadContacts()
    }

    private fun setupSosButton() {
        binding.sosButton.setOnClickListener {
            longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_DELAY)
        }

        binding.sosButton.setOnLongClickListener {
            longPressHandler.removeCallbacks(longPressRunnable)
            if (!isLongPressing) {
                isLongPressing = true
                makeEmergencyCall()
            }
            true
        }

        binding.sosButton.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    longPressHandler.removeCallbacks(longPressRunnable)
                    isLongPressing = false
                }
            }
            false
        }
    }

    private fun setupAddContact() {
        binding.addContactButton.setOnClickListener {
            showAddContactDialog()
        }
    }

    private fun loadContacts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getEmergencyContacts()
                if (response.code == 200 && response.data != null) {
                    val jsonStr = gson.toJson(response.data)
                    val type = object : TypeToken<List<EmergencyContact>>() {}.type
                    val list: List<EmergencyContact> = gson.fromJson(jsonStr, type)
                    contactList.clear()
                    contactList.addAll(list)
                    updateContactsUI()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "加载联系人失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateContactsUI() {
        binding.contactsContainer.removeAllViews()

        if (contactList.isEmpty()) {
            binding.emptyContactsView.visibility = View.VISIBLE
        } else {
            binding.emptyContactsView.visibility = View.GONE

            contactList.forEachIndexed { index, contact ->
                val contactView = createContactView(contact)
                binding.contactsContainer.addView(contactView)

                if (index < contactList.size - 1) {
                    val divider = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                        )
                        setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.border))
                    }
                    binding.contactsContainer.addView(divider)
                }
            }
        }
    }

    private fun createContactView(contact: EmergencyContact): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, resources.getDimensionPixelSize(R.dimen.spacing_md), 0, resources.getDimensionPixelSize(R.dimen.spacing_md))
        }

        val infoLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val nameText = TextView(requireContext()).apply {
            text = "${contact.relationship ?: "联系人"} - ${contact.name}"
            textSize = resources.getDimension(R.dimen.font_size_md) / resources.displayMetrics.scaledDensity
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        }

        val phoneText = TextView(requireContext()).apply {
            text = contact.phone
            textSize = resources.getDimension(R.dimen.font_size_sm) / resources.displayMetrics.scaledDensity
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
        }

        infoLayout.addView(nameText)
        infoLayout.addView(phoneText)
        layout.addView(infoLayout)

        val deleteBtn = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.delete)
            background = null
            setOnClickListener { showDeleteConfirmation(contact) }
        }
        layout.addView(deleteBtn)

        layout.setOnClickListener { callContact(contact.phone) }

        return layout
    }

    private fun showAddContactDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val nameInput = EditText(context).apply { hint = "姓名" }
        val phoneInput = EditText(context).apply { hint = "电话" }
        val relationshipInput = EditText(context).apply { hint = "关系（如：子女、配偶）" }

        layout.addView(nameInput)
        layout.addView(phoneInput)
        layout.addView(relationshipInput)

        AlertDialog.Builder(context)
            .setTitle("添加紧急联系人")
            .setView(layout)
            .setPositiveButton("添加") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                val relationship = relationshipInput.text.toString().trim()

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    addContact(name, phone, relationship)
                } else {
                    Toast.makeText(context, "请填写姓名和电话", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun addContact(name: String, phone: String, relationship: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = EmergencyContactRequest(
                    name = name,
                    phone = phone,
                    relationship = relationship.ifEmpty { null },
                    isPrimary = if (contactList.isEmpty()) 1 else 0
                )
                val response = RetrofitClient.apiService.addEmergencyContact(request)
                if (response.code == 200) {
                    Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show()
                    loadContacts()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "添加失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmation(contact: EmergencyContact) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除联系人")
            .setMessage("确定要删除联系人 ${contact.name} 吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteContact(contact.id)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteContact(id: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteEmergencyContact(id)
                if (response.code == 200) {
                    Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                    loadContacts()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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

    private fun callContact(phone: String) {
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
            intent.data = Uri.parse("tel:$phone")
            startActivity(intent)
        }
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

    override fun onResume() {
        super.onResume()
        loadContacts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        longPressHandler.removeCallbacks(longPressRunnable)
        _binding = null
    }
}