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
import androidx.recyclerview.widget.GridLayoutManager
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductAdapter
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityProductsBinding
import com.colarsort.app.models.Products
import com.colarsort.app.repository.ProductsRepo

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var productsRepo: ProductsRepo
    private lateinit var adapter: ProductAdapter
    private val productList = ArrayList<Products>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dbHelper = DatabaseHelper(this)
        productsRepo = ProductsRepo(dbHelper)

        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TEMPORARY PRODUCT CREATION
        productList.add(Products(1, "T-shirt", null))
        productList.add(Products(2, "Jeans", null))
        productList.add(Products(3, "Sweater", null))
        productList.add(Products(4, "Dress", null))
        productList.add(Products(5, "Shoes", null))
        productList.add(Products(6, "Hat", null))
        productList.add(Products(7, "Jacket", null))
        productList.add(Products(8, "Gloves", null))
        productList.add(Products(9, "Scarf", null))

        // Set up RecyclerView
        adapter = ProductAdapter(productList)
        binding.recyclerViewProducts.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerViewProducts.adapter = adapter

        // Load data from the database and update only the newly added items
        val existingSize = productList.size
        val newItems = productsRepo.getAll()
        productList.addAll(newItems)

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
            Toast.makeText(this, "You are already in products", Toast.LENGTH_SHORT).show()
        }

        binding.ivMaterials.setOnClickListener {
            val intent = Intent(this, MaterialsActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.productMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }

        binding.btnAdd.setOnClickListener {

        }

        adapter.onItemMoreClickListener = { product: Products, view: View ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.more_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit_product -> {
                        // TODO: open edit dialog or activity
                    }
                    R.id.delete_product -> {
                        productsRepo.deleteColumn(product.id!!)

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
