package com.colarsort.app.repository

import com.colarsort.app.database.DatabaseHelper
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import com.colarsort.app.database.UserTable
import android.content.ContentValues

class UsersRepo(private val context: Context)
{
    private val dbHelper = DatabaseHelper(context)

    /**
     * To verify the user account, based on the existing accounts
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
     * Creates a new worker account, based on the existing admin user.
     *
     * @param adminName - username of the admin performing action
     * @param newEmployeeUsername - username of the new worker account
     * @param employeePassword - password of the new worker account
     */
    fun assignWorker(adminName: String, newEmployeeUsername: String, employeePassword: String)
    {
        val db: SQLiteDatabase = dbHelper.writableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${UserTable.TABLE_NAME} WHERE ${UserTable.ROLE} = 'Admin' AND ${UserTable.USERNAME} = ?",
            arrayOf(adminName)
        )

        val isAdmin = cursor.count > 0

        cursor.close()

        if(!isAdmin)
        {
            Toast.makeText(context, "You are not an admin!!! (Under construction)", Toast.LENGTH_SHORT).show()
            db.close()
            return
        }

        val values = ContentValues().apply {
            put(UserTable.USERNAME, newEmployeeUsername)
            put(UserTable.ROLE, "Worker")
            put(UserTable.PASSWORD, employeePassword)
        }

        db.insert(UserTable.TABLE_NAME, null, values)

        db.close()
    }
}