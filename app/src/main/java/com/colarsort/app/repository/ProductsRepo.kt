package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.MaterialsTable
import com.colarsort.app.database.ProductMaterialTable
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

    fun getById(id: Int): Products? {
        val list = fetchList("SELECT * FROM $tableName WHERE id = $id")
        return list.firstOrNull()
    }

    fun checkMaterialQuantity(quantity: Int, productId : Int) : List<String>
    {
        val db = dbHelper.readableDatabase
        val query =
            """
            SELECT
                m.${MaterialsTable.NAME} AS m_name
            FROM
                ${ProductMaterialTable.TABLE_NAME} AS pm
            JOIN
                ${MaterialsTable.TABLE_NAME} AS m
                ON ${ProductMaterialTable.MATERIAL_ID} = m.${MaterialsTable.ID}
            WHERE
                 pm.${ProductMaterialTable.PRODUCT_ID} = ?
            AND 
                m.${MaterialsTable.QUANTITY} < (pm.${ProductMaterialTable.QUANTITY_REQUIRED} * ?);
            """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(productId.toString(), quantity.toString())
        )

        val insufficientList = mutableListOf<String>()

        while (cursor.moveToNext())
        {
            val matName = cursor.getString(cursor.getColumnIndexOrThrow("m_name"))
            insufficientList.add(matName)
        }

        db.close()
        cursor.close()

        return insufficientList
    }
}