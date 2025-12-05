package com.colarsort.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "production_status",
    foreignKeys = [ForeignKey(
        entity = OrderItems::class,
        parentColumns = ["id"],
        childColumns = ["orderItemId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("orderItemId")
    ]
)
data class ProductionStatus(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo val orderItemId: Int,
    @ColumnInfo val cuttingStatus: Int,
    @ColumnInfo val stitchingStatus: Int,
    @ColumnInfo val embroideryStatus: Int,
    @ColumnInfo val finishingStatus: Int
)