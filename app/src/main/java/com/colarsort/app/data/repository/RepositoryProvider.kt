package com.colarsort.app.data.repository

import com.colarsort.app.App
object RepositoryProvider
{
    private val database get() = App.database
    val usersRepo by lazy { UsersRepo(database.userDao()) }
    val productsRepo by lazy { ProductsRepo(database.productDao()) }

    val materialsRepo by lazy { MaterialsRepo(database.materialDao()) }

    val ordersRepo by lazy { OrdersRepo(database.orderDao()) }

    val productMaterialsRepo by lazy { ProductMaterialsRepo(database.productMaterialDao()) }

    val orderItemRepo by lazy { OrderItemsRepo(database.orderItemDao()) }
    val productionStatusRepo by lazy { ProductionStatusRepo(database.productionStatusDao()) }
}