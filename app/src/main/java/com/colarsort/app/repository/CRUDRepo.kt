package com.colarsort.app.repository

import android.content.ContentValues
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.models.RowConversion

abstract class CRUDRepo(private val dbHelper: DatabaseHelper)
{
    /**
     * Column names for this table. The order MUST match the order of values returned by
     * `model.toRow()` in all models using this repository.
     */
    protected abstract val tableName: String
    protected abstract val tableRows: Array<String>

    /**
     * Inserts a model into the database by converting it into row representation.
     * The model must implement [RowConversion], which defines the order and values
     * returned by [RowConversion.toRow]
     *
     * @param model - data model that implements [RowConversion]
     */
    fun insert(model: RowConversion)
    {
        val db = dbHelper.writableDatabase

        val values = model.toRow()
        val cv = ContentValues()

        for (i in values.indices)
        {
            val column = tableRows[i]
            val value = values[i]

            when(value)
            {
                null -> cv.putNull(column)
                is String -> cv.put(column, value)
                is Int -> cv.put(column, value)
                is Double -> cv.put(column, value)
                is ByteArray -> cv.put(column, value)
            }
        }

        db.insert(tableName, null, cv)
        db.close()
    }
}