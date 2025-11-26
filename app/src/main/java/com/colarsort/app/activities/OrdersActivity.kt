package com.colarsort.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
import androidx.core.view.isEmpty
import com.colarsort.app.databinding.OrderRowBinding
import com.colarsort.app.databinding.OrdersDialogAddProductBinding
import com.colarsort.app.models.OrderItems
import com.colarsort.app.models.Orders
import com.colarsort.app.repository.OrderItemsRepo

class OrdersActivity : BaseActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var ordersRepo: OrdersRepo
    private lateinit var productsRepo: ProductsRepo
    private lateinit var orderItemsRepo: OrderItemsRepo
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database and repository
        dbHelper = DatabaseHelper(this)
        ordersRepo = OrdersRepo(dbHelper)
        productsRepo = ProductsRepo(dbHelper)
        orderItemsRepo = OrderItemsRepo(dbHelper)

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
        binding.ivStatus.setOnClickListener {
            val intent = Intent(this, ProductionStatusActivity::class.java)
            startActivity(intent)
            finish()
        }
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

        binding.tvClearOrderList.setOnClickListener {
            if (binding.layoutMaterialsContainer.isEmpty()) {
                showCustomToast(this, "Order List is empty")
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Clear Order List")
                .setMessage("Are you sure you want to clear the order list?")
                .setPositiveButton("Yes") { _, _ ->
                    binding.layoutMaterialsContainer.removeAllViews()
                    showCustomToast(this, "Order List Cleared")
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        binding.tvConfirmOrder.setOnClickListener {
            val customerName = binding.etCustomerName.text.toString().trim()

            if (customerName.isEmpty()) {
                showCustomToast(this, "Please enter a customer name")
                return@setOnClickListener
            }

            if (binding.layoutMaterialsContainer.isEmpty()) {
                showCustomToast(this, "Order List is empty")
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Confirm Order")
                .setMessage("Are you sure you want to confirm the order?")
                .setPositiveButton("Yes") { _, _ ->

                    val container = binding.layoutMaterialsContainer
                    val customerName = binding.etCustomerName.text.toString().trim()

                    // validation
                    if (customerName.isEmpty()) {
                        showCustomToast(this, "Please enter a customer name")
                        return@setPositiveButton
                    }
                    if (container.childCount == 0) {
                        showCustomToast(this, "Order List is empty")
                        return@setPositiveButton
                    }

                    // build order metadata
                    val productCount = container.childCount
                    val numberOfDays = productCount * 2
                    val expectedDelivery = "$numberOfDays days"
                    val status = "In Production"

                    // Insert a single order and get its id (insertAndReturnId must be implemented)
                    val order = Orders(id = null, customerName = customerName, status = status, expectedDelivery = expectedDelivery)
                    val orderIdLong = ordersRepo.insertAndReturnId(order)
                    if (orderIdLong == -1L) {
                        showCustomToast(this, "Failed to create order")
                        return@setPositiveButton
                    }
                    val orderId = orderIdLong.toInt()

                    // Loop rows and insert order items using the same orderId
                    for (i in 0 until container.childCount) {
                        val row = container.getChildAt(i)
                        val rowBinding = OrderRowBinding.bind(row)

                        // IMPORTANT: productId must have been stored on the row when added:
                        // rowBinding.root.tag = product.id
                        val productId = rowBinding.root.tag as? Int
                        if (productId == null) {
                            showCustomToast(this, "Missing product id in row ${i + 1} — skipping")
                            continue
                        }

                        val quantity = rowBinding.orderProductQuantity.text.toString().toIntOrNull() ?: 0
                        if (quantity <= 0) {
                            showCustomToast(this, "Invalid quantity in row ${i + 1} — skipping")
                            continue
                        }

                        val orderItem = OrderItems(id = null, orderId = orderId, productId = productId, quantity = quantity)
                        orderItemsRepo.insert(orderItem) // or insertAndReturnId if you prefer
                    }

                    // clear UI
                    container.removeAllViews()
                    binding.etCustomerName.setText("")
                    showCustomToast(this, "Order Created Successfully")

                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
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
    private fun showAddProductDialog() {
        // Use ViewBinding for the dialog
        val dialogBinding = OrdersDialogAddProductBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // Setup RecyclerView with grid 3 columns
        val products = ArrayList(productsRepo.getAll())
        val adapter = ProductAdapter(products)
        dialogBinding.recyclerViewProducts.layoutManager = GridLayoutManager(this, 3)
        dialogBinding.recyclerViewProducts.adapter = adapter

        // Reference to the container in main activity where selected products are added
        val container = binding.layoutMaterialsContainer

        // Product click listener
        adapter.onItemMoreClickListener = { product: Products, view: View ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.more_menu_orders, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.add_product -> {
                        // Check if product is already added
                        for (i in 0 until container.childCount) {
                            val row = container.getChildAt(i)
                            val tvProductName = row.findViewById<TextView>(R.id.order_product_name)
                            if (tvProductName.text == product.name) {
                                showCustomToast(this, "Product is already on the List")
                                return@setOnMenuItemClickListener true
                            }
                        }

                        // Inflate row using binding
                        val rowBinding = OrderRowBinding.inflate(layoutInflater, container, false)
                        rowBinding.orderProductName.text = product.name
                        rowBinding.orderProductQuantity.setText("1")
                        rowBinding.root.tag = product.id

                        product.image?.let { bytes ->
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            rowBinding.orderProductImage.setImageBitmap(bitmap)
                        }

                        // Quantity buttons
                        rowBinding.quantityAdd.setOnClickListener {
                            val currentQty = rowBinding.orderProductQuantity.text.toString().toIntOrNull() ?: 0
                            rowBinding.orderProductQuantity.setText((currentQty + 1).toString())
                        }
                        rowBinding.quantityMinus.setOnClickListener {
                            val currentQty = rowBinding.orderProductQuantity.text.toString().toIntOrNull() ?: 0
                            if (currentQty > 1) rowBinding.orderProductQuantity.setText((currentQty - 1).toString())
                        }

                        // Remove row
                        rowBinding.btnRemoveRow.setOnClickListener {
                            container.removeView(rowBinding.root)
                            showCustomToast(this, "Product removed from the List")
                        }

                        // Add row to container
                        container.addView(rowBinding.root)
                        showCustomToast(this, "Product added to List")

                        // Dismiss dialog
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