package com.colarsort.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.colarsort.app.data.entities.Users
import com.colarsort.app.data.pojo.UserIdAndRole
import com.colarsort.app.data.pojo.PublicUser

@Dao
interface UserDao
{
    @Insert
    suspend fun insert(model : Users)

    @Query("""
        SELECT id, role
        FROM user
        WHERE username = :username AND password = :password;
    """)
    suspend fun getIdAndRole(username: String, password: String): UserIdAndRole

    @Insert
    suspend fun addWorker(user: Users) : Long

    @Query("""
        SELECT id, username, role
        FROM user
        ORDER BY CASE 
        WHEN role = 'Manager' THEN 0 
        WHEN role = 'Worker' THEN 1 ELSE 2 END, 
        username ASC;
    """)
    suspend fun getUsersData() : List<PublicUser>

    @Query("""
        DELETE FROM user
        WHERE id = :id;
    """)
    suspend fun delete(id: Int) : Int
}