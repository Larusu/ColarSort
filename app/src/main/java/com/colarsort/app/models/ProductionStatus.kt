package com.colarsort.app.models

data class ProductionStatus(val id: Int?,
                            val orderItemId: Int?,
                            val cuttingStatus: Int?,
                            val stitchingStatus: Int?,
                            val embroideryStatus: Int?,
                            val finishingStatus: Int?
) : RowConversion
{
    override fun toRow(): Array<Any?> =
        arrayOf(orderItemId, cuttingStatus, stitchingStatus, embroideryStatus, finishingStatus)
}
