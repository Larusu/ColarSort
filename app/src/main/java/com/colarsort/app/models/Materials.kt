package com.colarsort.app.models

data class Materials(val id: Int?,
                     val name: String?,
                     val quantity: Double?,
                     val unit: String?,
                     val stockThreshold: Double?
                    ) : RowConversion
{
    override fun toRow(): Array<Any?> =
        arrayOf(name, quantity, unit, stockThreshold)

}
