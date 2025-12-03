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
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.colarsort.app.R
import com.colarsort.app.adapters.MaterialAdapter
import com.colarsort.app.databinding.ActivityMaterialsBinding
import com.colarsort.app.databinding.DialogAddMaterialBinding
import com.colarsort.app.models.Materials
import com.colarsort.app.repository.MaterialsRepo
import com.colarsort.app.repository.ProductMaterialsRepo
import com.colarsort.app.utils.UtilityHelper.compressBitmap
import com.colarsort.app.utils.RecyclerUtils
import com.colarsort.app.utils.UtilityHelper.showCustomToast


class MaterialsActivity : BaseActivity() {

    private lateinit var binding: ActivityMaterialsBinding
    private lateinit var materialsRepo: MaterialsRepo
    private lateinit var productMaterialsRepo: ProductMaterialsRepo
    private lateinit var adapter: MaterialAdapter
    private val materialList = ArrayList<Materials>()
    private var tempDialogImageView: ImageView? = null
    private var selectedImageBytes: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        materialsRepo = MaterialsRepo(dbHelper)
        productMaterialsRepo = ProductMaterialsRepo(dbHelper)

        // Setup view binding
        binding = ActivityMaterialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        adapter = MaterialAdapter(materialList)
        binding.recyclerViewMaterials.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMaterials.adapter = adapter

