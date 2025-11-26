package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colarsort.app.R
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityProductionStatusBinding
import com.colarsort.app.utils.UtilityHelper.showCustomToast

class ProductionStatusActivity : BaseActivity() {

    private lateinit var binding: ActivityProductionStatusBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database and repository
        dbHelper = DatabaseHelper(this)

        // Set up view binding
        binding = ActivityProductionStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigation click listeners
        binding.ivHome.setOnClickListener { /* TODO: open home activity */ }
        binding.ivStatus.setOnClickListener {
            showCustomToast(this, "You are already in Production Status")
        }
        binding.ivOrders.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivProducts.setOnClickListener {
            showCustomToast(this, "You are already in Products")
        }
        binding.ivMaterials.setOnClickListener {
            val intent = Intent(this, MaterialsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}