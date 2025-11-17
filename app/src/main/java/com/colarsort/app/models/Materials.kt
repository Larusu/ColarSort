package com.colarsort.app.models

data class Materials(val name: String?,
                     val quantity: Double?,
                     val unit: String?,
                     val stockThreshold: String?) : RowConversion
{
    override fun toRow(): Array<Any?> =
        arrayOf(name, quantity, unit, stockThreshold)

}
