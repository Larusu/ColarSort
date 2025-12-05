package com.colarsort.app.data.pojo

import androidx.room.ColumnInfo

data class UserIdAndRole(
    @ColumnInfo(name = "id") val id : Int,
    @ColumnInfo(name = "role") val role : String
)
