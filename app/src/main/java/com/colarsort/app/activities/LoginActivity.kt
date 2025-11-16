package com.colarsort.app.activities

import android.os.Bundle
import android.text.InputType
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colarsort.app.R
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var binding: ActivityLoginBinding
    var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contentContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.showPasswordIcon.setOnClickListener { togglePasswordVisibility() }

    }
    fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        val font = binding.passwordField.typeface

        if (isPasswordVisible) {
            binding.passwordField.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.showPasswordIcon.setImageResource(R.drawable.show_password)
        } else {
            binding.passwordField.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.showPasswordIcon.setImageResource(R.drawable.hide_password)
        }

        binding.passwordField.typeface = font
        binding.passwordField.setSelection(binding.passwordField.length())
    }
}

