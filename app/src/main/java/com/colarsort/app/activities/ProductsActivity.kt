package com.colarsort.app.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductAdapter
import com.colarsort.app.databinding.ActivityProductsBinding
import com.colarsort.app.databinding.DialogAddProductBinding
import com.colarsort.app.databinding.MaterialRowBinding
import com.colarsort.app.models.ProductMaterials
import com.colarsort.app.models.Products
import com.colarsort.app.repository.MaterialsRepo
import com.colarsort.app.repository.ProductMaterialsRepo
import com.colarsort.app.repository.ProductsRepo
import com.colarsort.app.utils.RecyclerUtils
import com.colarsort.app.utils.UtilityHelper.compressBitmap
import com.colarsort.app.utils.UtilityHelper.inputStreamToByteArray
import com.colarsort.app.utils.UtilityHelper.showCustomToast

class ProductsActivity : BaseActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var adapter: ProductAdapter
    private lateinit var productsRepo: ProductsRepo
    private lateinit var materialsRepo: MaterialsRepo
    private lateinit var productMaterialsRepo: ProductMaterialsRepo
    private val productList = ArrayList<Products>()
    private var tempDialogImageView: ImageView? = null
    private var selectedImageBytes: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        productsRepo = ProductsRepo(dbHelper)
        materialsRepo = MaterialsRepo(dbHelper)
        productMaterialsRepo = ProductMaterialsRepo(dbHelper)

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
        binding.ivHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
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
            showCustomToast(this, "You are already in Products")
        }
        binding.ivMaterials.setOnClickListener {
            val intent = Intent(this, MaterialsActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Menu Button
        binding.productMenu.setOnClickListener { view -> showPopupMenu(view) }

        // For searching product
        binding.ivSearch.setOnClickListener {
            val strSearch = binding.etSearchField.text.toString().trim()

            val newList = if (strSearch.isEmpty()) {
                productsRepo.getAll()
            } else {
                productsRepo.searchProductBaseOnName(strSearch)
            }
            adapter = ProductAdapter(ArrayList(newList))
            binding.recyclerViewProducts.layoutManager = GridLayoutManager(this, 3)
            binding.recyclerViewProducts.adapter = adapter
        }

        when(sessionManager.getRole())
        {
            "Worker" -> {
                adapter.setUserRole("Worker")
                binding.btnAdd.visibility = View.GONE
            }
            "Manager" -> handleManagerSession()
        }

    }

    // Handle image picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            tempDialogImageView?.setImageBitmap(bitmap) // Show in dialog
            selectedImageBytes = compressBitmap(bitmap) // Compress and store
        }
    }

    private fun handleManagerSession()
    {
        binding.btnAdd.setOnClickListener { showAddProductDialog() }

        // Adapter item_more click listener
        adapter.onItemMoreClickListener = { product: Products, view: View ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.more_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                handleProductMenuClick(product, menuItem.itemId)
            }
            popup.show()
        }
    }

    private fun handleProductMenuClick(product: Products, menuItemId: Int): Boolean {
        when (menuItemId) {
            R.id.edit_product -> {
                showEditProductDialog(product)
            }

            R.id.delete_product -> {
                productMaterialsRepo.deleteProductById(product.id)

                val successful = productsRepo.deleteColumn(product.id!!)
                if (!successful) {
                    showCustomToast(this, "Delete failed")
                    return false
                }
                val position = productList.indexOf(product)
                RecyclerUtils.deleteAt(productList, position, adapter)
                showCustomToast(this, "Material deleted successfully")
            }
        }
        return true
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
            handleSaveButton(
                dialogBinding = dialogBinding,
                layoutMaterialsContainer = dialogBinding.layoutMaterialsContainer,
                dialog = dialog,
                selectedImageBytes = selectedImageBytes
            )
        }

        // Cancel button
        dialogBinding.tvCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun handleSaveButton(
        dialogBinding: DialogAddProductBinding,
        layoutMaterialsContainer: LinearLayout,
        dialog: Dialog,
        selectedImageBytes: ByteArray?
    )
    {
        val name = dialogBinding.etProductName.text.toString().trim()

        if (name.isEmpty()) {
            showCustomToast(this, "Please enter a product name")
            return
        }

        // Collect selected materials
        val selectedMaterials = mutableListOf<Pair<Int, Double>>() // Pair<materialId, quantity>

        for (i in 0 until layoutMaterialsContainer.childCount) {
            val row = layoutMaterialsContainer.getChildAt(i)

            // safer retrieval of binding
            val rowBinding = (row.tag as? MaterialRowBinding) ?: MaterialRowBinding.bind(row)

            val materialName = rowBinding.sAvailableMaterials.selectedItem as? String
            val quantityText = rowBinding.etMaterialQuantity.text.toString().trim()
            val quantity = quantityText.toDoubleOrNull()

            if (materialName == null) {
                showCustomToast(this, "Please select a material in row ${i + 1}")
                return
            }

            if (quantity == null || quantity <= 0) {
                showCustomToast(this, "Please enter a valid quantity for material $materialName")
                return
            }

            // Get materialId from the repository
            val material = materialsRepo.getAll().firstOrNull { it.name == materialName }
            if (material == null) {
                showCustomToast(this, "Material $materialName not found")
                return
            }

            selectedMaterials.add(material.id!! to quantity)
        }

        // Check for duplicate materials
        val duplicates = selectedMaterials.map { it.first }.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (duplicates.isNotEmpty()) {
            val duplicateNames = duplicates.mapNotNull { id -> materialsRepo.getAll().firstOrNull { it.id == id }?.name }
            showCustomToast(this, "Duplicate materials found: ${duplicateNames.joinToString()}")
            return
        }

        // Insert the product first
        val product = Products(null, name, selectedImageBytes)
        productsRepo.insert(product)

        // Get the last inserted product ID
        val productId = productsRepo.getLastInsertedId()

        // Insert into ProductMaterials table
        selectedMaterials.forEach { (materialId, quantityRequired) ->
            val productMaterial =
                ProductMaterials(null, productId, materialId, quantityRequired)
            productMaterialsRepo.insert(productMaterial)
        }

        showCustomToast(this, "Product added successfully")
        RecyclerUtils.insertedItems(productList, productsRepo.getAll(), adapter)

        tempDialogImageView = null
        dialog.dismiss()
    }

    //  Add a material row
    private fun addMaterialRow(container: LinearLayout) {
        // Inflate using binding
        val rowBinding = MaterialRowBinding.inflate(layoutInflater, container, false)

        // Put materials in the spinner
        val materials = materialsRepo.getAll()

        if (materials.isEmpty()) {
            showCustomToast(this, "No materials available. Please add materials first.")
            return
        }

        val materialNames = materials.map { it.name ?: "Unknown" }
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

        // store binding in tag so save loop can read it later
        rowBinding.root.tag = rowBinding

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
        if (product == null) {
            addMaterialRow(dialogBinding.layoutMaterialsContainer)
        } else {
            fillProductDetails(product, dialogBinding)
        }

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

            // Add Mode
            if (product == null) {
                productsRepo.insert(Products(null, name, selectedImageBytes))
                showCustomToast(this, "Product added successfully")
                return@setOnClickListener
            }

            // Update Mode
            val updated = product.copy(name = name, image = selectedImageBytes ?: product.image)
            val success = productsRepo.update(updated)

            if(!success)
            {
                showCustomToast(this, "Update failed")
                return@setOnClickListener
            }

            RecyclerUtils.updateItem(productList, updated, adapter) { it.id }
            showCustomToast(this, "Product updated successfully")

            syncProductMaterials(product.id!!, dialogBinding)

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

    private fun fillProductDetails(product: Products, dialogBinding: DialogAddProductBinding) {
        dialogBinding.etProductName.setText(product.name)

        product.image?.let { bytes ->
            dialogBinding.ivProductImage.setImageBitmap(
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            )
        }

        val usedMaterials = productMaterialsRepo.getMaterialsPerProduct(product.id!!)

        if (usedMaterials.isEmpty()) {
            addMaterialRow(dialogBinding.layoutMaterialsContainer)
            return
        }

        usedMaterials.forEach { pm ->
            addMaterialRow(dialogBinding.layoutMaterialsContainer)

            val row = dialogBinding.layoutMaterialsContainer.getChildAt(
                dialogBinding.layoutMaterialsContainer.childCount - 1
            )
            val rowBinding = (row.tag as? MaterialRowBinding) ?: MaterialRowBinding.bind(row)

            rowBinding.etMaterialQuantity.setText(pm.quantityRequired.toString())

            val materials = materialsRepo.getAll()
            val index = materials.indexOfFirst { it.id == pm.materialId }
            if (index != -1) rowBinding.sAvailableMaterials.setSelection(index)

            rowBinding.tvUnit.text = pm.materialUnit
        }
    }

    private fun syncProductMaterials(productId: Int, dialogBinding: DialogAddProductBinding) {

        // 1️. Get OLD materials from DB
        val oldMaterials = productMaterialsRepo.getMaterialsPerProduct(productId)

        // 2. Gather NEW materials from the UI
        val newMaterials = mutableListOf<ProductMaterials>()

        for (i in 0 until dialogBinding.layoutMaterialsContainer.childCount) {
            val row = dialogBinding.layoutMaterialsContainer.getChildAt(i)
            val rowBinding = (row.tag as? MaterialRowBinding) ?: MaterialRowBinding.bind(row)

            val selectedName = rowBinding.sAvailableMaterials.selectedItem as String
            val qty = rowBinding.etMaterialQuantity.text.toString().trim().toDoubleOrNull() ?: 0.0

            val material = materialsRepo.getAll().first { it.name == selectedName }

            newMaterials.add(
                ProductMaterials(
                    id = null,
                    productId = productId,
                    materialId = material.id,
                    quantityRequired = qty
                )
            )
        }

        // 3. Compare OLD vs NEW
        val toInsert = mutableListOf<ProductMaterials>()
        val toUpdate = mutableListOf<ProductMaterials>()
        val toDelete = mutableListOf<Int>()

        for (newMat in newMaterials) {
            val existing = oldMaterials.firstOrNull { it.materialId == newMat.materialId }

            if (existing == null) {
                toInsert.add(newMat)
                continue
            }
            toUpdate.add(
                ProductMaterials(
                    id = existing.productMaterialId,
                    productId = productId,
                    materialId = existing.materialId,
                    quantityRequired = newMat.quantityRequired
                )
            )
        }

        oldMaterials.forEach { oldMat ->
            if (newMaterials.none { it.materialId == oldMat.materialId }) {
                toDelete.add(oldMat.materialId)
            }
        }

        // 4. Apply DB operations
        toInsert.forEach { productMaterialsRepo.insert(it) }
        toUpdate.forEach { productMaterialsRepo.update(it) }
        toDelete.forEach { productMaterialsRepo.deleteColumn(it) }
    }
}