        RecyclerUtils.initialize(materialList,materialsRepo.getAll(), adapter)

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
            startActivity(Intent(this, ProductsActivity::class.java))
            finish()
        }
        binding.ivMaterials.setOnClickListener {
            showCustomToast(this, "You are already in Materials")
        }
        // Menu button
        binding.materialsMenu.setOnClickListener { view -> showPopupMenu(view) }

        // For searching materials
        binding.ivSearch.setOnClickListener {
            val strSearch = binding.etSearchField.text.toString().trim()

            val newList = if (strSearch.isEmpty()) {
                materialsRepo.getAll()
            } else {
                materialsRepo.searchMaterialBaseOnName(strSearch)
            }

            adapter = MaterialAdapter(ArrayList(newList))
            binding.recyclerViewMaterials.layoutManager = LinearLayoutManager(this)
            binding.recyclerViewMaterials.adapter = adapter
        }

        when(sessionManager.getRole())
        {
            "Worker" -> binding.btnAddMaterial.visibility = View.GONE
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
            selectedImageBytes = compressBitmap(this, bitmap)
        }
    }

    private fun handleManagerSession()
    {
        binding.btnAddMaterial.setOnClickListener { showAddMaterialDialog() }

        adapter.setUserRole("Manager")
        // Adapter item "more" click listener
        adapter.onItemMoreClickListener = { material: Materials, view: View ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.more_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
               handleProductMenuClick(material, menuItem.itemId)
            }
            popup.show()
        }
    }

    private fun handleProductMenuClick(material: Materials, menuItemId: Int): Boolean {

        if (menuItemId == R.id.edit_product) {
            showEditMaterialDialog(material)
            return true
        }

        if (menuItemId == R.id.delete_product) {

            if (productMaterialsRepo.isMaterialUsedInAnyOrder(material.id!!)) {
                showCustomToast(this, "Cannot delete material. It is used in products with active orders.")
                return true
            }

            AlertDialog.Builder(this)
                .setTitle("Delete Material")
                .setMessage("Are you sure you want to delete this material?")
                .setPositiveButton("Yes") { _, _ ->

                    productMaterialsRepo.deleteById(material.id, false)

                    val successful = materialsRepo.deleteColumn(material.id)
                    if (!successful) {
                        showCustomToast(this, "Delete failed")
                        return@setPositiveButton
                    }

                    val position = materialList.indexOf(material)
                    if (position != -1) {
                        RecyclerUtils.deleteAt(materialList, position, adapter)
                    }

                    showCustomToast(this, "Material deleted successfully")
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

            return true
        }

        return true
    }


    // Show add material dialog
    private fun showAddMaterialDialog() {
        selectedImageBytes = null

        val dialogBinding = DialogAddMaterialBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // Setup unit spinner like addMaterialRow
        val units = arrayOf("m", "pcs", "roll", "ft", "in", "yard")
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spMaterialUnit.adapter = unitAdapter

        // Change selected text color to white
        dialogBinding.spMaterialUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.WHITE)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Image picker
        dialogBinding.ivMaterialImage.setOnClickListener {
            tempDialogImageView = dialogBinding.ivMaterialImage
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        // Save button
        dialogBinding.tvSave.setOnClickListener {
            val name = dialogBinding.etMaterialName.text.toString().trim()
            val unit = dialogBinding.spMaterialUnit.selectedItem.toString()
            val quantity = dialogBinding.etMaterialQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val lowStockThreshold = dialogBinding.etLowStockThreshold.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isEmpty() || unit.isEmpty()) {
                showCustomToast(this, "Please fill in all fields")
                return@setOnClickListener
            }

            val material = Materials(null, name, quantity, unit, lowStockThreshold, selectedImageBytes)
            materialsRepo.insert(material)
            showCustomToast(this, "Material added successfully")

            RecyclerUtils.insertedItems(materialList, materialsRepo.getAll(), adapter)

            tempDialogImageView = null
            dialog.dismiss()
        }

        // Cancel button
        dialogBinding.tvCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // Show edit material dialog
    private fun showEditMaterialDialog(material: Materials?) {
        selectedImageBytes = material?.image

        // Inflate using binding
        val dialogBinding = DialogAddMaterialBinding.inflate(layoutInflater)

        // Set dialog title
        dialogBinding.tvAddMaterial.text = if (material == null) "Add Material" else "Edit Material"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // Setup unit spinner like addMaterialRow
        val units = arrayOf("m", "pcs", "roll", "ft", "in", "yard")
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spMaterialUnit.adapter = unitAdapter

        // Set spinner selected text color to white
        dialogBinding.spMaterialUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.WHITE)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Pre-select the current unit if editing
        material?.let {
            dialogBinding.etMaterialName.setText(it.name)
            val unitPosition = units.indexOf(it.unit).takeIf { pos -> pos >= 0 } ?: 0
            dialogBinding.spMaterialUnit.setSelection(unitPosition)
            dialogBinding.etMaterialQuantity.setText(it.quantity.toString())
            dialogBinding.etLowStockThreshold.setText(it.stockThreshold.toString())
            it.image?.let { path ->
                dialogBinding.ivMaterialImage.load(path)
            }
        }

        // Image picker
        dialogBinding.ivMaterialImage.setOnClickListener {
            tempDialogImageView = dialogBinding.ivMaterialImage
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        // Save button
        dialogBinding.tvSave.setOnClickListener {
            handleEditSaveButton(material, dialogBinding, dialog)
        }

        // Cancel button
        dialogBinding.tvCancel.setOnClickListener {
            tempDialogImageView = null
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun handleEditSaveButton(material: Materials?, dialogBinding: DialogAddMaterialBinding, dialog: Dialog)
    {
        val name: String? = dialogBinding.etMaterialName.text.toString().trim().ifEmpty { null }
        val quantity: Double? = dialogBinding.etMaterialQuantity.text.toString().toDoubleOrNull()?.takeIf { it != 0.0 }
        val unit: String? = dialogBinding.spMaterialUnit.selectedItem.toString().takeIf { it.isNotEmpty() }
        val threshold: Double? = dialogBinding.etLowStockThreshold.text.toString().toDoubleOrNull()?.takeIf { it != 0.0 }

        when {
            name == null -> {
                showCustomToast(this, "Invalid name. Please fill in the field.")
                return
            }
            quantity == null -> {
                showCustomToast(this, "Invalid quantity. Please fill in the field.")
                return
            }
            unit == null -> {
                showCustomToast(this, "Invalid unit. Please select a unit.")
                return
            }
            threshold == null -> {
                showCustomToast(this, "Invalid threshold. Please fill in the field.")
                return
            }
        }

        val materialData = Materials(
            material!!.id,
            name,
            quantity,
            unit,
            threshold,
            selectedImageBytes ?: material.image
        )
        val success = materialsRepo.update(materialData)

        tempDialogImageView = null
        dialog.dismiss()

        if (success) {
            RecyclerUtils.updateItem(materialList, materialData, adapter) { it.id }
            showCustomToast(this, "Material updated successfully")
        } else {
            showCustomToast(this, "Update failed")
        }
    }
}
