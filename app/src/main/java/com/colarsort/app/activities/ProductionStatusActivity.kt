package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductionStatusAdapter
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityProductionStatusBinding
import com.colarsort.app.models.OrderItems
import com.colarsort.app.models.ProductionStatus
import com.colarsort.app.models.Products
import com.colarsort.app.repository.OrderItemsRepo
import com.colarsort.app.repository.ProductionStatusDisplay
import com.colarsort.app.repository.ProductionStatusRepo
import com.colarsort.app.repository.ProductsRepo
import com.colarsort.app.utils.RecyclerUtils
import com.colarsort.app.utils.UtilityHelper.showCustomToast

class ProductionStatusActivity : BaseActivity() {

    private lateinit var binding: ActivityProductionStatusBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ProductionStatusAdapter
    private lateinit var productionStatusRepo: ProductionStatusRepo
    private lateinit var orderItemsRepo: OrderItemsRepo
    private lateinit var productsRepo: ProductsRepo
    private val productionStatusList = ArrayList<ProductionStatusDisplay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database and repository
        dbHelper = DatabaseHelper(this)
        productionStatusRepo = ProductionStatusRepo(dbHelper)
        orderItemsRepo = OrderItemsRepo(dbHelper)
        productsRepo = ProductsRepo(dbHelper)

        // Set up view binding
        binding = ActivityProductionStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Temporary Creation
        val existing = productionStatusRepo.getAll()
        if (existing.isEmpty()) {
            // Insert sample order items
            orderItemsRepo.insert(OrderItems(1, 1, 1, 10)) // T-Shirt x10
            orderItemsRepo.insert(OrderItems(2, 1, 2, 5))  // Hoodie x5
            orderItemsRepo.insert(OrderItems(3, 2, 3, 8))  // Polo x8

            // Insert production status
            productionStatusRepo.insert(ProductionStatus(1, 1, 0, 1, 0, 0))
            productionStatusRepo.insert(ProductionStatus(2, 2, 1, 1, 1, 0))
            productionStatusRepo.insert(ProductionStatus(3, 3, 0, 0, 0, 1))
        }

        // Set up RecyclerView
        adapter = ProductionStatusAdapter(productionStatusList, productionStatusRepo)
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
                orderItemsQuantity = orderItem.quantity?.toInt() ?: 0,
                cuttingStatus = status.cuttingStatus == 1,
                stitchingStatus = status.stitchingStatus == 1,
                embroideryStatus = status.embroideryStatus == 1,
                finishingStatus = status.finishingStatus == 1
            )
        }

        // Load into the adapter using RecyclerUtils
        RecyclerUtils.initialize(productionStatusList, displayItems, adapter)

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
            val intent = Intent(this, ProductsActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivMaterials.setOnClickListener {
            val intent = Intent(this, MaterialsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}