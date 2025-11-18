package com.colarsort.app.activities

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colarsort.app.R
import com.colarsort.app.adapters.ProductAdapter
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.databinding.ActivityProductsBinding
import com.colarsort.app.models.Products

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = binding.recyclerViewProducts

        val products = arrayListOf<Products>(
//            Products(1, "Product 1", null),
//            Products(2, "Product 2", null),
//            Products(3, "Product 3", null),
//            Products(4, "Product 4", null),
//            Products(5, "Product 5", null),
//            Products(6, "Product 6", null),
//            Products(7, "Product 7", null),
//            Products(8, "Product 8", null),
//            Products(9, "Product 9", null),
//            Products(10, "Product 10", null),
//            Products(11, "Product 11", null),
//            Products(12, "Product 12", null),
        )

        val adapter = ProductAdapter(products)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter

        val hamburgerMenu = binding.productMenu

        hamburgerMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.hamburger_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_product -> Toast.makeText(this, "Option 1 clicked", Toast.LENGTH_SHORT).show()
                R.id.update_product -> Toast.makeText(this, "Option 2 clicked", Toast.LENGTH_SHORT).show()
                R.id.remove_product -> Toast.makeText(this, "Option 3 clicked", Toast.LENGTH_SHORT).show()
            }
            true
        }

        popup.show()
    }

}