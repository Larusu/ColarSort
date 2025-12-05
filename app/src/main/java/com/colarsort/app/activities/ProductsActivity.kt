package com.colarsort.app.activities

import android.app.Dialog
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductAdapter
import com.colarsort.app.databinding.ActivityProductsBinding
import com.colarsort.app.databinding.DialogAddProductBinding
import com.colarsort.app.databinding.MaterialRowBinding
import com.colarsort.app.data.entities.ProductMaterials
import com.colarsort.app.data.entities.Products
import com.colarsort.app.data.repository.MaterialsRepo
import com.colarsort.app.data.repository.ProductMaterialsRepo
import com.colarsort.app.data.repository.ProductsRepo
import com.colarsort.app.data.repository.RepositoryProvider
import com.colarsort.app.utils.RecyclerUtils
import com.colarsort.app.utils.UtilityHelper.compressBitmap
import com.colarsort.app.utils.UtilityHelper.showCustomToast
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ProductsActivity : BaseActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var adapter: ProductAdapter
    private lateinit var productsRepo: ProductsRepo
    private lateinit var materialsRepo: MaterialsRepo
    private lateinit var productMaterialsRepo: ProductMaterialsRepo
    private val productList = ArrayList<Products>()
    private var tempDialogImageView: ImageView? = null
    private var selectedImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        productsRepo = RepositoryProvider.productsRepo
        materialsRepo = RepositoryProvider.materialsRepo
        productMaterialsRepo = RepositoryProvider.productMaterialsRepo

        // Setup view binding
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        adapter = ProductAdapter(productList)
        binding.recyclerViewProducts.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerViewProducts.adapter = adapter

        // Load data from database
        lifecycleScope.launch {
            val existingSize = productList.size
            val newItems = runIO { productsRepo.getAll() }
            productList.addAll(newItems)
            adapter.notifyItemRangeInserted(existingSize, newItems.size)
        }

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

            lifecycleScope.launch {
                val newList = if (strSearch.isEmpty()) {
                    runIO { productsRepo.getAll() }
                } else {
                    runIO { productsRepo.searchProductBaseOnName(strSearch) }
                }
                adapter = ProductAdapter(ArrayList(newList))
                binding.recyclerViewProducts.layoutManager = GridLayoutManager(this@ProductsActivity, 3)
                binding.recyclerViewProducts.adapter = adapter
            }
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
            tempDialogImageView?.setImageBitmap(bitmap)
            selectedImage = compressBitmap(this, bitmap)
        }
    }

    private fun handleManagerSession()
    {
        binding.btnAdd.setOnClickListener {
            lifecycleScope.launch {
                if (runIO{ materialsRepo.getAll().isEmpty() }) {
                    showCustomToast(this@ProductsActivity, "No materials found. Add a material first to continue")
                    return@launch
                }
                showAddProductDialog()
            }
            return@setOnClickListener
        }

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

        if(menuItemId == R.id.edit_product){
            showEditProductDialog(product)
        }

        if(menuItemId == R.id.delete_product) {
            lifecycleScope.launch {
                if (runIO{productMaterialsRepo.isProductUsedInAnyOrder(product.id)}) {
                    showCustomToast(this@ProductsActivity, "Cannot delete product with active orders")
                    return@launch
                }
                AlertDialog.Builder(this@ProductsActivity)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete this product?")
                    .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        // Delete related product materials
                        runIO { productMaterialsRepo.deleteByProductOrMaterialId(product.id) }

                        // Delete product itself
                        val successful = runIO { productsRepo.deleteById(product.id) }
                        if (!successful) {
                            showCustomToast(this@ProductsActivity, "Delete failed")
                            return@launch
                        }

                        // Update UI
                        val position = productList.indexOf(product)
                        if (position != -1) {
                            RecyclerUtils.deleteAt(productList, position, adapter)
                        }

                        showCustomToast(this@ProductsActivity, "Product deleted successfully")
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            }
            return true
        }

        return false
    }

    // Show Add Product dialog
    private fun showAddProductDialog() {
        selectedImage = null

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

            onImagePicked = { uri ->
                tempDialogImageView?.setImageURI(uri)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                selectedImage = compressBitmap(this, bitmap)
            }
            openImagePicker()
        }

        // Save button
        dialogBinding.tvSave.setOnClickListener {
            lifecycleScope.launch {
                handleSaveButton(
                    dialogBinding = dialogBinding,
                    layoutMaterialsContainer = dialogBinding.layoutMaterialsContainer,
                    dialog = dialog,
                    selectedImageBytes = selectedImage
                )
            }
        }

        // Cancel button
        dialogBinding.tvCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private suspend fun handleSaveButton(
        dialogBinding: DialogAddProductBinding,
        layoutMaterialsContainer: LinearLayout,
        dialog: Dialog,
        selectedImageBytes: String?
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
            val rowBinding = row.getTag(R.id.tag_binding) as MaterialRowBinding

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
            val material = runIO { materialsRepo.getAll().firstOrNull { it.name == materialName } }

            if (material?.id == null) {
                showCustomToast(this, "Material $materialName not found")
                return
            }

            selectedMaterials.add(material.id to quantity)
        }

        // Check for duplicate materials
        val duplicates = selectedMaterials.map { it.first }.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (duplicates.isNotEmpty()) {
            val duplicateNames = duplicates.mapNotNull { id -> runIO{ materialsRepo.getAll().firstOrNull { it.id == id }?.name } }
            showCustomToast(this, "Duplicate materials found: ${duplicateNames.joinToString()}")
            return
        }

        // Insert the product first
        val product = Products(id = 0, name, selectedImageBytes)
        runIO { productsRepo.insert(product) }

        // Get the last inserted product ID
        val productId = runIO { productsRepo.getLastInsertedId() }

        // Insert into ProductMaterials table
        selectedMaterials.forEach { (materialId, quantityRequired) ->
            val productMaterial =
                ProductMaterials(id = 0, productId, materialId, quantityRequired)
            runIO { productMaterialsRepo.insert(productMaterial) }
        }

        showCustomToast(this, "Product added successfully")
        RecyclerUtils.insertedItems(productList, runIO { productsRepo.getAll() }, adapter)

        tempDialogImageView = null
        dialog.dismiss()
    }

    private fun addMaterialRow(container: LinearLayout,
                               materialIdToSelect: Int? = null,
                               quantity: Double? = null,
                               productMaterialId: Int? = null
    ) {
        val rowBinding = MaterialRowBinding.inflate(layoutInflater, container, false)

        // Put materials in the spinner
        lifecycleScope.launch {
            val materials = runIO { materialsRepo.getAll() }

            if (materials.isEmpty()) {
                showCustomToast(this@ProductsActivity, "No materials available. Please add materials first.")
                return@launch
            }

            val materialNames = materials.map { it.name }
            val adapter = ArrayAdapter(
                this@ProductsActivity,
                android.R.layout.simple_spinner_item,
                materialNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            rowBinding.sAvailableMaterials.adapter = adapter

            materialIdToSelect?.let { id ->
                val index = materials.indexOfFirst { it.id == id }
                if (index != -1) {
                    rowBinding.sAvailableMaterials.setSelection(index)
                    rowBinding.tvUnit.text = materials[index].unit
                }
            }

            quantity?.let {
                rowBinding.etMaterialQuantity.setText(it.toString())
            }

            // Update unit when material selected
            rowBinding.sAvailableMaterials.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        (view as? TextView)?.setTextColor(Color.WHITE) // Selected text white
                        rowBinding.tvUnit.text = materials[position].unit
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        }

        // Remove row
        rowBinding.btnRemoveRow.setOnClickListener {
            container.removeView(rowBinding.root)
        }

        // store PM ID
        rowBinding.root.setTag(R.id.tag_pm_id, productMaterialId)

        // store Binding
        rowBinding.root.setTag(R.id.tag_binding, rowBinding)

        // Add the row to the container
        container.addView(rowBinding.root)
    }

    //  Show Edit Product dialog
    private fun showEditProductDialog(product: Products? = null) {
        selectedImage = product?.image

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

        dialogBinding.ivProductImage.setOnClickListener {
            tempDialogImageView = dialogBinding.ivProductImage

            onImagePicked = { uri ->
                tempDialogImageView?.setImageURI(uri)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                selectedImage = compressBitmap(this, bitmap)
            }
            openImagePicker()
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

            lifecycleScope.launch {
                // Add Mode
                if (product == null) {
                    runIO { productsRepo.insert(Products(id = 0, name, selectedImage)) }
                    showCustomToast(this@ProductsActivity, "Product added successfully")
                    return@launch
                }

                // Update Mode
                val updated = product.copy(name = name, image = selectedImage ?: product.image)
                val success = runIO { productsRepo.update(updated) }

                if (!success) {
                    showCustomToast(this@ProductsActivity, "Update failed")
                    return@launch
                }

                RecyclerUtils.updateItem(productList, updated, adapter) { it.id }
                showCustomToast(this@ProductsActivity, "Product updated successfully")

                // Update product materials
                syncProductMaterials(product.id, dialogBinding)

                tempDialogImageView = null
                dialog.dismiss()
            }
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

        product.image?.let { path ->
            dialogBinding.ivProductImage.load(path)
        }

        lifecycleScope.launch {
            val usedMaterials = runIO { productMaterialsRepo.getMaterialsPerProduct(product.id) }

            // update UI
            if (usedMaterials.isEmpty()) {
                addMaterialRow(dialogBinding.layoutMaterialsContainer)
            } else {
                usedMaterials.forEach { pm ->
                    addMaterialRow(
                        dialogBinding.layoutMaterialsContainer,
                        materialIdToSelect = pm.materialId,
                        quantity = pm.quantityRequired,
                        productMaterialId = pm.productMaterialId
                    )
                }
            }
        }
    }

    private suspend fun syncProductMaterials(productId: Int, dialogBinding: DialogAddProductBinding) {
        val unchangedMaterialIds = mutableSetOf<Int>()

        // 1️. Get OLD materials from DB
        val oldMaterials = runIO { productMaterialsRepo.getMaterialsPerProduct(productId) }

        val oldIds: MutableSet<Int> = try {
            oldMaterials.map { it.productMaterialId }.toMutableSet()
        } catch (e: Exception) {
            mutableSetOf()
        }

        val idsInUI = mutableSetOf<Int>()
        val toInsert = mutableListOf<ProductMaterials>()
        val toUpdate = mutableListOf<ProductMaterials>()

        val materials = runIO { materialsRepo.getAll() }

        // 2. Gather NEW materials from the UI
        for (i in 0 until dialogBinding.layoutMaterialsContainer.childCount) {
            val row = dialogBinding.layoutMaterialsContainer.getChildAt(i)
            val rowBinding = row.getTag(R.id.tag_binding) as MaterialRowBinding

            val selectedName = rowBinding.sAvailableMaterials.selectedItem as String
            val qtyText = rowBinding.etMaterialQuantity.text.toString().trim()

            if (qtyText.isEmpty()) return

            if (qtyText.isEmpty() || qtyText == "0") {
                val material = runIO { materialsRepo.getAll().first { it.name == selectedName } }
                unchangedMaterialIds.add(material.id)
                continue
            }

            val qty = qtyText.toDoubleOrNull() ?: continue

            val material = materials.firstOrNull { it.name == selectedName } ?: continue
            val materialId = material.id

            val pmId = row.getTag(R.id.tag_pm_id) as? Int
            if (pmId != null) {
                // existing DB row -> update
                idsInUI.add(pmId)
                toUpdate.add(
                    ProductMaterials(
                        id = pmId,
                        productId = productId,
                        materialId = materialId,
                        quantityRequired = qty
                    )
                )
            } else {
                // new row -> insert
                toInsert.add(
                    ProductMaterials(
                        id = 0,
                        productId = productId,
                        materialId = materialId,
                        quantityRequired = qty
                    )
                )
            }
        }

        // 3. Compare OLD vs NEW
        val toDelete = (oldIds - idsInUI).toList()

        // 4) Apply DB operations (wrap each in runIO)
        toInsert.forEach { runIO { productMaterialsRepo.insert(it) } }
        toUpdate.forEach { runIO { productMaterialsRepo.update(it) } }
        toDelete.forEach { id -> runIO { productMaterialsRepo.deleteColumn(id) } }
    }
}