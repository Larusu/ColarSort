package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.colarsort.app.R
import com.colarsort.app.data.repository.RepositoryProvider
import com.colarsort.app.databinding.ActivityLoginBinding
import com.colarsort.app.utils.UtilityHelper.showCustomToast
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contentContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.showPasswordIcon.setOnClickListener { togglePasswordVisibility() }

        binding.loginButton.setOnClickListener {
            handleLogin(
                binding.usernameField.text.toString().trim(),
                binding.passwordField.text.toString().trim()
            )
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        val font = binding.passwordField.typeface

        binding.passwordField.inputType = if (isPasswordVisible) {
            binding.showPasswordIcon.setImageResource(R.drawable.show_password)
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.showPasswordIcon.setImageResource(R.drawable.hide_password)
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        binding.passwordField.typeface = font
        binding.passwordField.setSelection(binding.passwordField.length())
    }

    private fun handleLogin(username: String, password: String) {
        when {
            username.isEmpty() && password.isEmpty()-> {
                showCustomToast(this, "Please enter your username and password")
                return
            }

            username.isEmpty() -> {
                showCustomToast(this, "Please enter your username")
                return
            }

            password.isEmpty() -> {
                showCustomToast(this, "Please enter your password")
                return
            }
        }

        lifecycleScope.launch {
            val userRepo = RepositoryProvider.usersRepo
            val result = userRepo.getIdAndRoleIfExists(username.lowercase(), password)

            if (result != null) {
                sessionManager.saveUser(result.id, result.role)
                showCustomToast(this@LoginActivity, "Logged in successfully")
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                showCustomToast(this@LoginActivity, "User not found")
            }
        }
    }
}
