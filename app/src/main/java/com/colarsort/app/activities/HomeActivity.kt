package com.colarsort.app.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colarsort.app.R
import com.colarsort.app.databinding.ActivityHomeBinding
import com.colarsort.app.repository.OrdersRepo
import com.colarsort.app.repository.ProductionStatusRepo
import com.colarsort.app.repository.ProductsRepo
import com.colarsort.app.utils.UtilityHelper.showCustomToast
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var orderRepo: OrdersRepo
    private lateinit var productsRepo: ProductsRepo
    private lateinit var productionStatusRepo: ProductionStatusRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        orderRepo = OrdersRepo(dbHelper)
        productsRepo = ProductsRepo(dbHelper)
        productionStatusRepo = ProductionStatusRepo(dbHelper)

        // Set up view binding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get total products and orders
        val totalProducts = productsRepo.getAll().size
        val totalOrders = orderRepo.getAll().size

        // Set total products and orders
        binding.tvTotalProducts.text = totalProducts.toString()
        binding.tvTotalOrders.text = totalOrders.toString()

        // Set Progress Bar and Pie chart
        val progressValue = calculateProductionProgress()
        binding.progressBar.progress = progressValue
        val progressValueStr = "$progressValue%"
        binding.tvProgressValue.text = (progressValueStr)
        showChart()

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
        binding.homeMenu.setOnClickListener { view -> showPopupMenu(view)}
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
        data.setValueTextSize(14f)
        data.setValueTextColor(Color.BLACK)

        chart.data = data

        // Style
        chart.setUsePercentValues(true)
        chart.isDrawHoleEnabled = true
        chart.holeRadius = 60f
        chart.transparentCircleRadius = 65f
        chart.centerText = "Orders"
        chart.description.isEnabled = false

        // Labels on slices
        chart.setEntryLabelColor(Color.BLACK)
        chart.setEntryLabelTextSize(12f)

        // Legend
        val legend = chart.legend
        chart.legend.isEnabled = true
        chart.legend.textColor = Color.BLUE
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER

        chart.invalidate()
    }

    private fun calculateProductionProgress(): Int {
        val statuses = productionStatusRepo.getAll()
        if (statuses.isEmpty()) return 0

        var completed = 0
        val totalStages = statuses.size * 4

        statuses.forEach { s ->
            if (s.cuttingStatus == 1) completed++
            if (s.stitchingStatus == 1) completed++
            if (s.embroideryStatus == 1) completed++
            if (s.finishingStatus == 1) completed++
        }

        return ((completed * 100) / totalStages)
    }

}