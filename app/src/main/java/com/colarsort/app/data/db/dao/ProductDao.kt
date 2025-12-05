package com.colarsort.app.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.colarsort.app.data.entities.Products

@Dao
interface ProductDao : BaseDao<Products>
{
    @Query("SELECT * FROM products;")
    suspend fun getAllData() : List<Products>

    @Query("SELECT COALESCE(MAX(id), 0) FROM products;")
    suspend fun getLatestId() : Int

    @Query("SELECT * FROM products WHERE id = :id;")
    suspend fun getDataById(id: Int) : Products

    @Query("SELECT * FROM products WHERE name LIKE :search;")
    suspend fun searchProductsByName(search : String) : List<Products>
}