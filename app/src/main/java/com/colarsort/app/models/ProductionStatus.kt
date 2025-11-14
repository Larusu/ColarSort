package com.colarsort.app.models

data class ProductionStatus(val id: Integer,
                            val orderItemId: Integer,
                            val cuttingStatus: Integer,
                            val stitchingStatus: Integer,
                            val embroideryStatus: Integer,
                            val finishingStatus: Integer)
