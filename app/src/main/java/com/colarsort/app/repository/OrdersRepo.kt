package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.OrdersTable
import com.colarsort.app.models.Orders

class OrdersRepo(dbHelper: DatabaseHelper) : CRUDRepo<Orders>(dbHelper)
{
    override val tableName: String = OrdersTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(
        OrdersTable.CUSTOMER_NAME,
        OrdersTable.STATUS,
        OrdersTable.EXPECTED_DELIVERY
    )
    override val idName: String = OrdersTable.ID

    override fun converter(cursor: Cursor): Orders {
        return Orders(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(OrdersTable.ID)),
            customerName = cursor.getString(cursor.getColumnIndexOrThrow(OrdersTable.CUSTOMER_NAME)),
            status = cursor.getString(cursor.getColumnIndexOrThrow(OrdersTable.STATUS)),
            expectedDelivery = cursor.getString(cursor.getColumnIndexOrThrow(OrdersTable.EXPECTED_DELIVERY))
        )
    }
}