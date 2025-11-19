package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
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

class MaterialsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaterialsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var materialsRepo: MaterialsRepo
    private lateinit var adapter: MaterialAdapter
    private val materialList = ArrayList<Materials>()

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
        materialList.add(Materials(1, "Cotton", 100.0, "m", 10.0))
        materialList.add(Materials(2, "Silk", 50.0, "m", 5.0))
        materialList.add(Materials(3, "Wool", 75.0, "m", 8.0))
        materialList.add(Materials(4, "Linen", 60.0, "m", 7.0))
        materialList.add(Materials(5, "Denim", 90.0, "m", 12.0))
        materialList.add(Materials(6, "Cashmere", 40.0, "m", 4.0))
        materialList.add(Materials(7, "Satin", 30.0, "m", 3.0))
        materialList.add(Materials(8, "Velvet", 20.0, "m", 2.0))

        // Set up RecyclerView
        adapter = MaterialAdapter(materialList)
        binding.recyclerViewMaterials.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMaterials.adapter = adapter

        // Load data from the database
        materialList.addAll(materialsRepo.getAll())
        adapter.notifyDataSetChanged()

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

        binding.materialsMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }

        binding.btnAdd.setOnClickListener {
            TODO("Not yet implemented")
        }
    }

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

}