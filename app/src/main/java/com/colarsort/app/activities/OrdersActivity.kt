package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductAdapter
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityOrdersBinding
import com.colarsort.app.models.Orders
import com.colarsort.app.models.Products
import com.colarsort.app.repository.OrdersRepo
import com.colarsort.app.repository.ProductsRepo

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var ordersRepo: OrdersRepo
    private lateinit var productsRepo: ProductsRepo
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database and repository
        dbHelper = DatabaseHelper(this)
        ordersRepo = OrdersRepo(dbHelper)
        productsRepo = ProductsRepo(dbHelper)

        // Setup view binding
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
//        products = productsRepo.getAll()
//
//        adapter = ProductAdapter(products)
//        binding.recyclerViewProducts.layoutManager = GridLayoutManager(this, 3)
//        binding.recyclerViewProducts.adapter = adapter

        // Navigation click listeners
        binding.ivHome.setOnClickListener { /* TODO: open home activity */ }
        binding.ivStatus.setOnClickListener { /* TODO: open status activity */ }
        binding.ivOrders.setOnClickListener {
            Toast.makeText(this, "You are already in Orders", Toast.LENGTH_SHORT).show()
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

        binding.orderMenu.setOnClickListener { view -> showPopupMenu(view) }

        binding.tvAddProductRow.setOnClickListener {

        }

    }

    // Popup menu
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.hamburger_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.log_out -> showLogoutDialog()
                else -> false
            }
            true
        }
        popup.show()
    }

    // Logout dialog
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, LoginActivity::class.java))
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Add Product Dialog
    private fun showAddProductDialog() {

        val dialogView = layoutInflater.inflate(R.layout.orders_dialog_add_product, null)

    }

}