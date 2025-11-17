package com.colarsort.app.activities

import android.content.ContentValues
import android.os.Bundle
import android.provider.SyncStateContract.Helpers.insert
import android.text.InputType
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colarsort.app.R
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.UserTable
import com.colarsort.app.databinding.ActivityLoginBinding
import com.colarsort.app.models.Users
import com.colarsort.app.repository.UsersRepo

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var binding: ActivityLoginBinding
    var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contentContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val user = listOf(
//            Users(null, "admin", "Admin", "admin"),
//            Users(null, "user", "User", "user")
//        )
//
//        user.forEach {
//                users -> insert(users)
//        }

        binding.showPasswordIcon.setOnClickListener { togglePasswordVisibility() }

        binding.loginButton.setOnClickListener {
            handleLogin(binding.usernameField.text.toString(), binding.passwordField.text.toString())
        }
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

    fun handleLogin(username: String, password: String)
    {
        if(username.isEmpty() || password.isEmpty())
        {
            Toast.makeText(this, "Enter all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val repo = UsersRepo(this)
        val userExists = repo.validateCredentials(username, password)
        if(userExists)
        {
            Toast.makeText(this, "Welcome to login", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "You're not welcome!!", Toast.LENGTH_SHORT).show()
    }

//    fun insert(user : Users)
//    {
//        val db = dbHelper.writableDatabase
//        val values = ContentValues().apply {
//            put(UserTable.USERNAME, user.username)
//            put(UserTable.ROLE, user.role)
//            put(UserTable.PASSWORD, user.password)
//        }
//        db.insert(UserTable.TABLE_NAME, null, values)
//        db.close()
//    }

}

