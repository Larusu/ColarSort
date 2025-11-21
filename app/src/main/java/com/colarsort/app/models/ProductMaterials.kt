package com.colarsort.app.models

data class ProductMaterials(val id: Int?,
                            val productId: Int?,
                            val materialId: Int?,
                            val quantityRequired: Double?
) : RowConversion
{
    override fun toRow(): Array<Any?> =
        arrayOf(productId, materialId, quantityRequired)
}
