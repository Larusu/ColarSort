package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.OrderItemsTable
import com.colarsort.app.models.OrderItems

class OrderItemsRepo(dbHelper: DatabaseHelper) : CRUDRepo<OrderItems>(dbHelper)
{
    override val tableName: String = OrderItemsTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(
        OrderItemsTable.ORDER_ID,
        OrderItemsTable.PRODUCT_ID,
        OrderItemsTable.QUANTITY
    )
    override val idName: String = OrderItemsTable.ID

    override fun converter(cursor: Cursor): OrderItems {
        return OrderItems(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(OrderItemsTable.ID)),
            orderId = cursor.getInt(cursor.getColumnIndexOrThrow(OrderItemsTable.ORDER_ID)),
            productId = cursor.getInt(cursor.getColumnIndexOrThrow(OrderItemsTable.PRODUCT_ID)),
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow(OrderItemsTable.QUANTITY))
        )
    }

    override fun getAll(): List<OrderItems> {
        // not specified yet
        return super.getAll()
    }
}