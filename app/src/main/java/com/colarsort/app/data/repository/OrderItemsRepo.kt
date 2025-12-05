package com.colarsort.app.data.repository

import com.colarsort.app.data.db.dao.OrderItemDao
import com.colarsort.app.data.entities.OrderItems

class OrderItemsRepo(dao: OrderItemDao) : BaseRepo<OrderItems, OrderItemDao>(dao)
{
    suspend fun getById(id: Int): OrderItems? = dao.getDataById(id)

    suspend fun getLastInsertedId() : Int = dao.getLatestId()
}