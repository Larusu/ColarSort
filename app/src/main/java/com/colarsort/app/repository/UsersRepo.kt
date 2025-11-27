package com.colarsort.app.repository

import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.UserTable
import android.content.ContentValues
import com.colarsort.app.models.Users
import com.colarsort.app.utils.UtilityHelper.hashPassword

class UsersRepo(private val dbHelper: DatabaseHelper)
{
    /**
     * Returns userId if the user exists on the existing accounts
     *
     * @param username username of the user performing action
     * @param password password of the user performing action
     */
    fun getIdAndRoleIfExists(username: String, password: String): Pair<Int, String>
    {
        val db = dbHelper.writableDatabase
        var userId = 0
        var role = ""

        val cursor = db.query(
            UserTable.TABLE_NAME,
            arrayOf(UserTable.ID, UserTable.ROLE),
            "${UserTable.USERNAME} = ? AND ${UserTable.PASSWORD} = ?",
            arrayOf(username, hashPassword(password)),
            null,
            null,
            null
        )
        cursor.use {
            if(it.moveToNext())
            {
                userId = it.getInt(it.getColumnIndexOrThrow(UserTable.ID))
                role = it.getString(it.getColumnIndexOrThrow(UserTable.ROLE))
            }
        }

//        cursor.close()
//        db.close()

        return userId to role
    }

    /**
     * Creates a new worker account by admin and
     * returns true if creating is successful
     *
     * @param newEmployeeUsername username of the new worker account
     * @param employeePassword password of the new worker account
     */
    fun assignWorker(newEmployeeUsername: String, employeePassword: String): Boolean
    {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(UserTable.USERNAME, newEmployeeUsername)
            put(UserTable.ROLE, "Worker")
            put(UserTable.PASSWORD, hashPassword(employeePassword))
        }

        val result = db.insert(UserTable.TABLE_NAME, null, values)

        db.close()

        return result != -1L // returns true if insert of row ID is successful
    }

    /**
     * Retrieves all users from the database, ordered first by role priority and
     * then by username
     *
     * Role ordering is defined as:
     * - Manager -> Highest priority
     * - Worker  -> Lowest priority
     *
     * @return A list of users that is sorted by role priority and username
     */
    fun getAll() : List<Users>
    {
        val db = dbHelper.readableDatabase
        val dataList = mutableListOf<Users>()

        val cursor = db.rawQuery(
            "SELECT ${UserTable.USERNAME}, ${UserTable.ROLE} " +
                    "FROM ${UserTable.TABLE_NAME} " +
                    "ORDER BY " +
                    "   CASE " +
                    "       WHEN ${UserTable.ROLE} = 'Manager' THEN 0 " +
                    "       WHEN ${UserTable.ROLE} = 'Worker' THEN 1" +
                    "       ELSE 2" +
                    "   END," +
                    "${UserTable.USERNAME} ASC",
            arrayOf()
        )

        cursor.use {
            while (it.moveToNext())
            {
                val name = it.getString(it.getColumnIndexOrThrow(UserTable.USERNAME))
                val role = it.getString(it.getColumnIndexOrThrow(UserTable.ROLE))
                val user = Users(null, name, role, null)
                dataList.add(user)
            }
        }

        cursor.close()
        db.close()

        return dataList
    }
}