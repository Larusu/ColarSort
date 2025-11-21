package com.colarsort.app.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.MaterialAdapter
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityMaterialsBinding
import com.colarsort.app.models.Materials
import com.colarsort.app.repository.MaterialsRepo
import com.colarsort.app.utils.UtilityHelper.compressBitmap
import com.colarsort.app.utils.UtilityHelper.inputStreamToByteArray
import com.colarsort.app.utils.RecyclerUtils


class MaterialsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaterialsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var materialsRepo: MaterialsRepo
    private lateinit var adapter: MaterialAdapter
    private val materialList = ArrayList<Materials>()
    private var tempDialogImageView: ImageView? = null
    private var selectedImageBytes: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database and repository
        dbHelper = DatabaseHelper(this)
        materialsRepo = MaterialsRepo(dbHelper)

        // Setup view binding
        binding = ActivityMaterialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Temporary material creation
        val existing = materialsRepo.getAll()
        if (existing.size < 4) {
            val material = arrayOf(
                Materials(null, "Cotton", 100.0, "m", 10.0, inputStreamToByteArray(this, "materials/cotton_fabric.jpg")),
                Materials(null, "Canvas", 200.0, "m", 15.0, inputStreamToByteArray(this, "materials/canvas_fabric.jpg")),
                Materials(null, "Polyester", 150.0, "m", 12.0, inputStreamToByteArray(this, "materials/polyester_fabric.jpg")),
                Materials(null, "Thread Roll", 20.0, "roll", 5.0, inputStreamToByteArray(this, "materials/thread_roll.jpg")),
                Materials(null, "Button", 300.0, "pcs", 20.0, inputStreamToByteArray(this, "materials/button.jpg")),
                Materials(null, "Zipper", 100.0, "pcs", 20.0, inputStreamToByteArray(this, "materials/zipper.jpg")),
                Materials(null, "Elastic Band", 80.0, "m", 5.0, inputStreamToByteArray(this, "materials/elastic_band.jpg")),
                Materials(null, "Velcro Strip", 120.0, "m", 10.0, inputStreamToByteArray(this, "materials/velcro_strip.jpg"))
            )
            material.forEach { m -> materialsRepo.insert(m) }
        }

        // Setup RecyclerView
        adapter = MaterialAdapter(materialList)
        binding.recyclerViewMaterials.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMaterials.adapter = adapter

        RecyclerUtils.initialize(materialList,materialsRepo.getAll(), adapter)

        // Navigation click listeners
        binding.ivHome.setOnClickListener { /* TODO: open home activity */ }
        binding.ivStatus.setOnClickListener { /* TODO: open status activity */ }
        binding.ivOrders.setOnClickListener { /* TODO: open orders activity */ }
        binding.ivProducts.setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
            finish()
        }
        binding.ivMaterials.setOnClickListener {
            Toast.makeText(this, "You are already in materials", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddMaterial.setOnClickListener { showAddMaterialDialog() }

        // Adapter item "more" click listener
        adapter.onItemMoreClickListener = { material: Materials, view: View ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.more_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit_product -> showEditMaterialDialog(material)
                    R.id.delete_product -> {
                        val successful = materialsRepo.deleteColumn(material.id!!)
                        if (!successful) Toast.makeText(this, "Error deleting material", Toast.LENGTH_SHORT).show()

                        val position = materialList.indexOf(material)
                        RecyclerUtils.deleteAt(materialList, position, adapter)
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
            tempDialogImageView?.setImageBitmap(bitmap)
            selectedImageBytes = compressBitmap(bitmap)
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

    // Show add material dialog
    private fun showAddMaterialDialog() {
        selectedImageBytes = null

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_material, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_material_name)
        val etUnit = dialogView.findViewById<AutoCompleteTextView>(R.id.et_material_unit)
        val etQuantity = dialogView.findViewById<EditText>(R.id.et_material_quantity)
        val etLowStockThreshold = dialogView.findViewById<EditText>(R.id.et_low_stock_threshold)
        val btnAdd = dialogView.findViewById<TextView>(R.id.tv_save)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val dialogImageView = dialogView.findViewById<ImageView>(R.id.iv_material_image)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val units = arrayOf("m", "pcs", "roll", "ft", "in", "yard")
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, units)
        etUnit.setAdapter(unitAdapter)
        etUnit.setOnClickListener { etUnit.showDropDown() }

        dialogImageView.setOnClickListener {
            tempDialogImageView = dialogImageView
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val unit = etUnit.text.toString().trim()
            val quantity = etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val lowStockThreshold = etLowStockThreshold.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isEmpty() || unit.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val material = Materials(null, name, quantity, unit, lowStockThreshold, selectedImageBytes)
            materialsRepo.insert(material)

            RecyclerUtils.insertedItems(materialList, materialsRepo.getAll(), adapter)

            tempDialogImageView = null
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // Show edit material dialog
    private fun showEditMaterialDialog(material: Materials?) {
        selectedImageBytes = material?.image

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_material, null)
        val tvAddMaterial = dialogView.findViewById<TextView>(R.id.tv_add_material)
        val etName = dialogView.findViewById<EditText>(R.id.et_material_name)
        val etUnit = dialogView.findViewById<AutoCompleteTextView>(R.id.et_material_unit)
        val etQuantity = dialogView.findViewById<EditText>(R.id.et_material_quantity)
        val etLowStockThreshold = dialogView.findViewById<EditText>(R.id.et_low_stock_threshold)
        val btnSave = dialogView.findViewById<TextView>(R.id.tv_save)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val dialogImageView = dialogView.findViewById<ImageView>(R.id.iv_material_image)

        tvAddMaterial.text = if (material == null) "Add Material" else "Edit Material"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val units = arrayOf("m", "pcs", "roll", "ft", "in", "yard")
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, units)
        etUnit.setAdapter(unitAdapter)
        etUnit.setOnClickListener { etUnit.showDropDown() }

        // Pre-fill fields if editing
        material?.let {
            etName.setText(it.name)
            etUnit.setText(it.unit)
            etQuantity.setText(it.quantity.toString())
            etLowStockThreshold.setText(it.stockThreshold.toString())
            it.image?.let { bytes -> dialogImageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)) }
        }

        dialogImageView.setOnClickListener {
            tempDialogImageView = dialogImageView
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        btnSave.setOnClickListener {
            val name: String? = etName.text.toString().trim().ifEmpty { null }
            val quantity: Double? = etQuantity.text.toString().toDoubleOrNull()?.takeIf { it != 0.0 }
            val unit: String? = etUnit.text.toString().trim().ifEmpty { null }
            val threshold: Double? = etLowStockThreshold.text.toString().toDoubleOrNull()?.takeIf { it != 0.0 }

            val materialData = Materials(material!!.id, name,  quantity, unit, threshold, selectedImageBytes?: material.image)
            val success =  materialsRepo.update(materialData)

            tempDialogImageView = null
            dialog.dismiss()

            if (success) {
                RecyclerUtils.updateItem(materialList, materialData, adapter) {it.id}
                Toast.makeText(this, "Material updated successfully", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        btnCancel.setOnClickListener {
            tempDialogImageView = null
            dialog.dismiss()
        }

        dialog.show()
    }
}
