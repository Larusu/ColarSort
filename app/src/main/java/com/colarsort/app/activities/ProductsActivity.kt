package com.colarsort.app.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductAdapter
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityProductsBinding
import com.colarsort.app.databinding.DialogAddProductBinding
import com.colarsort.app.databinding.MaterialRowBinding
import com.colarsort.app.models.Products
import com.colarsort.app.repository.MaterialsRepo
import com.colarsort.app.repository.ProductsRepo
import com.colarsort.app.utils.RecyclerUtils
import com.colarsort.app.utils.UtilityHelper.compressBitmap
import com.colarsort.app.utils.UtilityHelper.inputStreamToByteArray
import com.colarsort.app.utils.UtilityHelper.showCustomToast

class ProductsActivity : BaseActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var productsRepo: ProductsRepo
    private lateinit var adapter: ProductAdapter
    private lateinit var materialsRepo: MaterialsRepo
    private val productList = ArrayList<Products>()
    private var tempDialogImageView: ImageView? = null
    private var selectedImageBytes: ByteArray? = null

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
            showCustomToast(this, "You are already in Products")
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
                        if (!successful) {
                            showCustomToast(this, "Delete failed")
                            return@setOnMenuItemClickListener false
                        }

                        showCustomToast(this, "Product deleted")
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
                showCustomToast(this, "Logged out successfully")
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Show Add Product dialog
    private fun showAddProductDialog() {
        selectedImageBytes = null

        val dialogBinding = DialogAddProductBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val layoutMaterialsContainer = dialogBinding.layoutMaterialsContainer
        addMaterialRow(layoutMaterialsContainer) // Add first row

        // "+" button adds more rows
        dialogBinding.ivAddMaterialRow.setOnClickListener {
            addMaterialRow(layoutMaterialsContainer)
        }

        // Image picker
        dialogBinding.ivProductImage.setOnClickListener {
            tempDialogImageView = dialogBinding.ivProductImage
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        // Save button
        dialogBinding.tvSave.setOnClickListener {
            val name = dialogBinding.etProductName.text.toString().trim()

            if (name.isEmpty()) {
                showCustomToast(this, "Please enter a product name")
                return@setOnClickListener
            }

            val selectedMaterials = mutableListOf<String>()
            for (i in 0 until layoutMaterialsContainer.childCount) {
                val row = layoutMaterialsContainer.getChildAt(i)
                val rowBinding = MaterialRowBinding.bind(row)

                val materialName = rowBinding.sAvailableMaterials.selectedItem as? String
                val quantityText = rowBinding.etMaterialQuantity.text.toString().trim()
                val quantity = quantityText.toDoubleOrNull()

                if (materialName == null) {
                    showCustomToast(this, "Please select a material in row ${i + 1}")
                    return@setOnClickListener
                }

                if (quantity == null || quantity <= 0) {
                    showCustomToast(this, "Please enter a valid quantity for material ${materialName}")
                    return@setOnClickListener
                }

                selectedMaterials.add(materialName)
            }

            // Check for duplicate materials
            val duplicates = selectedMaterials.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            if (duplicates.isNotEmpty()) {
                showCustomToast(this, "Duplicate materials found: ${duplicates.joinToString()}")
                return@setOnClickListener
            }

            // No issues, proceed to save
            val product = Products(null, name, selectedImageBytes)
            productsRepo.insert(product)

            showCustomToast(this, "Product added successfully")
            RecyclerUtils.insertedItems(productList, productsRepo.getAll(), adapter)

            tempDialogImageView = null
            dialog.dismiss()
        }



        // Cancel button
        dialogBinding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    //  Add a material row
    private fun addMaterialRow(container: LinearLayout) {
        // Inflate using binding
        val rowBinding = MaterialRowBinding.inflate(layoutInflater, container, false)

        // Put materials in the spinner
        val materials = materialsRepo.getAll()
        val materialNames = materials.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, materialNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rowBinding.sAvailableMaterials.adapter = adapter

        // Update unit when material selected
        rowBinding.sAvailableMaterials.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.WHITE) // Selected text white
                rowBinding.tvUnit.text = materials[position].unit
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Remove row
        rowBinding.btnRemoveRow.setOnClickListener {
            container.removeView(rowBinding.root)
        }

        // Add the row to the container
        container.addView(rowBinding.root)
    }


    //  Show Edit Product dialog
    private fun showEditProductDialog(product: Products? = null) {
        selectedImageBytes = product?.image

        // Inflate using binding
        val dialogBinding = DialogAddProductBinding.inflate(layoutInflater)

        // Set dialog title
        dialogBinding.tvAddProduct.text = if (product == null) "Add Product" else "Edit Product"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // Pre-fill fields if editing
        product?.let {
            dialogBinding.etProductName.setText(it.name)
            it.image?.let { bytes ->
                dialogBinding.ivProductImage.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            }
            addMaterialRow(dialogBinding.layoutMaterialsContainer) // Placeholder for materials
        } ?: addMaterialRow(dialogBinding.layoutMaterialsContainer) // Placeholder for new product

        // Image picker
        dialogBinding.ivProductImage.setOnClickListener {
            tempDialogImageView = dialogBinding.ivProductImage
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        // "+" button adds material rows
        dialogBinding.ivAddMaterialRow.setOnClickListener {
            addMaterialRow(dialogBinding.layoutMaterialsContainer)
        }

        // Save product
        dialogBinding.tvSave.setOnClickListener {
            val name = dialogBinding.etProductName.text.toString().trim()
            if (name.isEmpty()) {
                showCustomToast(this, "Please enter a product name")
                return@setOnClickListener
            }

            if (product == null) {
                productsRepo.insert(Products(null, name, selectedImageBytes))
                showCustomToast(this, "Product added successfully")
            } else {
                val updated = product.copy(name = name, image = selectedImageBytes ?: product.image)
                val success = productsRepo.update(updated)

                if (success) {
                    RecyclerUtils.updateItem(productList, updated, adapter) { it.id }
                    showCustomToast(this, "Product updated successfully")
                } else {
                    showCustomToast(this, "Update failed")
                }
            }

            tempDialogImageView = null
            dialog.dismiss()
        }

        // Cancel button
        dialogBinding.tvCancel.setOnClickListener {
            tempDialogImageView = null
            dialog.dismiss()
        }

        dialog.show()
    }

}
