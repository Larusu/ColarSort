package com.colarsort.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "product_material",
    foreignKeys = [ForeignKey(
        entity = Products::class,
        parentColumns = ["id"],
        childColumns = ["productId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Materials::class,
        parentColumns = ["id"],
        childColumns = ["materialId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("productId"),
        Index("materialId")
    ]
)
data class ProductMaterials(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo val productId: Int,
    @ColumnInfo val materialId: Int,
    @ColumnInfo val quantityRequired: Double
)