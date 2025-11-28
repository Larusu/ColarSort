package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.MaterialsTable
import com.colarsort.app.database.ProductMaterialTable
import com.colarsort.app.models.Materials
import kotlin.math.floor

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
            quantity = floor(cursor.getDouble(cursor.getColumnIndexOrThrow(MaterialsTable.QUANTITY)) * 100) / 100.0,
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

    fun checkMaterialQuantity(items: List<Pair<Int, Int>>): List<String>
    {
        val db = dbHelper.readableDatabase
        val requiredMap = mutableMapOf<Int, Double>()

        for ((productId, qty) in items) {
            val cursor = db.rawQuery(
                """
            SELECT material_id, quantity_required
            FROM ${ProductMaterialTable.TABLE_NAME}
            WHERE product_id = ?
            """.trimIndent(),
                arrayOf(productId.toString())
            )

            while (cursor.moveToNext()) {
                val materialId = cursor.getInt(0)
                val requiredPerUnit = cursor.getDouble(1)

                val totalRequired = requiredPerUnit * qty
                requiredMap[materialId] = (requiredMap[materialId] ?: 0.0) + totalRequired
            }

            cursor.close()
        }

        // Check if any material lacks stock
        val insufficient = mutableListOf<String>()

        requiredMap.forEach { (materialId, totalRequired) ->
            val cursor = db.rawQuery(
                """
            SELECT name, quantity
            FROM ${MaterialsTable.TABLE_NAME}
            WHERE id = ?
            """.trimIndent(),
                arrayOf(materialId.toString())
            )

            if (cursor.moveToFirst()) {
                val name = cursor.getString(0)
                val available = cursor.getDouble(1)

                if (available < totalRequired) {
                    insufficient.add(name)
                }
            }

            cursor.close()
        }

        db.close()
        return insufficient
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