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
import com.colarsort.app.utils.UtilityHelper.inputStreamToByteArray

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
        val existing = productsRepo.getAll()

        if(existing.size < 4)
        {
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
                        // TODO: edit and update product
                    }
                    R.id.delete_product -> {
                        val successful = productsRepo.deleteColumn(product.id!!)

                        if(!successful)
                        {
                            Toast.makeText(this, "Error deleting product", Toast.LENGTH_SHORT).show()
                        }

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
