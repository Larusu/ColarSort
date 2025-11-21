package com.colarsort.app.models

data class OrderItems(val id: Int?,
                      val orderId: Int?,
                      val productId: Int?,
                      val quantity: Int?
) : RowConversion
{
    override fun toRow(): Array<Any?> =
        arrayOf(id, orderId, productId, quantity)
}
