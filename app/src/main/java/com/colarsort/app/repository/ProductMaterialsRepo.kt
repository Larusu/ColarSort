package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.ProductMaterialTable
import com.colarsort.app.models.ProductMaterials

class ProductMaterialsRepo(dbHelper: DatabaseHelper) : CRUDRepo<ProductMaterials>(dbHelper)
{
    override val tableName: String = ProductMaterialTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(
        ProductMaterialTable.PRODUCT_ID,
        ProductMaterialTable.MATERIAL_ID,
        ProductMaterialTable.QUANTITY_REQUIRED
    )
    override val idName: String = ProductMaterialTable.ID

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
}