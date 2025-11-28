package com.colarsort.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colarsort.app.R
import com.colarsort.app.models.Materials
import com.colarsort.app.models.ProductMaterials
import com.colarsort.app.models.Products
import com.colarsort.app.repository.MaterialsRepo
import com.colarsort.app.repository.ProductMaterialsRepo
import com.colarsort.app.repository.ProductsRepo
import com.colarsort.app.utils.UtilityHelper.inputStreamToByteArray

class MainActivity : BaseActivity() {

    private lateinit var materialsRepo : MaterialsRepo
    private lateinit var productsRepo : ProductsRepo
    private lateinit var productMaterialsRepo : ProductMaterialsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        materialsRepo = MaterialsRepo(dbHelper)
        productsRepo = ProductsRepo(dbHelper)
        productMaterialsRepo = ProductMaterialsRepo(dbHelper)

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeData()

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun initializeData()
    {
        val existingMaterials = materialsRepo.getAll()
        if (existingMaterials.size < 4) {
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

        val existingProducts = productsRepo.getAll()
        if (existingProducts.size < 4) {
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

        val existingProductMaterials = productMaterialsRepo.getAll()

        val productIds = mapOf(
            "T-shirt" to 1,
            "Jeans" to 2,
            "Sweater" to 3,
            "Dress" to 4,
            "Shoes" to 5,
            "Hat" to 6,
            "Jacket" to 7,
            "Gloves" to 8,
            "Scarf" to 9
        )

        val materialIds = mapOf(
            "Cotton" to 1,
            "Canvas" to 2,
            "Polyester" to 3,
            "Thread Roll" to 4,
            "Button" to 5,
            "Zipper" to 6,
            "Elastic Band" to 7,
            "Velcro Strip" to 8
        )
        val productMaterials = arrayOf(
            // T-shirt
            ProductMaterials(null, productIds["T-shirt"]!!, materialIds["Cotton"]!!, 1.5),
            ProductMaterials(null, productIds["T-shirt"]!!, materialIds["Thread Roll"]!!, 0.2),

            // Jeans
            ProductMaterials(null, productIds["Jeans"]!!, materialIds["Canvas"]!!, 2.0),
            ProductMaterials(null, productIds["Jeans"]!!, materialIds["Thread Roll"]!!, 0.3),
            ProductMaterials(null, productIds["Jeans"]!!, materialIds["Button"]!!, 1.0),
            ProductMaterials(null, productIds["Jeans"]!!, materialIds["Zipper"]!!, 1.0),

            // Sweater
            ProductMaterials(null, productIds["Sweater"]!!, materialIds["Polyester"]!!, 1.8),
            ProductMaterials(null, productIds["Sweater"]!!, materialIds["Thread Roll"]!!, 0.2),

            // Dress
            ProductMaterials(null, productIds["Dress"]!!, materialIds["Cotton"]!!, 2.5),
            ProductMaterials(null, productIds["Dress"]!!, materialIds["Thread Roll"]!!, 0.3),
            ProductMaterials(null, productIds["Dress"]!!, materialIds["Button"]!!, 3.0),
            ProductMaterials(null, productIds["Dress"]!!, materialIds["Zipper"]!!, 1.0),

            // Shoes
            ProductMaterials(null, productIds["Shoes"]!!, materialIds["Canvas"]!!, 0.8),
            ProductMaterials(null, productIds["Shoes"]!!, materialIds["Elastic Band"]!!, 0.5),
            ProductMaterials(null, productIds["Shoes"]!!, materialIds["Velcro Strip"]!!, 0.5),

            // Hat
            ProductMaterials(null, productIds["Hat"]!!, materialIds["Cotton"]!!, 0.5),
            ProductMaterials(null, productIds["Hat"]!!, materialIds["Thread Roll"]!!, 0.1),

            // Jacket
            ProductMaterials(null, productIds["Jacket"]!!, materialIds["Canvas"]!!, 2.5),
            ProductMaterials(null, productIds["Jacket"]!!, materialIds["Polyester"]!!, 1.0),
            ProductMaterials(null, productIds["Jacket"]!!, materialIds["Zipper"]!!, 1.0),
            ProductMaterials(null, productIds["Jacket"]!!, materialIds["Thread Roll"]!!, 0.4),

            // Gloves
            ProductMaterials(null, productIds["Gloves"]!!, materialIds["Cotton"]!!, 0.2),
            ProductMaterials(null, productIds["Gloves"]!!, materialIds["Elastic Band"]!!, 0.2),

            // Scarf
            ProductMaterials(null, productIds["Scarf"]!!, materialIds["Polyester"]!!, 1.2),
            ProductMaterials(null, productIds["Scarf"]!!, materialIds["Thread Roll"]!!, 0.1)
        )

        if(existingProductMaterials.isEmpty()) {
            for (pm in productMaterials) {
                productMaterialsRepo.insert(pm)
            }
        }
    }
}