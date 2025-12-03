package com.colarsort.app.models

/**
 * Data class representing Product table for storage
 *
 * @property id - Optional unique identifier
 * @property name - Optional name of product
 * @property image - Optional string that is from a file path
 */
data class Products(val id: Int?,
                    val name: String?,
                    val image: String?
                    ): RowConversion
{
    override fun toRow(): Array<Any?> =
        arrayOf(id, name, image)
}
