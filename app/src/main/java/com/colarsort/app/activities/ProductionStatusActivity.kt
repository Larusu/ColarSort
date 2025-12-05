package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductionStatusAdapter
import com.colarsort.app.data.pojo.ProductionStatusDisplay
import com.colarsort.app.databinding.ActivityProductionStatusBinding
import com.colarsort.app.data.repository.OrderItemsRepo
import com.colarsort.app.data.repository.OrdersRepo
import com.colarsort.app.data.repository.ProductionStatusRepo
import com.colarsort.app.data.repository.ProductsRepo
import com.colarsort.app.data.repository.RepositoryProvider
import com.colarsort.app.utils.RecyclerUtils
import com.colarsort.app.utils.UtilityHelper.showCustomToast
import kotlinx.coroutines.launch

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

        productionStatusRepo = RepositoryProvider.productionStatusRepo
        orderItemsRepo = RepositoryProvider.orderItemRepo
        productsRepo = RepositoryProvider.productsRepo
        ordersRepo = RepositoryProvider.ordersRepo

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
        adapter = ProductionStatusAdapter(productionStatusList, productionStatusRepo, ordersRepo, lifecycleScope)
        binding.rvOrdersStatus.layoutManager = LinearLayoutManager(this)
        binding.rvOrdersStatus.adapter = adapter

        // Load data into the list
        lifecycleScope.launch {
            val statuses = productionStatusRepo.getAll()
            val displayItems = statuses.map { status ->
                val orderItem = orderItemsRepo.getById(status.orderItemId)
                val product = runIO { productsRepo.getById(orderItem?.productId ?: return@runIO null) }

                ProductionStatusDisplay(
                    productionStatusId = status.id,
                    productName = product?.name ?: "Unknown Product",
                    orderId = orderItem!!.orderId,
                    orderItemId = orderItem.id,
                    orderItemsQuantity = orderItem.quantity,
                    cuttingStatus = status.cuttingStatus == 1,
                    stitchingStatus = status.stitchingStatus == 1,
                    embroideryStatus = status.embroideryStatus == 1,
                    finishingStatus = status.finishingStatus == 1
                )
            }


            // Load into the adapter using RecyclerUtils
            RecyclerUtils.initialize(productionStatusList, displayItems, adapter)
            updateEmptyView()
        }

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