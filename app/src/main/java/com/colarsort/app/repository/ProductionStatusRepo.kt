package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.ProductionStatusTable
import com.colarsort.app.models.ProductionStatus

class ProductionStatusRepo(dbHelper: DatabaseHelper) : CRUDRepo<ProductionStatus>(dbHelper)
{
    override val tableName: String = ProductionStatusTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(
        ProductionStatusTable.ORDER_ITEM_ID,
        ProductionStatusTable.CUTTING_STATUS,
        ProductionStatusTable.STITCHING_STATUS,
        ProductionStatusTable.EMBROIDERY_STATUS,
        ProductionStatusTable.FINISHING_STATUS
    )

    override val idName: String = ProductionStatusTable.ID

    override fun converter(cursor: Cursor): ProductionStatus {
        return ProductionStatus(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductionStatusTable.ID)),
            orderItemId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductionStatusTable.ORDER_ITEM_ID)),
            cuttingStatus = cursor.getInt(cursor.getColumnIndexOrThrow(ProductionStatusTable.CUTTING_STATUS)),
            stitchingStatus = cursor.getInt(cursor.getColumnIndexOrThrow(ProductionStatusTable.STITCHING_STATUS)),
            embroideryStatus = cursor.getInt(cursor.getColumnIndexOrThrow(ProductionStatusTable.EMBROIDERY_STATUS)),
            finishingStatus = cursor.getInt(cursor.getColumnIndexOrThrow(ProductionStatusTable.FINISHING_STATUS))
        )
    }

    override fun getAll(): List<ProductionStatus> {
        // not specified yet
        return super.getAll()
    }
}