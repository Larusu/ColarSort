package com.colarsort.app.repository

import android.content.ContentValues
import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.models.RowConversion

abstract class CRUDRepo<T : RowConversion>(private val dbHelper: DatabaseHelper)
{
    /**
     * Column names for this table. The order MUST match the order of values returned by
     * `model.toRow()` in all models using this repository.
     */
    protected abstract val tableName: String
    protected abstract val tableRows: Array<String>
    protected abstract val idName : String
    protected abstract fun converter(cursor: Cursor): T

    /**
     * Inserts a model into the database by converting it into row representation.
     * The model must implement [RowConversion], which defines the order and values
     * returned by [RowConversion.toRow]
     *
     * @param model the data model that implements [RowConversion]
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

            putValue(cv, column, value)
        }

        db.insert(tableName, null, cv)
        db.close()
    }

    /**
     * Retrieves all records from the table and convert it into its corresponding
     * data models instance.
     *
     * This function executes a `SELECT *` query, iterates through the result Cursor,
     * and uses the repository's row converter to map each row into a model
     *
     * @return A list of all records stored in the table, converted to model objects.
     */
    fun getAll() : List<T>
    {
        val db = dbHelper.readableDatabase
        val dataList = mutableListOf<T>()

        val cursor = db.rawQuery("SELECT * FROM $tableName", null)

        cursor.use {
            while(it.moveToNext()) { dataList.add(converter(it)) }
        }

        return dataList
    }

    /**
     * Deletes a row from the table using its primary key value.
     *
     * @param rowId The primary key of the row to delete.
     * @return `true` if a row was deleted, `false` if no matching row exists.
     */
    fun deleteColumn(rowId: Int) : Boolean
    {
        val db = dbHelper.writableDatabase

        val rowsAffected = db.rawQuery(
            "SELECT 1 FROM $tableName WHERE $idName = ? LIMIT 1",
            arrayOf(rowId.toString())
        )

        if(rowsAffected.count <= 0) return false

        val rowsDeleted = db.delete(
            tableName,
            "$idName = ?",
            arrayOf(rowId.toString())
        )

        db.close()
        return rowsDeleted > 0
    }

    /**
     * Updates an existing record in the database. The first value is treated as the
     * primary key (id) and is used to identify which row should be updated.
     *
     * Only non-null values are written in the database
     *
     * @param model the instance containing the updated data
     *
     * @return 'true' if at least one row was successfully updated, otherwise 'false'
     *
     * Usage:
     * val Products(1, "newProduct"Name, null)
     */
    fun update(model: T): Boolean
    {
        val values = model.toRow()
        val idValue = values[0]

        if(idValue == null) return false

        val db = dbHelper.writableDatabase

        val cv = ContentValues()

        for(i in 1 until tableRows.size)
        {
            val value = values[i]
            val column = tableRows[i]

            if(value != null) putValue(cv, column, value)
        }

        val rowsAffected = db.update(
            tableName,
            cv,
            "$idName = ?",
            arrayOf(idValue.toString())
        )

        db.close()

        return rowsAffected > 0
    }

    /**
     * Inserts a value into a [ContentValues] instance, automatically selecting
     * the appropriate `put()` overload based on the runtime type of [data].
     *
     *  @param cv The ContentValues object to populate.
     *  @param key The column name.
     *  @param data The value to insert, which may be null.
     */
    protected fun putValue(cv: ContentValues, key: String, data: Any?)
    {
        when(data)
        {
            null -> cv.putNull(key)
            is String -> cv.put(key, data)
            is Int -> cv.put(key, data)
            is Double -> cv.put(key, data)
            is ByteArray -> cv.put(key, data)
            else -> cv.put(key, data.toString())
        }
    }
}