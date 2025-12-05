package com.colarsort.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.colarsort.app.data.db.dao.MaterialDao
import com.colarsort.app.data.db.dao.OrderDao
import com.colarsort.app.data.db.dao.OrderItemDao
import com.colarsort.app.data.db.dao.ProductDao
import com.colarsort.app.data.db.dao.ProductMaterialDao
import com.colarsort.app.data.db.dao.ProductionStatusDao
import com.colarsort.app.data.db.dao.UserDao
import com.colarsort.app.data.entities.Materials
import com.colarsort.app.data.entities.OrderItems
import com.colarsort.app.data.entities.Orders
import com.colarsort.app.data.entities.ProductMaterials
import com.colarsort.app.data.entities.ProductionStatus
import com.colarsort.app.data.entities.Products
import com.colarsort.app.data.entities.Users

@Database(
    entities = [
        Users::class,
        Products::class,
        Materials::class,
        Orders::class,
        OrderItems::class,
        ProductionStatus::class,
        ProductMaterials::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun userDao() : UserDao
    abstract fun productDao() : ProductDao
    abstract fun materialDao() : MaterialDao
    abstract fun productMaterialDao() : ProductMaterialDao
    abstract fun orderDao() : OrderDao
    abstract fun orderItemDao() : OrderItemDao
    abstract fun productionStatusDao() : ProductionStatusDao
}
