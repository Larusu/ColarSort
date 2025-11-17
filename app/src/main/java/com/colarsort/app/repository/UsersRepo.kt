package com.colarsort.app.repository

import com.colarsort.app.database.DatabaseHelper
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import com.colarsort.app.database.UserTable
import android.content.ContentValues

class UsersRepo(private val dbHelper: DatabaseHelper)
{
    /**
     * Returns true if the user exists on the existing accounts
     *
     * @param username - username of the user performing action
     * @param password - password of the user performing action
     */
    fun validateCredentials(username: String, password: String): Boolean
    {
        val db: SQLiteDatabase = dbHelper.writableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${UserTable.TABLE_NAME} WHERE ${UserTable.USERNAME} = ? AND ${UserTable.PASSWORD} = ?",
            arrayOf(username, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()

        return exists
    }

    /**
     * Creates a new worker account by admin and
     * returns true if creating is successful
     *
     * @param newEmployeeUsername - username of the new worker account
     * @param employeePassword - password of the new worker account
     */
    fun assignWorker(newEmployeeUsername: String, employeePassword: String): Boolean
    {
        val db: SQLiteDatabase = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(UserTable.USERNAME, newEmployeeUsername)
            put(UserTable.ROLE, "Worker")
            put(UserTable.PASSWORD, employeePassword)
        }

        val result = db.insert(UserTable.TABLE_NAME, null, values)

        db.close()

        return result != -1L // returns true if insert of row ID is successful
    }
}