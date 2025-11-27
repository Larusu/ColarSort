package com.colarsort.app.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colarsort.app.R
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityHomeBinding
import com.colarsort.app.repository.OrdersRepo
import com.colarsort.app.utils.UtilityHelper.showCustomToast
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var orderRepo: OrdersRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dbHelper = DatabaseHelper(this)
        orderRepo = OrdersRepo(dbHelper)

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
        showChart()
    }

    fun showChart()
    {
        val orders = orderRepo.getAll()
        var completed = 0
        var inProgress = 0
        var pending = 0
        for(order in orders)
        {
            when(order.status)
            {
                "Completed" -> completed += 1
                "In Progress" -> inProgress += 1
                "Pending" -> pending += 1
            }
        }

        val chart = findViewById<PieChart>(R.id.donutChart)
        if(completed + inProgress + pending == 0)
        {
            chart.visibility = View.GONE
            binding.chartAvailability.visibility = View.VISIBLE
            return
        }
        binding.chartAvailability.visibility = View.GONE

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        if (completed > 0) {
            entries.add(PieEntry(completed.toFloat(), "Completed"))
            colors.add(Color.GREEN)
        }

        if (inProgress > 0) {
            entries.add(PieEntry(inProgress.toFloat(), "In Progress"))
            colors.add(Color.YELLOW)
        }

        if (pending > 0) {
            entries.add(PieEntry(pending.toFloat(), "Pending"))
            colors.add(Color.RED)
        }

        // Dataset
        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawValues(true)
        dataSet.sliceSpace = 2f
        dataSet.colors = colors

        // Data
        val data = PieData(dataSet)
        data.setValueTextSize(14f)           // values size
        data.setValueTextColor(Color.BLACK)  // values color

        // Apply data
        chart.data = data

        // Style
        chart.setUsePercentValues(true)
        chart.isDrawHoleEnabled = true
        chart.holeRadius = 60f
        chart.transparentCircleRadius = 65f
        chart.setCenterText("Orders")
        chart.description.isEnabled = false

        // Labels on slices
        chart.setEntryLabelColor(Color.BLACK)   // label color
        chart.setEntryLabelTextSize(12f)      // label size

        // Legend (optional)
        val legend = chart.legend
        chart.legend.isEnabled = true
        chart.legend.textColor = Color.BLUE
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER)

        // Refresh
        chart.invalidate()
    }
}