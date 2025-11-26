package com.colarsort.app.repository

import android.content.ContentValues
import android.database.Cursor
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.models.RowConversion

abstract class CRUDRepo<T : RowConversion>(protected val dbHelper: DatabaseHelper)
{
    /**
     * Column names for this table. The order MUST match the order of values returned by
     * `model.toRow()` in all models using this repository.
     */
    protected abstract val tableName: String
    protected abstract val tableRows: Array<String>
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

        for (i in 1 until values.size)
        {
            val column = tableRows[i]
            val value = values[i]

            putValue(cv, column, value)
        }

        db.insert(tableName, null, cv)

        db.close()
    }

    /**
     * Retrieves all rows from the table and converts them into model instances.
     *
     *  This method runs a `SELECT *` query on the table and delegates
     *  the row-to-model mapping to [fetchList].
     *
     *  @return A list of all records in the table as model objects.
     */
    open fun getAll() : List<T>
    {
        return fetchList("SELECT * FROM $tableName")
    }

    /**
     * Executes the given SQL query and converts each resulting row into
     * a model instance.
     *
     * This method serves as a reusable helper for SELECT operations.
     * It runs the provided query, iterates through the Cursor results,
     * and uses the repository's row converter to map each row into a
     * corresponding data model.
     *
     * @param query The raw SQL SELECT query to execute.
     * @return A list of model objects produced from the query results.
     */
    protected fun fetchList(query: String) : List<T>
    {
        val db = dbHelper.readableDatabase
        val dataList = mutableListOf<T>()

        val cursor = db.rawQuery(query, null)

        cursor.use {
            while(it.moveToNext()) { dataList.add(converter(it)) }
        }

        db.close()
        cursor.close()

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
        val idName = tableRows[0]

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

        rowsAffected.close()
        db.close()

        return rowsDeleted > 0
    }

    /**
     * Updates an existing record in the database. The first value is treated as the
     * primary key (id) and is used to identify which row should be updated.
     *
     * Only non-null values are written to the database
     *
     * @param model the instance containing the updated data
     * @return `true` if at least one row was successfully updated, otherwise `false`
     *
     * ### Usage example:
     * ```
     * val newProductValue = Products(1, "newProductName", null)
     * val repoProduct = ProductsRepo(this)
     * repoProduct.update(newProductValue)
     * ```
     */
    fun update(model: T): Boolean
    {
        val values = model.toRow()
        val idValue = values[0]

        if(idValue == null) return false

        val db = dbHelper.writableDatabase
        val idName = tableRows[0]

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

    /**
     * Inserts a model into the database and returns the auto-generated row ID.
     *
     * This behaves like the regular `insert()` function but additionally returns
     * the primary key value generated by SQLite after a successful insert.
     *
     * @param model The data model implementing [RowConversion] to be saved.
     * @return The ID of the newly inserted row, or -1 if the insert failed.
     */
    fun insertAndReturnId(model: RowConversion): Long {
        val db = dbHelper.writableDatabase

        val values = model.toRow()
        val cv = ContentValues()

        for (i in 1 until values.size) {
            val column = tableRows[i]
            val value = values[i]
            putValue(cv, column, value)
        }

        val id = db.insert(tableName, null, cv)
        db.close()

        return id
    }


}