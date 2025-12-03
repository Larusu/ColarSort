package com.colarsort.app.models

data class Materials(val id: Int?,
                     val name: String?,
                     val quantity: Double?,
                     val unit: String?,
                     val stockThreshold: Double?,
                     val image: String?
                    ) : RowConversion
{
    override fun toRow(): Array<Any?> =
        arrayOf(id, name, quantity, unit, stockThreshold, image)
}
