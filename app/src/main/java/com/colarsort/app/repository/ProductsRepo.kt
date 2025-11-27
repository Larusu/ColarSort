package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.ProductsTable
import com.colarsort.app.models.Products

class ProductsRepo(dbHelper: DatabaseHelper) : CRUDRepo<Products>(dbHelper)
{
    override val tableName: String = ProductsTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(
        ProductsTable.ID,
        ProductsTable.NAME,
        ProductsTable.IMAGE
    )

    override fun converter(cursor: Cursor): Products
    {
        return Products(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductsTable.ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(ProductsTable.NAME)),
            image = cursor.getBlob(cursor.getColumnIndexOrThrow(ProductsTable.IMAGE))
        )
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

    fun getById(id: Int): Products?
    {
        val list = fetchList("SELECT * FROM $tableName WHERE id = $id")
        return list.firstOrNull()
    }

    fun searchProductBaseOnName(searchName : String) : List<Products>
    {
        val db = dbHelper.readableDatabase
        val newList = mutableListOf<Products>()

        val cursor = db.rawQuery(
            "SELECT * FROM $tableName WHERE ${ProductsTable.NAME} LIKE ?",
            arrayOf("%$searchName%")
        )
        cursor.use {
            while(it.moveToNext()) { newList.add(converter(it)) }
        }

        return newList
    }
}