package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductionStatusAdapter
import com.colarsort.app.databinding.ActivityProductionStatusBinding
import com.colarsort.app.repository.OrderItemsRepo
import com.colarsort.app.repository.OrdersRepo
import com.colarsort.app.repository.ProductionStatusDisplay
import com.colarsort.app.repository.ProductionStatusRepo
import com.colarsort.app.repository.ProductsRepo
import com.colarsort.app.utils.RecyclerUtils
import com.colarsort.app.utils.UtilityHelper.showCustomToast

class ProductionStatusActivity : BaseActivity() {

    private lateinit var binding: ActivityProductionStatusBinding
    private lateinit var adapter: ProductionStatusAdapter
    private lateinit var productionStatusRepo: ProductionStatusRepo
    private lateinit var orderItemsRepo: OrderItemsRepo
    private lateinit var productsRepo: ProductsRepo
    private lateinit var ordersRepo: OrdersRepo
    private val productionStatusList = ArrayList<ProductionStatusDisplay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        productionStatusRepo = ProductionStatusRepo(dbHelper)
        orderItemsRepo = OrderItemsRepo(dbHelper)
        productsRepo = ProductsRepo(dbHelper)
        ordersRepo = OrdersRepo(dbHelper)

        // Set up view binding
        binding = ActivityProductionStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up RecyclerView
        adapter = ProductionStatusAdapter(productionStatusList, productionStatusRepo, ordersRepo)
        binding.rvOrdersStatus.layoutManager = LinearLayoutManager(this)
        binding.rvOrdersStatus.adapter = adapter

        // Load data into the list
        val statuses = productionStatusRepo.getAll()
        val displayItems = statuses.mapNotNull { status ->
            val orderItem = orderItemsRepo.getById(status.orderItemId ?: return@mapNotNull null)
            val product = productsRepo.getById(orderItem?.productId ?: return@mapNotNull null)

            ProductionStatusDisplay(
                productionStatusId = status.id ?: return@mapNotNull null,
                productName = product?.name ?: "Unknown Product",
                orderId = orderItem.orderId ?: 0,
                orderItemId = orderItem.id ?: 0,
                orderItemsQuantity = orderItem.quantity ?: 0,
                cuttingStatus = status.cuttingStatus == 1,
                stitchingStatus = status.stitchingStatus == 1,
                embroideryStatus = status.embroideryStatus == 1,
                finishingStatus = status.finishingStatus == 1
            )
        }

        // Load into the adapter using RecyclerUtils
        RecyclerUtils.initialize(productionStatusList, displayItems, adapter)
        updateEmptyView()


        // Navigation click listeners
        binding.ivHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivStatus.setOnClickListener {
            showCustomToast(this, "You are already in Production Status")
        }
        binding.ivOrders.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivProducts.setOnClickListener {
            val intent = Intent(this, ProductsActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivMaterials.setOnClickListener {
            val intent = Intent(this, MaterialsActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.productionStatusMenu.setOnClickListener { view -> showPopupMenu(view) }

    }

    private fun updateEmptyView() {
        if (productionStatusList.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvOrdersStatus.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvOrdersStatus.visibility = View.VISIBLE
        }
    }
}