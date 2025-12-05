package com.colarsort.app.data.repository

import com.colarsort.app.data.db.dao.ProductDao
import com.colarsort.app.data.entities.Products

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
}