package com.colarsort.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "order_items",
    foreignKeys = [ForeignKey(
        entity = Orders::class,
        parentColumns = ["id"],
        childColumns = ["orderId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Products::class,
        parentColumns = ["id"],
        childColumns = ["productId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("orderId"),
        Index("productId")
    ]
)
data class OrderItems(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo val orderId: Int,
    @ColumnInfo val productId: Int,
    @ColumnInfo val quantity: Int
)