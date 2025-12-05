package com.colarsort.app.data.pojo

import androidx.room.ColumnInfo

data class PublicUser(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "role") val role: String
)
