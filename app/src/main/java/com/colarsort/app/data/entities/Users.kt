package com.colarsort.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "user")
data class Users(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val username: String,
    @ColumnInfo val role: String,
    @ColumnInfo val password: String
)
