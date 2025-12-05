package com.colarsort.app.data.repository

import com.colarsort.app.data.db.dao.ProductionStatusDao
import com.colarsort.app.data.entities.ProductionStatus

class ProductionStatusRepo(dao: ProductionStatusDao) : BaseRepo<ProductionStatus, ProductionStatusDao>(dao)
{
    suspend fun getAll(): List<ProductionStatus> = dao.getAllData()

    suspend fun deleteById(id : Int) = dao.deleteById(id)
}