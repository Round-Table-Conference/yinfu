package com.example.yinfuapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.yinfuapp.data.api.RetrofitClient
import com.example.yinfuapp.data.model.RegisterRequest
import com.example.yinfuapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupListeners()
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val phone = binding.phoneInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            if (validateInput(username, phone, password, confirmPassword)) {
                performRegister(username, phone, password)
            }
        }

        binding.loginLink.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(
        username: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (username.isEmpty()) {
            binding.usernameLayout.error = "请输入用户名"
            return false
        }
        binding.usernameLayout.error = null

        if (phone.isEmpty() || phone.length != 11) {
            binding.phoneLayout.error = "请输入正确的手机号"
            return false
        }
        binding.phoneLayout.error = null

        if (password.isEmpty() || password.length < 6) {
            binding.passwordLayout.error = "密码至少6位"
            return false
        }
        binding.passwordLayout.error = null

        if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = "两次密码不一致"
            return false
        }
        binding.confirmPasswordLayout.error = null

        return true
    }

    private fun performRegister(username: String, phone: String, password: String) {
        binding.registerButton.isEnabled = false
        binding.registerButton.text = "注册中..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(username, password, phone)
                )
                if (response.code == 200) {
                    Toast.makeText(this@RegisterActivity, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.registerButton.isEnabled = true
                binding.registerButton.text = "注册"
            }
        }
    }
}