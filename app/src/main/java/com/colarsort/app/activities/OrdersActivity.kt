package com.colarsort.app.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
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
import com.colarsort.app.models.Products
import com.colarsort.app.repository.OrdersRepo
import com.colarsort.app.repository.ProductsRepo
import com.colarsort.app.utils.UtilityHelper.showCustomToast
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

        // Navigation click listeners
        binding.ivHome.setOnClickListener { /* TODO: open home activity */ }
        binding.ivStatus.setOnClickListener { /* TODO: open status activity */ }
        binding.ivOrders.setOnClickListener {
            showCustomToast(this, "You are already in Orders")
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
            showAddProductDialog()
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
                showCustomToast(this, "Logged out")
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Add Product Dialog
    @SuppressLint("SetTextI18n")
    private fun showAddProductDialog() {
        val dialogView = layoutInflater.inflate(R.layout.orders_dialog_add_product, null)
        val recyclerViewProducts = dialogView.findViewById<RecyclerView>(R.id.recyclerViewProducts)

        // Set up RecyclerView in grid 3 columns
        recyclerViewProducts.layoutManager = GridLayoutManager(this, 3)

        val products = ArrayList(productsRepo.getAll())
        val adapter = ProductAdapter(products)
        recyclerViewProducts.adapter = adapter

        // Reference to the LinearLayout in main activity where selected products are added
        val container = findViewById<LinearLayout>(R.id.layout_materials_container)

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Product click listener
        adapter.onItemMoreClickListener = { product: Products, view: View ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.more_menu_orders, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.add_product -> {
                        // Inflate a row layout for the selected product
                        val row = layoutInflater.inflate(R.layout.order_row, container, false)
                        val tvProductName = row.findViewById<TextView>(R.id.order_product_name)
                        val etQuantity = row.findViewById<EditText>(R.id.order_product_quantity)
                        val btnRemove = row.findViewById<ImageView>(R.id.btn_remove_row)
                        val ivProductImage = row.findViewById<ImageView>(R.id.order_product_image)
                        val addQuantity = row.findViewById<ImageView>(R.id.quantity_add)
                        val minusQuantity = row.findViewById<ImageView>(R.id.quantity_minus)

                        // Set product name, image, quantity
                        tvProductName.text = product.name
                        etQuantity.setText("1")

                        product.image?.let { bytes ->
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            ivProductImage.setImageBitmap(bitmap)
                        }

                        // Quantity buttons
                        addQuantity.setOnClickListener {
                            val currentQuantity = etQuantity.text.toString().toIntOrNull() ?: 0
                            etQuantity.setText((currentQuantity + 1).toString())
                        }

                        minusQuantity.setOnClickListener {
                            val currentQuantity = etQuantity.text.toString().toIntOrNull() ?: 0
                            if (currentQuantity > 1) {
                                etQuantity.setText((currentQuantity - 1).toString())
                            }
                        }

                        // Remove row button
                        btnRemove.setOnClickListener {
                            container.removeView(row)
                            showCustomToast(this, "Product removed from the List")
                        }

                        // Add row to the LinearLayout
                        container.addView(row)
                        showCustomToast(this, "Product added to List")

                        // Dismiss the dialog after adding
                        dialog.dismiss()
                        true
                    }

                    R.id.cancel -> true
                    else -> false
                }
            }
            popup.show()
        }
        dialog.show()
    }

}