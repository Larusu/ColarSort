package com.colarsort.app.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductAdapter
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
import com.colarsort.app.models.ProductionStatus
import com.colarsort.app.repository.MaterialsRepo
import com.colarsort.app.repository.OrderItemsRepo
import com.colarsort.app.repository.ProductMaterialsRepo
import com.colarsort.app.repository.ProductionStatusRepo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import androidx.core.content.edit
import coil.load

class OrdersActivity : BaseActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private lateinit var ordersRepo: OrdersRepo
    private lateinit var productsRepo: ProductsRepo
    private lateinit var orderItemsRepo: OrderItemsRepo
    private lateinit var productionStatusRepo: ProductionStatusRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ordersRepo = OrdersRepo(dbHelper)
        productsRepo = ProductsRepo(dbHelper)
        orderItemsRepo = OrderItemsRepo(dbHelper)
        productionStatusRepo = ProductionStatusRepo(dbHelper)

        // Setup view binding
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        restoreTempOrder()

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigation click listeners
        binding.ivHome.setOnClickListener {
            saveTempOrder()
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivStatus.setOnClickListener {
            saveTempOrder()
            val intent = Intent(this, ProductionStatusActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivOrders.setOnClickListener {
            showCustomToast(this, "You are already in Orders")
        }
        binding.ivProducts.setOnClickListener {
            saveTempOrder()
            startActivity(Intent(this, ProductsActivity::class.java))
            finish()
        }
        binding.ivMaterials.setOnClickListener {
            saveTempOrder()
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
                    saveButton()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }
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
                        addProductToOrderList(product, container, dialog)
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

    private fun saveButton()
    {
        val container = binding.layoutMaterialsContainer
        val customerName = binding.etCustomerName.text.toString().trim()

        // validation
        if (customerName.isEmpty()) {
            showCustomToast(this, "Please enter a customer name")
            return
        }
        if (container.isEmpty()) {
            showCustomToast(this, "Order List is empty")
            return
        }

        val materialRepo = MaterialsRepo(dbHelper)

        // Collect all rows as (productId, quantity)
        val itemMaterials = mutableListOf<Pair<Int, Int>>()

        // Validate stock
        for (i in 0 until container.childCount)
        {
            val row = container.getChildAt(i)
            val rowBinding = OrderRowBinding.bind(row)

            val productId = rowBinding.root.tag as? Int
            if (productId == null) {
                showCustomToast(this, "Missing product id in row ${i + 1} — skipping")
                return
            }
            val productMaterialsRepo = ProductMaterialsRepo(dbHelper)
            val hasMaterials = productMaterialsRepo.checkProductIfExists(productId)

            if(!hasMaterials) {
                showCustomToast(this, "Missing materials in product id row ${i + 1}")
                return
            }

            val quantity = rowBinding.orderProductQuantity.text.toString().toIntOrNull() ?: 0
            if (quantity <= 0) {
                showCustomToast(this, "Invalid quantity in row ${i + 1} — skipping")
                return
            }

            itemMaterials.add(productId to quantity)
        }

        val insufficientMaterials = materialRepo.checkMaterialQuantity(itemMaterials)
        if (insufficientMaterials.isNotEmpty()) {
            val message = insufficientMaterials.joinToString("\n") { "• $it" }

            MaterialAlertDialogBuilder(this)
                .setTitle("Insufficient Materials")
                .setMessage("The following materials are not enough:\n\n$message")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        // build order metadata
        val productCount = container.childCount
        val numberOfDays = productCount * 2
        val expectedDelivery = "$numberOfDays days"
        val status = "Pending"

        // Insert a single order and get its id
        val order = Orders(
            id = null,
            customerName = customerName,
            status = status,
            expectedDelivery = expectedDelivery
        )

        val orderIdLong = ordersRepo.insertAndReturnId(order)
        if (orderIdLong == -1L) {
            showCustomToast(this, "Failed to create order")
            return
        }

        getSharedPreferences("TempOrder", MODE_PRIVATE)
            .edit {
                clear()
            }

        val orderId = orderIdLong.toInt()

        // Loop rows and insert order items using the same orderId
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i)
            val rowBinding = OrderRowBinding.bind(row)
            val productId = rowBinding.root.tag as? Int
            val quantity = rowBinding.orderProductQuantity.text.toString().toIntOrNull() ?: 0

            val orderItem = OrderItems(id = null, orderId = orderId, productId = productId, quantity = quantity)
            orderItemsRepo.insert(orderItem)

            materialRepo.setQuantity(quantity, productId!!)

            val orderItemId = orderItemsRepo.getLastInsertedId()
            val productionStatusModel = ProductionStatus(null, orderItemId, 0, 0, 0, 0)
            productionStatusRepo.insert(productionStatusModel)
        }

        // clear UI
        container.removeAllViews()
        binding.etCustomerName.setText("")
        showCustomToast(this, "Order Created Successfully")
    }

    private fun addProductToOrderList(product: Products, container: LinearLayout, dialog: Dialog)
    {
        // Check if product is already added
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i)
            val tvProductName = row.findViewById<TextView>(R.id.order_product_name)
            if (tvProductName.text == product.name) {
                showCustomToast(this, "Product is already on the List")
                return
            }
        }

        // Inflate row using binding
        val rowBinding = OrderRowBinding.inflate(layoutInflater, container, false)
        rowBinding.orderProductName.text = product.name
        rowBinding.orderProductQuantity.setText("1")
        rowBinding.root.tag = product.id

        product.image?.let { path ->
            rowBinding.orderProductImage.load(path)
        }

        // Quantity buttons
        rowBinding.quantityAdd.setOnClickListener {
            val currentQty = rowBinding.orderProductQuantity.text.toString().toIntOrNull() ?: 0
            val text = currentQty + 1
            rowBinding.orderProductQuantity.setText((text).toString())
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
    }

    private fun saveTempOrder() {
        val container = binding.layoutMaterialsContainer
        val items = mutableListOf<TempOrderItem>()

        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i)
            val rowBinding = OrderRowBinding.bind(row)

            val productId = rowBinding.root.tag as? Int ?: continue
            val qty = rowBinding.orderProductQuantity.text.toString().toIntOrNull() ?: 1

            items.add(TempOrderItem(productId, qty))
        }

        val json = Gson().toJson(items)

        val prefs = getSharedPreferences("TempOrder", MODE_PRIVATE)
        prefs.edit {
            putString("customerName", binding.etCustomerName.text.toString())
                .putString("items", json)
        }
    }

    private fun restoreTempOrder() {
        val prefs = getSharedPreferences("TempOrder", MODE_PRIVATE)
        val json = prefs.getString("items", null) ?: return

        val items = Gson().fromJson(json, Array<TempOrderItem>::class.java)
        val container = binding.layoutMaterialsContainer

        binding.etCustomerName.setText(prefs.getString("customerName", ""))

        for (item in items) {
            val product = productsRepo.getById(item.productId) ?: continue
            addProductToOrderList(product, container, Dialog(this))
            val lastRow = container.getChildAt(container.childCount - 1)
            val rowBinding = OrderRowBinding.bind(lastRow)
            rowBinding.orderProductQuantity.setText(item.quantity.toString())
        }
    }

    private data class TempOrderItem(
        val productId: Int,
        val quantity: Int
    )
}