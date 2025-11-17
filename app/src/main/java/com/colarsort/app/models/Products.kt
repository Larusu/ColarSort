package com.colarsort.app.models

/**
 * Data class representing Product table for storage
 *
 * @property id - Optional unique identifier
 * @property name - Optional name of product
 * @property image - Optional image data that is stored as ByteArray
 */
data class Products(val id: Integer?,
                    val name: String?,
                    val image: ByteArray?) : RowConversion
{
    override fun toRow(): Array<Any?>
    = arrayOf(name, image)
}
