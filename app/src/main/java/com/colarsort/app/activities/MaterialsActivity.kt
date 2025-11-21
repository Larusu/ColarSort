package com.colarsort.app.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.MaterialAdapter
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityMaterialsBinding
import com.colarsort.app.models.Materials
import com.colarsort.app.repository.MaterialsRepo
import android.widget.AutoCompleteTextView
import com.colarsort.app.utils.UtilityHelper.inputStreamToByteArray
import com.colarsort.app.utils.UtilityHelper.compressBitmap
import androidx.core.graphics.drawable.toDrawable


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

        dbHelper = DatabaseHelper(this)
        materialsRepo = MaterialsRepo(dbHelper)

        binding = ActivityMaterialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TEMPORARY MATERIAL CREATION
        val existing = materialsRepo.getAll()

        if(existing.size < 4)
        {
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

        // Set up RecyclerView
        adapter = MaterialAdapter(materialList)
        binding.recyclerViewMaterials.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMaterials.adapter = adapter

        // Load data from the database and update only the newly added items
        val existingSize = materialList.size
        val newItems = materialsRepo.getAll()
        materialList.addAll(newItems)

        adapter.notifyItemRangeInserted(existingSize, newItems.size)

        // Set up on click listeners
        binding.ivHome.setOnClickListener {
            // TODO: open home activity
        }

        binding.ivStatus.setOnClickListener {
            // TODO: open status activity
        }

        binding.ivOrders.setOnClickListener {
            // TODO: open orders activity
        }

        binding.ivProducts.setOnClickListener {
            val intent = Intent(this, ProductsActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.ivMaterials.setOnClickListener {
            Toast.makeText(this, "You are already in materials", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddMaterial.setOnClickListener {
            showAddMaterialDialog()
        }

        adapter.onItemMoreClickListener = { material: Materials, view: View ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.more_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit_product -> {
                        showMaterialDialog(material)
                    }

                    R.id.delete_product -> {
                        val successful = materialsRepo.deleteColumn(material.id!!)

                        if (!successful) {
                            Toast.makeText(this, "Error deleting material", Toast.LENGTH_SHORT)
                                .show()
                        }

                        val index = materialList.indexOf(material)
                        if (index != -1) {
                            materialList.removeAt(index)
                            adapter.notifyItemRemoved(index)
                        }
                    }
                }
                true
            }
            popup.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {

            val uri = data?.data ?: return
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            // show image inside dialog
            tempDialogImageView?.setImageBitmap(bitmap)

            // convert bitmap → small compressed bytes
            selectedImageBytes = compressBitmap(bitmap)
        }
    }

    // Functions
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.hamburger_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.log_out -> {
                    showLogoutDialog()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Log Out")
        builder.setMessage("Are you sure you want to log out?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            finish()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // Add ng material
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

            val material = Materials(
                id = null,
                name = name,
                quantity = quantity,
                unit = unit,
                stockThreshold = lowStockThreshold,
                image = selectedImageBytes
            )

            materialsRepo.insert(material)

            materialList.clear()
            materialList.addAll(materialsRepo.getAll())
            adapter.notifyDataSetChanged()

            tempDialogImageView = null
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // Edit ng material
    private fun showMaterialDialog(material: Materials? = null) {
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
            it.image?.let { bytes ->
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                dialogImageView.setImageBitmap(bmp)
            }
        }

        // Image picker
        dialogImageView.setOnClickListener {
            tempDialogImageView = dialogImageView
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val unit = etUnit.text.toString().trim()
            val quantity = etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val lowStockThreshold = etLowStockThreshold.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isEmpty() || unit.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (material == null) {
                // Add new material
                val newMaterial = Materials(
                    id = null,
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    stockThreshold = lowStockThreshold,
                    image = selectedImageBytes
                )
                materialsRepo.insert(newMaterial)
            } else {
                // Update existing material
                val updatedMaterial = material.copy(
                    id = material.id,
                    name = name,
                    unit = unit,
                    quantity = quantity,
                    stockThreshold = lowStockThreshold,
                    image = selectedImageBytes ?: material.image
                )

                val success = materialsRepo.update(updatedMaterial)
                if (success) {
                    Toast.makeText(this, "Material updated successfully", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Refresh RecyclerView
            materialList.clear()
            materialList.addAll(materialsRepo.getAll())
            adapter.notifyDataSetChanged()

            tempDialogImageView = null
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            tempDialogImageView = null
            dialog.dismiss()
        }

        dialog.show()
    }


}