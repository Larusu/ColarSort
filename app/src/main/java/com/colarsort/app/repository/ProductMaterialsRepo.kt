package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.MaterialsTable
import com.colarsort.app.database.OrderItemsTable
import com.colarsort.app.database.ProductMaterialTable
import com.colarsort.app.database.ProductsTable
import com.colarsort.app.models.ProductMaterials

data class ProductMaterialDetails(
    val productMaterialId: Int,
    val materialId: Int,
    val materialName: String,
    val materialUnit: String,
    val quantityRequired: Double,
    val productName: String,
    val productImage: ByteArray?
)
class ProductMaterialsRepo(dbHelper: DatabaseHelper) : CRUDRepo<ProductMaterials>(dbHelper)
{
    override val tableName: String = ProductMaterialTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(
        ProductMaterialTable.ID,
        ProductMaterialTable.PRODUCT_ID,
        ProductMaterialTable.MATERIAL_ID,
        ProductMaterialTable.QUANTITY_REQUIRED
    )

    override fun converter(cursor: Cursor): ProductMaterials {
        return ProductMaterials(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductMaterialTable.ID)),
            productId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductMaterialTable.PRODUCT_ID)),
            materialId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductMaterialTable.MATERIAL_ID)),
            quantityRequired = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductMaterialTable.QUANTITY_REQUIRED))
        )
    }

    override fun getAll(): List<ProductMaterials> {
        // not specified yet
        return super.getAll()
    }

    fun getMaterialsPerProduct(productId: Int) : List<ProductMaterialDetails>
    {
        val db = dbHelper.readableDatabase
        val listOfMaterials = mutableListOf<ProductMaterialDetails>()

        val query =
            """
            SELECT
                pm.${ProductMaterialTable.ID} AS pm_id,
                m.${MaterialsTable.ID} AS m_id,
                m.${MaterialsTable.NAME} AS m_name,
                m.${MaterialsTable.UNIT} AS m_unit,
                pm.${ProductMaterialTable.QUANTITY_REQUIRED} AS pm_qr,
                p.${ProductsTable.NAME} AS p_name,
                p.${ProductsTable.IMAGE} AS p_image
            FROM
                ${ProductMaterialTable.TABLE_NAME} AS pm
            JOIN
                ${MaterialsTable.TABLE_NAME} AS m 
                ON pm.${ProductMaterialTable.MATERIAL_ID} = m.${MaterialsTable.ID}
            JOIN
                ${ProductsTable.TABLE_NAME} AS p 
                ON pm.${ProductMaterialTable.PRODUCT_ID} = p.${ProductsTable.ID}
            WHERE
                p.${ProductsTable.ID} = ?;
            """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(productId.toString()))

        cursor.use {
            while(it.moveToNext())
            {
                val details = ProductMaterialDetails(
                    productMaterialId = it.getInt(it.getColumnIndexOrThrow("pm_id")),
                    materialId = it.getInt(it.getColumnIndexOrThrow("m_id")),
                    materialName = it.getString(it.getColumnIndexOrThrow("m_name")),
                    materialUnit = it.getString(it.getColumnIndexOrThrow("m_unit")),
                    quantityRequired = it.getDouble(it.getColumnIndexOrThrow("pm_qr")),
                    productName = it.getString(it.getColumnIndexOrThrow("p_name")),
                    productImage = it.getBlob(it.getColumnIndexOrThrow("p_image"))
                )
                listOfMaterials.add(details)
            }
        }

        return listOfMaterials
    }

    fun deleteProductById(productId: Int?)
    {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.rawQuery(
            "SELECT 1 FROM ${ProductMaterialTable.TABLE_NAME} WHERE ${ProductMaterialTable.PRODUCT_ID} = ? LIMIT 1",
            arrayOf(productId.toString())
        )

        if(rowsAffected.count <= 0) return

        db.delete(
            ProductMaterialTable.TABLE_NAME,
            "${ProductMaterialTable.PRODUCT_ID} = ?",
            arrayOf(productId.toString())
        )
        rowsAffected.close()
        db.close()
    }

    fun isMaterialUsedInAnyOrder(materialId: Int): Boolean {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT COUNT(*)
        FROM ${OrderItemsTable.TABLE_NAME} oi
        INNER JOIN ${ProductMaterialTable.TABLE_NAME} pm
        ON oi.${OrderItemsTable.PRODUCT_ID} = pm.${ProductMaterialTable.PRODUCT_ID}
        WHERE pm.${ProductMaterialTable.MATERIAL_ID} = ?
        """.trimIndent(),
            arrayOf(materialId.toString())
        )

        cursor.use {
            return if (it.moveToFirst()) it.getInt(0) > 0 else false
        }
    }

}