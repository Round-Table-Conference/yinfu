package com.example.yinfuapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.yinfuapp.R
import com.example.yinfuapp.databinding.ActivityMainBinding
import com.example.yinfuapp.ui.fragment.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()

    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupBottomNavigation()
        setupInitialFragment()
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.statusBarPlaceholder.layoutParams.height = insets.top
            binding.statusBarPlaceholder.requestLayout()

            binding.navigationBarPlaceholder.layoutParams.height = insets.bottom
            binding.navigationBarPlaceholder.requestLayout()

            windowInsets
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchFragment(homeFragment)
                    closeSecondaryPages()
                    true
                }
                R.id.nav_profile -> {
                    switchFragment(profileFragment)
                    closeSecondaryPages()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupInitialFragment() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, profileFragment).hide(profileFragment)
            add(R.id.fragment_container, homeFragment)
        }.commit()
    }

    private fun switchFragment(fragment: Fragment) {
        if (fragment != activeFragment) {
            supportFragmentManager.beginTransaction().apply {
                hide(activeFragment)
                show(fragment)
            }.commit()
            activeFragment = fragment
        }
    }

    private fun closeSecondaryPages() {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun navigateToSos() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SosFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToPayment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PaymentFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToClassroom() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ClassroomFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToAiDiagnosis() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AiDiagnosisFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToAiChat() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AiChatFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToMedicationReminder() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MedicationReminderFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToHealthRecords() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HealthRecordsFragment())
            .addToBackStack(null)
            .commit()
    }
}
