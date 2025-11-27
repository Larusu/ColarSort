package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.MaterialsTable
import com.colarsort.app.database.ProductMaterialTable
import com.colarsort.app.models.Materials

class MaterialsRepo(dbHelper: DatabaseHelper) : CRUDRepo<Materials>(dbHelper)
{
    override val tableName: String = MaterialsTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(
        MaterialsTable.ID,
        MaterialsTable.NAME,
        MaterialsTable.QUANTITY,
        MaterialsTable.UNIT,
        MaterialsTable.LOW_STOCK_THRESHOLD,
        MaterialsTable.IMAGE
    )

    override fun converter(cursor: Cursor): Materials
    {
        return Materials(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(MaterialsTable.ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(MaterialsTable.NAME)),
            quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(MaterialsTable.QUANTITY)),
            unit = cursor.getString(cursor.getColumnIndexOrThrow(MaterialsTable.UNIT)),
            stockThreshold = cursor.getDouble(cursor.getColumnIndexOrThrow(MaterialsTable.LOW_STOCK_THRESHOLD)),
            image = cursor.getBlob(cursor.getColumnIndexOrThrow(MaterialsTable.IMAGE))
        )
    }

    fun setQuantity(quantity : Int, productId: Int)
    {
        val db = dbHelper.writableDatabase
        val query =
        """
            UPDATE 
                $tableName AS m
            SET 
                ${MaterialsTable.QUANTITY} = ${MaterialsTable.QUANTITY} - (
                    ? * (
                        SELECT ${ProductMaterialTable.QUANTITY_REQUIRED}
                        FROM ${ProductMaterialTable.TABLE_NAME} pm
                        WHERE pm.${ProductMaterialTable.MATERIAL_ID} = m.${MaterialsTable.ID}
                            AND pm.${ProductMaterialTable.PRODUCT_ID} = ?
                    )
                )
            WHERE EXISTS (
                    SELECT 1
                    FROM ${ProductMaterialTable.TABLE_NAME} pm
                    WHERE pm.${ProductMaterialTable.MATERIAL_ID} = m.${MaterialsTable.ID}
                        AND pm.${ProductMaterialTable.PRODUCT_ID} = ?
                )
        """.trimIndent()

        db.execSQL(
            query,
            arrayOf(quantity, productId, productId)
        )
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

    fun searchMaterialBaseOnName(searchName : String) : List<Materials>
    {
        val db = dbHelper.readableDatabase
        val newList = mutableListOf<Materials>()

        val cursor = db.rawQuery(
            "SELECT * FROM $tableName WHERE ${MaterialsTable.NAME} LIKE ?",
            arrayOf("%$searchName%")
        )
        cursor.use {
            while(it.moveToNext()) { newList.add(converter(it)) }
        }

        return newList
    }
}