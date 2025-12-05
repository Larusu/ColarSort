package com.colarsort.app.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.colarsort.app.data.entities.Materials
import com.colarsort.app.data.pojo.MaterialWithQuantity

@Dao
interface MaterialDao : BaseDao<Materials>
{
    @Query("SELECT * FROM materials;")
    suspend fun getAllData() : List<Materials>

    @Query("SELECT * FROM materials WHERE id IN (:ids);")
    suspend fun getMaterialsByIds(ids: List<Int>): List<Materials>

    @Query("SELECT * FROM materials WHERE id = :id;")
    suspend fun getDataById(id: Int) : Materials

    @Query("""
        SELECT m.id, m.quantity, pm.quantityRequired
        FROM materials m
        INNER JOIN product_material pm 
            ON pm.materialId = m.id
        WHERE pm.productId = :productId;
    """)
    suspend fun getMaterialsWithQuantityRequired(productId: Int): List<MaterialWithQuantity>

    @Query("UPDATE materials SET quantity = :quantity WHERE id = :id;")
    suspend fun updateQuantity(id: Int, quantity: Double)

    @Query("SELECT * FROM materials WHERE name LIKE :search;")
    suspend fun searchMaterialsByName(search : String) : List<Materials>
}