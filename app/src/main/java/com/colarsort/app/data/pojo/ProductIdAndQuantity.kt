package com.colarsort.app.data.pojo

import androidx.room.ColumnInfo

data class ProductIdAndQuantity(
    @ColumnInfo(name = "productId") val productId: Int,
    @ColumnInfo(name = "quantity") val quantity: Int
)
