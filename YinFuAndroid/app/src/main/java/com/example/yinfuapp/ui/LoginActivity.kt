package com.example.yinfuapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.yinfuapp.data.SessionManager
import com.example.yinfuapp.data.api.RetrofitClient
import com.example.yinfuapp.data.model.LoginRequest
import com.example.yinfuapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SessionManager.init(this)

        if (SessionManager.isLoggedIn()) {
            RetrofitClient.setAuthInterceptor(SessionManager.getToken())
            navigateToMain()
            return
        }

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
        binding.loginButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (validateInput(username, password)) {
                performLogin(username, password)
            }
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.forgotPassword.setOnClickListener {
            Toast.makeText(this, "请联系客服找回密码", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.usernameLayout.error = "请输入用户名"
            return false
        }
        binding.usernameLayout.error = null

        if (password.isEmpty()) {
            binding.passwordLayout.error = "请输入密码"
            return false
        }
        binding.passwordLayout.error = null

        return true
    }

    private fun performLogin(username: String, password: String) {
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "登录中..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                if (response.code == 200) {
                    response.data?.let { loginData ->
                        SessionManager.saveToken(loginData.token)
                        SessionManager.saveUserId(loginData.user.id)
                        loginData.user.realName?.let { SessionManager.saveRealName(it) }
                            ?: SessionManager.saveRealName(loginData.user.username)
                        loginData.user.phone?.let { SessionManager.savePhone(it) }
                        RetrofitClient.setAuthInterceptor(loginData.token)
                    }
                    Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    Toast.makeText(this@LoginActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.loginButton.isEnabled = true
                binding.loginButton.text = "登录"
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}