package com.colarsort.app.models

data class Orders(val id: Int?,
                  val customerName: String?,
                  val status: String?,
                  val expectedDelivery: String?
                  ) : RowConversion
{
    override fun toRow(): Array<Any?> =
        arrayOf(id, customerName, status, expectedDelivery)
}