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
}