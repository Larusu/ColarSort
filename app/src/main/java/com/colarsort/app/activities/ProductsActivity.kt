package com.colarsort.app.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductAdapter
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityProductsBinding
import com.colarsort.app.models.Products
import com.colarsort.app.repository.MaterialsRepo
import com.colarsort.app.repository.ProductsRepo
import com.colarsort.app.utils.UtilityHelper.compressBitmap
import com.colarsort.app.utils.UtilityHelper.inputStreamToByteArray

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var productsRepo: ProductsRepo
    private lateinit var adapter: ProductAdapter
    private lateinit var materialsRepo: MaterialsRepo
    private val productList = ArrayList<Products>()

    private var tempDialogImageView: ImageView? = null // Temporary reference for image picker
    private var selectedImageBytes: ByteArray? = null // Holds selected image bytes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database and repositories
        dbHelper = DatabaseHelper(this)
        productsRepo = ProductsRepo(dbHelper)
        materialsRepo = MaterialsRepo(dbHelper)

        // Setup view binding
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Temporary product creation
        val existing = productsRepo.getAll()
        if (existing.size < 4) {
            val product = arrayOf(
                Products(null, "T-shirt", inputStreamToByteArray(this, "products/tshirt.png")),
                Products(null, "Jeans", inputStreamToByteArray(this, "products/jeans.png")),
                Products(null, "Sweater", inputStreamToByteArray(this, "products/sweater.png")),
                Products(null, "Dress", inputStreamToByteArray(this, "products/dress.png")),
                Products(null, "Shoes", inputStreamToByteArray(this, "products/shoes.png")),
                Products(null, "Hat", inputStreamToByteArray(this, "products/hat.png")),
                Products(null, "Jacket", inputStreamToByteArray(this, "products/jacket.png")),
                Products(null, "Gloves", inputStreamToByteArray(this, "products/gloves.png")),
                Products(null, "Scarf", inputStreamToByteArray(this, "products/scarf.jpg"))
            )
            product.forEach { p -> productsRepo.insert(p) }
        }

        // Setup RecyclerView
        adapter = ProductAdapter(productList)
        binding.recyclerViewProducts.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerViewProducts.adapter = adapter

        // Load data from database
        val existingSize = productList.size
        val newItems = productsRepo.getAll()
        productList.addAll(newItems)
        adapter.notifyItemRangeInserted(existingSize, newItems.size)

        // Navigation click listeners
        binding.ivHome.setOnClickListener { /* TODO: open home activity */ }
        binding.ivStatus.setOnClickListener { /* TODO: open status activity */ }
        binding.ivOrders.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivProducts.setOnClickListener {
            Toast.makeText(this, "You are already in products", Toast.LENGTH_SHORT).show()
        }
        binding.ivMaterials.setOnClickListener {
            val intent = Intent(this, MaterialsActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.productMenu.setOnClickListener { view -> showPopupMenu(view) }
        binding.btnAdd.setOnClickListener { showAddProductDialog() }

        // Adapter item_more click listener
        adapter.onItemMoreClickListener = { product: Products, view: View ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.more_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit_product -> showEditProductDialog(product)
                    R.id.delete_product -> {
                        val successful = productsRepo.deleteColumn(product.id!!)
                        if (!successful) Toast.makeText(this, "Error deleting product", Toast.LENGTH_SHORT).show()

                        val index = productList.indexOf(product)
                        if (index != -1) {
                            productList.removeAt(index)
                            adapter.notifyItemRemoved(index)
                        }
                    }
                }
                true
            }
            popup.show()
        }
    }

    // Handle image picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            tempDialogImageView?.setImageBitmap(bitmap) // Show in dialog
            selectedImageBytes = compressBitmap(bitmap) // Compress and store
        }
    }

    // Popup menu for top-right hamburger menu
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

    // Logout confirmation dialog
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

    // Show Add Product dialog
    private fun showAddProductDialog() {
        selectedImageBytes = null

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val btnSave = dialogView.findViewById<TextView>(R.id.tv_save)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val ivProductImage = dialogView.findViewById<ImageView>(R.id.iv_product_image)
        val etProductName = dialogView.findViewById<EditText>(R.id.et_product_name)
        val layoutMaterialsContainer = dialogView.findViewById<LinearLayout>(R.id.layout_materials_container)
        val btnAddRow = dialogView.findViewById<ImageView>(R.id.iv_add_material_row)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        addMaterialRow(layoutMaterialsContainer) // Add first row

        // "+" button adds more rows
        btnAddRow.setOnClickListener { addMaterialRow(layoutMaterialsContainer) }

        // Image picker
        ivProductImage.setOnClickListener {
            tempDialogImageView = ivProductImage
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        btnSave.setOnClickListener { TODO("Save product") }
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    //  Add a material row
    private fun addMaterialRow(container: LinearLayout) {
        val row = layoutInflater.inflate(R.layout.material_row, container, false)
        val spinner = row.findViewById<Spinner>(R.id.s_available_materials)
        val etQty = row.findViewById<EditText>(R.id.et_material_quantity)
        val tvUnit = row.findViewById<TextView>(R.id.tv_unit)
        val btnRemove = row.findViewById<ImageView>(R.id.btn_remove_row)

        // Put materials in the spinner
        val materials = materialsRepo.getAll()
        val materialNames = materials.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, materialNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Update unit when material selected
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.WHITE) // Selected text white
                tvUnit.text = materials[position].unit
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Remove row
        btnRemove.setOnClickListener { container.removeView(row) }
        container.addView(row)
    }

    //  Show Edit Product dialog
    private fun showEditProductDialog(product: Products? = null) {
        selectedImageBytes = product?.image

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val tvAddProduct = dialogView.findViewById<TextView>(R.id.tv_add_product)
        val etProductName = dialogView.findViewById<EditText>(R.id.et_product_name)
        val ivProductImage = dialogView.findViewById<ImageView>(R.id.iv_product_image)
        val layoutMaterialsContainer = dialogView.findViewById<LinearLayout>(R.id.layout_materials_container)
        val btnAddRow = dialogView.findViewById<ImageView>(R.id.iv_add_material_row)
        val btnSave = dialogView.findViewById<TextView>(R.id.tv_save)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)

        tvAddProduct.text = if (product == null) "Add Product" else "Edit Product"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // Pre-fill fields if editing
        product?.let {
            etProductName.setText(it.name)
            it.image?.let { bytes ->
                ivProductImage.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            }
            addMaterialRow(layoutMaterialsContainer) // Placeholder for materials
        } ?: addMaterialRow(layoutMaterialsContainer) // Placeholder row for new product

        // Image picker
        ivProductImage.setOnClickListener {
            tempDialogImageView = ivProductImage
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        // "+" button adds material rows
        btnAddRow.setOnClickListener { addMaterialRow(layoutMaterialsContainer) }

        // Save product
        btnSave.setOnClickListener {
            val name = etProductName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a product name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (product == null) {
                productsRepo.insert(Products(null, name, selectedImageBytes))
                Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show()
            } else {
                val updated = product.copy(name = name, image = selectedImageBytes ?: product.image)
                val success = productsRepo.update(updated)
                Toast.makeText(this, if (success) "Product updated" else "Update failed", Toast.LENGTH_SHORT).show()
            }

            // TODO: Save material rows if needed
            tempDialogImageView = null
            dialog.dismiss()

            // Refresh RecyclerView
            productList.clear()
            productList.addAll(productsRepo.getAll())
            adapter.notifyDataSetChanged()
        }

        btnCancel.setOnClickListener {
            tempDialogImageView = null
            dialog.dismiss()
        }

        dialog.show()
    }
}
