package com.colarsort.app.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colarsort.app.R
import com.colarsort.app.databinding.ActivityHomeBinding
import com.colarsort.app.utils.UtilityHelper.showCustomToast
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

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

        val chart = findViewById<PieChart>(R.id.donutChart)
        val entries = listOf(
            PieEntry(40f, "Completed"),
            PieEntry(30f, "In Progress"),
            PieEntry(30f, "Pending")
        )
        // 2. Create dataset
        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawValues(true)          // show percentages
        dataSet.sliceSpace = 2f              // space between slices
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val data = PieData(dataSet)
        data.setValueTextSize(14f)
        data.setValueTextColor(Color.WHITE)

        // 4. Apply to chart
        chart.data = data

        // 5. Style
        chart.setUsePercentValues(true)
        chart.isDrawHoleEnabled = true
        chart.holeRadius = 60f
        chart.transparentCircleRadius = 65f
        chart.setCenterText("Status")
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        // 6. Refresh
        chart.invalidate()
    }
}