package com.colarsort.app.activities

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductAdapter
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.UserTable
import com.colarsort.app.databinding.ActivityLoginBinding
import com.colarsort.app.models.Products
import com.colarsort.app.models.Users
import com.colarsort.app.repository.ProductsRepo
import com.colarsort.app.repository.UsersRepo
import com.colarsort.app.utils.UtilityHelper.showCustomToast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dbHelper = DatabaseHelper(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contentContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set up on click listeners
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

        val userRepo = UsersRepo(dbHelper)
        val userExists = userRepo.validateCredentials(username, password)

        if (userExists) {
            showCustomToast(this, "Logged in successfully")
            val intent = Intent(this, ProductsActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            showCustomToast(this, "User not found")
        }
    }
}
