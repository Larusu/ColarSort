package com.colarsort.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.colarsort.app.data.entities.Orders

@Dao
interface OrderDao : BaseDao<Orders>
{
    @Insert
    suspend fun insertAndGetId(order: Orders) : Long

    @Query("SELECT * FROM orders WHERE id = :id;")
    suspend fun getDataById(id: Int) : Orders

    @Query("SELECT * FROM orders")
    suspend fun getAllData() : List<Orders>
}