package com.colarsort.app.repository

import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.MaterialsTable
import com.colarsort.app.models.Materials

class MaterialsRepo(val dbHelper: DatabaseHelper) : CRUDRepo<Materials>(dbHelper)
{
    override val tableName: String = MaterialsTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(
        MaterialsTable.NAME,
        MaterialsTable.QUANTITY,
        MaterialsTable.UNIT,
        MaterialsTable.LOW_STOCK_THRESHOLD
    )

}