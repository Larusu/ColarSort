package com.colarsort.app.data.pojo

data class ProductMaterialDetails(
    val productMaterialId: Int,
    val materialId: Int,
    val materialName: String,
    val materialUnit: String,
    val quantityRequired: Double,
    val productName: String,
    val productImage: String?
)