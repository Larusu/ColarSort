package com.colarsort.app.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.colarsort.app.data.entities.OrderItems

@Dao
interface OrderItemDao : BaseDao<OrderItems>
{
    @Query("SELECT * FROM order_items;")
    suspend fun getAllData() : List<OrderItems>

    @Query("SELECT * FROM order_items WHERE id = :id;")
    suspend fun getDataById(id: Int) : OrderItems

    @Query("SELECT COALESCE(MAX(id), 0) FROM order_items;")
    suspend fun getLatestId() : Int
}