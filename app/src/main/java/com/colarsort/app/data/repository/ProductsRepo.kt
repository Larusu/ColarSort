package com.colarsort.app.data.repository

import android.content.Context
import com.colarsort.app.data.db.dao.ProductDao
import com.colarsort.app.data.entities.Products
import com.colarsort.app.utils.UtilityHelper.compressBitmap
import com.colarsort.app.utils.UtilityHelper.loadBitmapFromAssets
import com.colarsort.app.utils.UtilityHelper.loadFromJson

class ProductsRepo(dao : ProductDao) : BaseRepo<Products, ProductDao>(dao)
{

    suspend fun getAll() : List<Products> = dao.getAllData()
    suspend fun getLastInsertedId() : Int = dao.getLatestId()
    suspend fun getById(id : Int): Products? = dao.getDataById(id)
    suspend fun searchProductBaseOnName(searchName : String) : List<Products> =
        dao.searchProductsByName("%$searchName%")

    suspend fun deleteById(id : Int) : Boolean
    {
        val product = dao.getDataById(id)
        val rowsDeleted = dao.delete(product)
        return rowsDeleted > 0
    }

    suspend fun insertInitialProducts(context: Context) {
        val items : List<Products> = loadFromJson(context, "products/products.json")

        items.forEach { json ->
            val bitmap = loadBitmapFromAssets(context, json.image!!)
            val filePath = compressBitmap(context, bitmap)

            val entity = Products(
                id = 0,
                name = json.name,
                image = filePath
            )
            dao.insert(entity)
        }
    }
}