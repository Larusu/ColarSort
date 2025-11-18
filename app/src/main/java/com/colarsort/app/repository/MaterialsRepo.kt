package com.colarsort.app.repository

import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.MaterialsTable
import com.colarsort.app.models.Materials

class MaterialsRepo(dbHelper: DatabaseHelper) : CRUDRepo<Materials>(dbHelper)
{
    override val tableName: String = MaterialsTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(
        MaterialsTable.NAME,
        MaterialsTable.QUANTITY,
        MaterialsTable.UNIT,
        MaterialsTable.LOW_STOCK_THRESHOLD
    )

    override fun converter(cursor: Cursor): Materials
    {
        return Materials(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(MaterialsTable.ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(MaterialsTable.NAME)),
            quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(MaterialsTable.QUANTITY)),
            unit = cursor.getString(cursor.getColumnIndexOrThrow(MaterialsTable.UNIT)),
            stockThreshold = cursor.getDouble(cursor.getColumnIndexOrThrow(MaterialsTable.LOW_STOCK_THRESHOLD))
        )
    }
}