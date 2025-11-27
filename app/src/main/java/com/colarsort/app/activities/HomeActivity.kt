package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colarsort.app.R
import com.colarsort.app.databinding.ActivityHomeBinding
import com.colarsort.app.utils.UtilityHelper.showCustomToast

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set up view binding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigation click listeners
        binding.ivHome.setOnClickListener {
            showCustomToast(this, "You are already in Home")
        }
        binding.ivStatus.setOnClickListener {
            val intent = Intent(this, ProductionStatusActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivOrders.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivProducts.setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
            finish()
        }
        binding.ivMaterials.setOnClickListener {
            val intent = Intent(this, MaterialsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}