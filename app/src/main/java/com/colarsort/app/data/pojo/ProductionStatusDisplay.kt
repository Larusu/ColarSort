package com.colarsort.app.data.pojo

data class ProductionStatusDisplay(
    val productionStatusId: Int,
    val productName: String,
    val orderId: Int,
    val orderItemId: Int,
    val orderItemsQuantity: Int,
    var cuttingStatus: Boolean,
    var stitchingStatus: Boolean,
    var embroideryStatus: Boolean,
    var finishingStatus: Boolean,
    var completionHandled: Boolean = false
)