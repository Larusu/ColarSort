package com.colarsort.app.data.pojo

import androidx.room.ColumnInfo

data class MaterialWithQuantity(
    @ColumnInfo(name = "id") val materialId: Int,
    @ColumnInfo(name = "quantity") val quantity: Double = 0.0,
    @ColumnInfo(name = "quantityRequired") val quantityRequired: Double = 0.0
)