package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.OrderItemsTable
import com.colarsort.app.models.OrderItems

class OrderItemsRepo(dbHelper: DatabaseHelper) : CRUDRepo<OrderItems>(dbHelper)
{
    override val tableName: String = OrderItemsTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(
        OrderItemsTable.ID,
        OrderItemsTable.ORDER_ID,
        OrderItemsTable.PRODUCT_ID,
        OrderItemsTable.QUANTITY
    )

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

    fun getById(id: Int): OrderItems? {
        val list = fetchList("SELECT * FROM $tableName WHERE id = $id")
        return list.firstOrNull()
    }

    fun getLastInsertedId() : Int
    {
        val db = dbHelper.writableDatabase
        var latestId : Int = -1
        val cursor = db.rawQuery("SELECT MAX(${tableRows[0]}) FROM $tableName", null)

        cursor.use {
            if(it.moveToFirst()) latestId = it.getLong(0).toInt()
        }

        db.close()
        return latestId
    }

    fun hasOrdersForProduct(productId: Int): Boolean {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${OrderItemsTable.TABLE_NAME} WHERE ${OrderItemsTable.PRODUCT_ID} = ?",
            arrayOf(productId.toString())
        )

        cursor.use {
            return if (it.moveToFirst()) it.getInt(0) > 0 else false
        }
    }

}