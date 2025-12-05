package com.colarsort.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Orders(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo val customerName: String,
    @ColumnInfo val status: String,
    @ColumnInfo val expectedDelivery: String
)