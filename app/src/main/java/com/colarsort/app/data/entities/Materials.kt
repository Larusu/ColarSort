package com.colarsort.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class Materials(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val quantity: Double,
    @ColumnInfo val unit: String,
    @ColumnInfo val lowStockThreshold: Double,
    @ColumnInfo val image: String?
)