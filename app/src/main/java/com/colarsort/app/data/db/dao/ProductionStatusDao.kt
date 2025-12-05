package com.colarsort.app.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.colarsort.app.data.entities.ProductionStatus

@Dao
interface ProductionStatusDao : BaseDao<ProductionStatus>
{
    @Query("SELECT * FROM production_status")
    suspend fun getAllData() : List<ProductionStatus>

    @Query("DELETE FROM production_status WHERE id = :id")
    suspend fun deleteById(id: Int)
}