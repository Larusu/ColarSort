package com.colarsort.app.repository

import com.colarsort.app.database.DatabaseHelper
import android.content.Context
import com.colarsort.app.database.UserTable

class UsersRepo(private val context: Context)
{
    val dbHelper = DatabaseHelper(context)

    public fun validateCredentials(username: String, password: String): Boolean
    {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${UserTable.TABLE_NAME} WHERE ${UserTable.USERNAME} = ? AND ${UserTable.PASSWORD} = ?",
            arrayOf(username, password)
        )
        val exists = cursor.count > 0
        cursor.close()

        return exists
    }   
}